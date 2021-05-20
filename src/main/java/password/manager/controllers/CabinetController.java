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

    private CabinetDataAccessor dataAccessor;
    private List<Data> allData;

    private Logger logger = Logger.getLogger("Log");
    private Appender fh;

    private static final String UPDATE_DATA = "UPDATE data SET url = ?, login = ?, password = ?, notes = ? WHERE url = ?";
    private static final String DELETE_ERR = "Error while deleting entry.";

    private Connection conn = null;
    private PreparedStatement pst = null;
    private Integer loggedInUserId;

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

    public void refreshTable() {
        initLoggerSettings();

        if (table.getItems() != null) {
            table.getItems().clear();
        }
        if (allData == null) {
            allData = new ArrayList<>();
        }
        loggedInUserId = LoginController.authorizedUserID();
        deactivateButtons(true);
        add.setDisable(false);
        clear.setDisable(false);
        table.setDisable(false);
        clearFields();

        try {
            if (dataAccessor == null) {
                dataAccessor = new CabinetDataAccessor();
            }
            ObservableList<Data> data = dataAccessor.getAllData(loggedInUserId);
            System.out.println("Data fetched from DB size " + data.size());
            allData.addAll(data);
            table.setItems(data);

        } catch (Exception e) {
            PopupUtils.showErrorDialog("Error loading data.");
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
        deactivateButtons(false);
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
                PopupUtils.showErrorDialog("Trying to display data in table but error occurred.");
                logger.debug("Trying to display data in table but error occurred.");
                e.printStackTrace();
            }
        }
    }

    public void addEntry() {
        loggedInUserId = LoginController.authorizedUserID();
        Data newEntry = new Data(urlToAdd.getText(), usernameToAdd.getText(),
                passwordToAdd.getText(), notesToAdd.getText(), loggedInUserId);

        boolean isValid = Validator.validateEntryFields(newEntry);

        String currentUrl = urlToAdd.getText();
        if (isValid) {
            try {
                Data savedEntry = dataAccessor.addEntry(newEntry);
                allData.add(savedEntry);
            } catch (Exception e) {
                PopupUtils.showErrorDialog("Trying to add new entry but error occurred.");
                e.printStackTrace();
            }
            clearFields();
            refreshTable();
            PopupUtils.showInformationDialog("Entry has been added successfully.");
        }
        logger.debug(LocalDateTime.now() + ": New entry with url '" + currentUrl + "' added.");
    }

    public void updateEntry() {
        Data selectedItem = (Data) table.getSelectionModel().getSelectedItem();
        Data data = getCurrentEntry(selectedItem);

        String currentPassword = "";

        boolean isValid = Validator.validateEntryFields(data);

        Optional<ButtonType> action;
        if (isValid) {
            action = PopupUtils.showConfirmationDialog("Are you sure you want to update entry?");
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
                    refreshTable();
                    PopupUtils.showInformationDialog("Entry has been updated successfully.");
                    logger.debug(LocalDateTime.now() + ": Entry with Web name '" + selectedItem.getUrl() + "' has been updated.");
                }
            }
        }
    }

    public void deleteEntry() {
        Data data = (Data) table.getSelectionModel().getSelectedItem();
        Data currentItem = getCurrentEntry(data);
        Optional<ButtonType> action = PopupUtils.showConfirmationDialog("Are you sure you want to delete entry?");

        if (action.isPresent()) {
            if (action.get() == ButtonType.OK) {
                try {
                    dataAccessor.deleteEntry(currentItem);
                    allData.remove(currentItem);

                    table.getItems().remove(data);
                } catch (Exception e) {
                    logger.debug(LocalDateTime.now() + ": " + DELETE_ERR);
                    PopupUtils.showErrorDialog(DELETE_ERR);
                }
                clearFields();
                PopupUtils.showInformationDialog("Entry has been deleted successfully.");
            }
        }
        logger.debug(LocalDateTime.now() + ": Entry" + currentItem.getId() + " has been deleted.");
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
        deactivateButtons(true);
        urlToAdd.clear();
        usernameToAdd.clear();
        passwordToAdd.clear();
        notesToAdd.clear();
        generatedPassword.clear();
    }

    private void deactivateButtons(boolean value) {
        showpass.setDisable(value);
        update.setDisable(value);
        delete.setDisable(value);
    }
}