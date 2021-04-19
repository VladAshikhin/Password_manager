package password.manager.controllers;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.apache.log4j.*;
import password.manager.accessors.CabinetDataAccessor;
import password.manager.utils.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.stage.Stage;
import password.manager.utils.Validator;
import password.manager.entity.Data;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CabinetController {
    private Logger logger = Logger.getLogger("Log");
    private Appender fh;

    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/kstore?serverTimezone=UTC";
    private static final String USERNAME = "vashikhi";
    private static final String PASSWORD = "Maxtomcat91!";

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

    private CabinetDataAccessor dataAccessor;
    private List<Data> allData;

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
            logger.error(LocalDateTime.now() + " Exception occurred:\n" + e);
        }
    }

    public void loadTable() {
        allData = new ArrayList<>();
        loggedInUserId = LoginController.authorizedUserID();
        setButtonsActive(true);
        add.setDisable(false);
        clear.setDisable(false);
        table.setDisable(false);
        clearFields();

        try {
            dataAccessor = new CabinetDataAccessor();
            ObservableList<Data> data = dataAccessor.getAllData(loggedInUserId);
            allData.addAll(data);
            table.setItems(data);

        } catch (Exception e) {
            PopupUtils.showError("Error loading data.");
            logger.error(LocalDateTime.now() + " Error executing SQL query.");
            e.printStackTrace();
        }
    }

    public Data getCurrentEntry(Data tableEntry) {
        return allData.stream()
                .filter(it -> it.equals(tableEntry))
                .findFirst()
                .orElse(null);
    }


    public void onTableClick() {
        clearFields();
        setButtonsActive(true);
        if (table.getItems() != null) {
            try {
                Data selectedItem = (Data) table.getSelectionModel().getSelectedItem();
                Data currentItem = getCurrentEntry(selectedItem);

                if (currentItem != null) {
                    urlToAdd.setText(currentItem.getUrl());
                    usernameToAdd.setText(currentItem.getLogin());
                    passwordToAdd.setText(currentItem.getHiddenPassword());
                    notesToAdd.setText(currentItem.getNotes());

                    showpass.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (showpass.isSelected()) {
                            passwordToAdd.setText(currentItem.getPassword());
                        } else {
                            passwordToAdd.setText(currentItem.getHiddenPassword());
                        }
                    });
                }
            } catch (Exception e) {
                PopupUtils.showError("Trying to display data in table but error occurred.");
                logger.debug("Trying to display data in table but error occurred.");
                e.printStackTrace();
            }
        }
    }

    public void addEntry() {
        initLoggerSettings();
        boolean isValid = Validator.validateEntryFields(urlToAdd.getText(), usernameToAdd.getText(),
                passwordToAdd.getText(), notesToAdd.getText());
        loggedInUserId = LoginController.authorizedUserID();
        String currentUrl = urlToAdd.getText();
        if (isValid) {
            try {
                Class.forName(DB_DRIVER);

                Driver driver = new com.mysql.cj.jdbc.Driver();
                DriverManager.registerDriver(driver);

                conn = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
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
            PopupUtils.entryAdded();
        }
        logger.debug(LocalDateTime.now() + ": New entry with url '" + currentUrl + "' added.");
    }

    public void updateEntry() {
        initLoggerSettings();
        Data data = (Data) table.getSelectionModel().getSelectedItem();

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
            action = PopupUtils.confirmUpdate();
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
                    PopupUtils.entryUpdated();
                    logger.debug(LocalDateTime.now() + ": Entry with Web name '" + data.getUrl() + "' has been updated.");
                }
            }
        }
    }

    public void deleteEntry() {
        Data data = (Data) table.getSelectionModel().getSelectedItem();
        String webName = data.getUrl();
        Optional<ButtonType> action = PopupUtils.confirmDelete();

        if (action.isPresent()) {
            if (action.get() == ButtonType.OK) {
                try {
                    table.getItems().remove(data);

                    pst = conn.prepareStatement(DELETE_FROM_DATA);
                    pst.setString(1, data.getLogin());

                    pst.executeUpdate();
                    pst.close();
                } catch (Exception e) {
                    logger.debug(LocalDateTime.now() + ": " + e.getClass() + " " + DELETE_ERR);
                    PopupUtils.showError(DELETE_ERR);
                }
                clearFields();
                PopupUtils.entryDeleted();
            }
        }
        logger.debug(LocalDateTime.now() + ": Entry with Web name '" + webName + "' has been deleted.");
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

    private void setButtonsActive(boolean value) {
        showpass.setDisable(value);
        update.setDisable(value);
        delete.setDisable(value);
    }
}