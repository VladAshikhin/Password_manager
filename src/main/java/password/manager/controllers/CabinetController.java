package password.manager.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.apache.log4j.*;
import password.manager.utils.PopUp;
import password.manager.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import password.manager.utils.Validator;
import password.manager.domain.Entity.Datarow;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class CabinetController {
    private Logger logger = Logger.getLogger("Log");
    private Appender fh;

    private static final String DATABASE_URL = "jdbc:sqlite:C:\\sqlite\\pmanager.db";
    private static final String FIND_ALL_DATA = "SELECT * FROM data";
    private static final String FIND_ALL_DATA_BY_ID = "SELECT * FROM data WHERE user_id = ?";
    private static final String FIND_BY_USER = "SELECT * FROM data WHERE login = ?";
    private static final String INSERT_INTO_DATA = "INSERT INTO data (url, login, password, notes, user_id) VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE_FROM_DATA = "DELETE FROM data WHERE login = ?";
    private static final String UPDATE_DATA = "UPDATE data SET url = ?, login = ?, password = ?, notes = ? WHERE url = ?";
    private static final String GET_PASS = "SELECT password FROM data WHERE url = ? and login = ?";

    private static final String DELETE_ERR = "Error while deleting entry.";

    private static Connection conn = null;
    private static PreparedStatement pst = null;
    private static ResultSet rs = null;
    private static Integer loggedInUserId;

    @FXML
    TableView table;
    @FXML
    ToggleButton showpass;
    @FXML
    Button add;
    @FXML
    Button update;
    @FXML
    Button delete;
    @FXML
    Button clear;

    @FXML
    TextField urlToAdd;
    @FXML
    TextField usernameToAdd;
    @FXML
    TextField passwordToAdd;
    @FXML
    TextArea notesToAdd;
    @FXML
    TextField generatedPassword;
    @FXML
    Button copy;

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

    public void onTableClick() {
        clearFields();
        setButtonsActive(true);
        if (table.getItems() != null) {
            try {
                Datarow datarow = (Datarow) table.getSelectionModel().getSelectedItem();
                if (datarow != null) {
                    pst = conn.prepareStatement(FIND_BY_USER);
                    pst.setString(1, datarow.getLogin());

                    rs = pst.executeQuery();

                    while (rs.next()) {
                        urlToAdd.setText(rs.getString("url"));
                        usernameToAdd.setText(rs.getString("login"));
                        passwordToAdd.setText(rs.getString("password"));
                        notesToAdd.setText(rs.getString("notes"));
                    }
                    String currentPasswordValue = passwordToAdd.getText();
                    passwordToAdd.setText(Utils.hidePassword(currentPasswordValue));

                    showpass.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (showpass.isSelected()) {
                            passwordToAdd.setText(currentPasswordValue);
                        } else {
                            passwordToAdd.setText(Utils.hidePassword(currentPasswordValue));
                        }
                    });
                }
            } catch (NullPointerException e) {
                System.out.println("No data chosen.");
            } catch (Exception e) {
                PopUp.showError("Unexpected error occurred.");
                logger.debug("Unexpected error occurred.");
                e.printStackTrace();
            }
        }
    }

    public void logOut(ActionEvent event) throws Exception {
        initLoggerSettings();
        String loggedInUser = LoginController.authorizedUserLogin();
        System.out.println(LocalDateTime.now() + ": User '" + loggedInUser + "' logged out.");
        logger.debug(LocalDateTime.now() + ": User '" + loggedInUser + "' logged out.");

        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("login.fxml")));
        Scene authScene = new Scene(root, 300, 180);
        currentStage.setScene(authScene);
    }

    public void addEntry() {
        initLoggerSettings();
        boolean isValid = Validator.validateEntryFields(urlToAdd.getText(), usernameToAdd.getText(),
                passwordToAdd.getText(), notesToAdd.getText());
        loggedInUserId = LoginController.authorizedUserID();
        String currentUrl = urlToAdd.getText();
        if (isValid) {
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection(DATABASE_URL);
                pst = conn.prepareStatement(INSERT_INTO_DATA);

                pst.setString(1, currentUrl);
                pst.setString(2, usernameToAdd.getText());
                pst.setString(3, passwordToAdd.getText());
                pst.setString(4, notesToAdd.getText());
                pst.setInt(5, loggedInUserId);

                pst.execute();
                pst.close();
            } catch (Exception e) {
                System.out.println("======= " + e.getClass() + " =======");
                e.printStackTrace();
            }
            clearFields();
            table.getItems().clear();
            loadTable();
            PopUp.entryAdded();
        }
        logger.debug(LocalDateTime.now() + ": New entry with url '" + currentUrl + "' added.");
    }

    public void updateEntry() {
        initLoggerSettings();
        Datarow datarow = (Datarow) table.getSelectionModel().getSelectedItem();

        String currentPassword = "";

        try {
            pst = conn.prepareStatement(GET_PASS);
            pst.setString(1, urlToAdd.getText());
            pst.setString(2, usernameToAdd.getText());

            rs = pst.executeQuery();

            currentPassword = rs.getString("password");

        } catch (SQLException e) {
            logger.debug(LocalDateTime.now() + ": Couldn't get entry details.");
            e.printStackTrace();
        } catch (Exception e) {
            logger.debug(LocalDateTime.now() + ": Failed to update entry.");
        }

        boolean isValid = Validator.validateEntryFields(urlToAdd.getText(), usernameToAdd.getText(),
                currentPassword, notesToAdd.getText());

        Optional<ButtonType> action;
        if (isValid) {
            action = PopUp.confirmUpdate();
        } else {
            logger.debug(LocalDateTime.now() + ": Failed to update entry.");
            return;
        }

        if (action.isPresent()) {
            if (action.get() == ButtonType.OK) {
                if (isValid) {
                    try {
                        pst = conn.prepareStatement(UPDATE_DATA);
                        String searchParam = urlToAdd.getText();

                        pst.setString(1, urlToAdd.getText());
                        pst.setString(2, usernameToAdd.getText());
                        pst.setString(3, currentPassword); // decode password
                        pst.setString(4, notesToAdd.getText());
                        pst.setString(5, searchParam);

                        pst.executeUpdate();
                        pst.close();
                    } catch (Exception e) {
                        System.out.println("======= " + e.getClass() + " =======");
                        e.printStackTrace();
                    }
                    clearFields();
                    table.getItems().clear();
                    loadTable();
                    PopUp.entryUpdated();
                    logger.debug(LocalDateTime.now() + ": Entry with Web name '" + datarow.getUrl() + "' has been updated.");
                }
            }
        }
    }

    public void deleteEntry() {
        Datarow datarow = (Datarow) table.getSelectionModel().getSelectedItem();
        String webName = datarow.getUrl();
        Optional<ButtonType> action = PopUp.confirmDelete();

        if (action.isPresent()) {
            if (action.get() == ButtonType.OK) {
                try {
                    table.getItems().remove(datarow);

                    pst = conn.prepareStatement(DELETE_FROM_DATA);
                    pst.setString(1, datarow.getLogin());

                    pst.executeUpdate();
                    pst.close();
                } catch (Exception e) {
                    logger.debug(LocalDateTime.now() + ": " + e.getClass() + " " + DELETE_ERR);
                    PopUp.showError(DELETE_ERR);
                }
                clearFields();
                PopUp.entryDeleted();
            }
        }
        logger.debug(LocalDateTime.now() + ": Entry with Web name '" + webName + "' has been deleted.");
    }

    public void loadTable() {
        loggedInUserId = LoginController.authorizedUserID();
        setButtonsActive(true);
        add.setDisable(false);
        clear.setDisable(false);
        table.setDisable(false);
        clearFields();

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DATABASE_URL);
            if (loggedInUserId.equals(999)) {
                pst = conn.prepareStatement(FIND_ALL_DATA);
            } else {
                pst = conn.prepareStatement(FIND_ALL_DATA_BY_ID);
                pst.setInt(1, loggedInUserId);
            }
            rs = pst.executeQuery();

            ObservableList<Datarow> data = FXCollections.observableArrayList();

            while (rs.next()) {
                data.add(new Datarow(
                        rs.getString("url"),
                        rs.getString("login"),
                        Utils.hidePassword(rs.getString("password")),
                        rs.getString("notes")
                ));
                table.setItems(data);
            }

            pst.close();
            rs.close();
        } catch (SQLException e) {
            PopUp.showError("Error loading data.");
            logger.error("Error executing SQL query.");
            e.printStackTrace();
        } catch (NullPointerException e) {
            PopUp.showError("No data found!");
            logger.error("SQL query returned null.");
            e.printStackTrace();
        } catch (Exception e) {
            PopUp.showError("Error loading data.");
            e.printStackTrace();
        }
        logger.error(LocalDateTime.now() + " Table is loaded.");
    }

    public void generatePassword() {
        generatedPassword.setText(Utils.generatePassword());
    }

    public void copyToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(generatedPassword.getText());
        clipboard.setContent(content);
    }

    public void clearFields() {
        showpass.setSelected(false);
        setButtonsActive(false);
        urlToAdd.clear();
        usernameToAdd.clear();
        passwordToAdd.clear();
        notesToAdd.clear();
        generatedPassword.clear();
    }

    private void setButtonsActive(boolean active) {
        showpass.setDisable(active);
        update.setDisable(active);
        delete.setDisable(active);
    }
}