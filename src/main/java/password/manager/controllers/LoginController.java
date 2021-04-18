package password.manager.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import password.manager.accessors.LoginDataAccessor;
import password.manager.utils.PopupUtils;
import password.manager.entity.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class LoginController {
    private Logger logger = Logger.getLogger("Log");
    private Appender fh;

    private static final String CREDS = "Credentials";
    private static final String BAD_CREDENTIALS = "Incorrect login or password!";
    private static final String OK = "Ok";
    private static final String NO_SUCH_USER = "User doesn't exist!";
    private static final String NEW_USER_INVALID = "Login and password fields should be\nbetween 5 and 30 characters!";
    private static final String NEW_USER_CREATED = "New user created successfully!";
    private static final String AUTH_ERROR = "Unknown authentication error occurred.";

    private static String loggedInUser;
    private static Integer authorizedUserId;

    private LoginDataAccessor dataAccessor;

    @FXML
    private Label loginAlertLabel;
    @FXML
    TextField login;
    @FXML
    PasswordField password;
    @FXML
    Button loginButton;

    private void initLoggerSettings() {
        try {
            fh = new FileAppender(new SimpleLayout(), "logger.log");
            logger.addAppender(fh);
            fh.setLayout(new SimpleLayout());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(LocalDateTime.now() + "Exception occurred:\n" + e);
        }
    }

    static Integer authorizedUserID() {
        if (authorizedUserId != null) {
            return authorizedUserId;
        } else {
            return 0;
        }
    }

    static String authorizedUserLogin() {
        if (loggedInUser != null && !loggedInUser.equals("")) {
            return loggedInUser;
        } else {
            return "undefined";
        }
    }

    public void addUser() {
        initLoggerSettings();
        Map<String, Object> newUserObject = PopupUtils.addUser();

        String newLogin = (String) newUserObject.get("login");
        String newPassword = (String) newUserObject.get("password");
        Optional<ButtonType> action = (Optional<ButtonType>) newUserObject.get("type");
        boolean isValid = (boolean) newUserObject.get("valid");

        try {
            if (action.get() == ButtonType.OK) {
                if (isValid) {
                    PopupUtils.userCreated(NEW_USER_CREATED);
                    logger.info(LocalDateTime.now() + ": New user with login '" + newLogin + "' created successfully.");
                    setAlertTextIncorrectField(OK);
                    try {
                        dataAccessor = new LoginDataAccessor();
                        dataAccessor.insertUser(newLogin, newPassword);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // For some reason number of log messages is multiplied
                    logger.info("Failed to create new user.");
                    setAlertTextIncorrectField(NEW_USER_INVALID);
                }
            } else {
                logger.debug("User adding process has been canceled.");
            }
        } catch (NullPointerException e) {
            logger.debug("User adding process has been canceled.");
        }
    }

    private void setAlertTextIncorrectField(String field) {
        loginAlertLabel.setTextFill(Color.FIREBRICK);
        switch (field) {
            case CREDS:
                loginAlertLabel.setText(BAD_CREDENTIALS);
                login.setStyle("-fx-border-color: firebrick;");
                password.setStyle("-fx-border-color: grey;");
                break;
            case OK:
                loginAlertLabel.setText("");
                login.setStyle("-fx-border-color: grey;");
                password.setStyle("-fx-border-color: grey;");
                break;
            case NEW_USER_INVALID:
                loginAlertLabel.setText(NEW_USER_INVALID);
                break;
        }
    }

    private void clearFields() {
        login.clear();
        password.clear();
    }

    private void changeScene(Stage currentStage, Scene newScene) {
        currentStage.setScene(newScene);
        currentStage.setX(100);
        currentStage.setY(100);
        setAlertTextIncorrectField(OK);
        clearFields();
    }

    private boolean allowAccess() {
        boolean accessGranted = false;
        boolean credentialsValid = false;
        String inputLogin = login.getText();
        String inputPassword = password.getText();


        try {

            dataAccessor = new LoginDataAccessor();

            final List<User> userList = dataAccessor.getUserList();

            for (User user : userList) {
                if (user.isActive()) {
                    if (user.getUsername().equals(inputLogin) && user.getPassword().equals(inputPassword)) {
                        accessGranted = true;
                        credentialsValid = true;

                        authorizedUserId = user.getId();
                        loggedInUser = user.getUsername();
                        break;
                    } else {
                        accessGranted = false;
                        credentialsValid = false;
                    }
                }
            }

            if (!credentialsValid) {
                setAlertTextIncorrectField(BAD_CREDENTIALS);
                clearFields();
                logger.warn(LocalDateTime.now() + BAD_CREDENTIALS);
            }
        } catch (Exception e) {
            setAlertTextIncorrectField(AUTH_ERROR);
            clearFields();
            logger.warn(LocalDateTime.now() + ": " + AUTH_ERROR);
            e.printStackTrace();
        }
        return accessGranted;
    }

    // what kind of exception we expect?
    public void handleSubmit(ActionEvent event) throws Exception {
        initLoggerSettings();

        String inputLogin = login.getText();
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("table.fxml"));
        Scene successScene = new Scene(root, 935, 500);
        boolean grantAccess = allowAccess();

        if (grantAccess) {
            changeScene(currentStage, successScene);
            clearFields();
            setAlertTextIncorrectField(OK);
            logger.debug(LocalDateTime.now() + ": User '" + inputLogin + "' logged in successfully.");
        } else {
            logger.warn(LocalDateTime.now() + ": Access denied.");
        }
    }

    public void onEnter(KeyEvent event) throws Exception {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String inputLogin = login.getText();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("table.fxml"));
            Scene successScene = new Scene(root, 935, 500);
            boolean grantAccess = allowAccess();

            if (grantAccess) {
                changeScene(currentStage, successScene);
                setAlertTextIncorrectField(OK);
                logger.debug(LocalDateTime.now() + ": User '" + inputLogin + "' logged in successfully.");
            } else {
                logger.warn(LocalDateTime.now() + ": Access denied.");
            }
            clearFields();
        }
    }
}