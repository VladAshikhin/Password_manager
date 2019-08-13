/*
package password.manager;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class PassMan {

    private static Connection conn = null;
    private static PreparedStatement pst = null;
    private static ResultSet rs = null;

    private static String query = null;

    public static void main(String[] args) {
        launch(args);
    }

    public static boolean grantAccess(TextField login, TextField password, Label label) {
        boolean isGrant = false;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:C:\\sqlite\\passwordManager.db");
            query = "SELECT * FROM users";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            ArrayList<User> users = new ArrayList<>();
            String loginValue;
            String passwordValue;


            while (rs.next()) {
                users.add(new User(
                        rs.getString("login"),
                        rs.getString("password")
                ));
            }

            for (int i = 0; i < users.size(); i++) {
                loginValue = users.get(i).getLogin();
                passwordValue = users.get(i).getPassword();
                if ((login.getText().equals(loginValue) &&
                        password.getText().equals(passwordValue))) {
                    isGrant = true;
                    break;
                } else if (login.getText().isEmpty() | password.getText().isEmpty()) {
                    login.setStyle("-fx-border-color: #cb4154;");
                    password.setStyle("-fx-border-color: #cb4154;");
                    label.setText("Please enter login and password!");
                    isGrant = false;

                } else if (!login.getText().equals(loginValue)) {
                    login.setStyle("-fx-border-color: #cb4154;");
                    password.setStyle("-fx-border-color: grey;");
                    label.setText("Login incorrect!");
                    isGrant = false;

                } else if (!password.getText().equals(passwordValue)) {
                    login.setStyle("-fx-border-color: grey;");
                    password.setStyle("-fx-border-color: #cb4154;");
                    label.setText("Password incorrect!");
                    isGrant = false;

                } else if (!login.getText().equals(loginValue) &&
                        !password.getText().equals(passwordValue)) {
                    login.setStyle("-fx-border-color: #cb4154;");
                    password.setStyle("-fx-border-color: #cb4154;");
                    label.setText("User is not registered!");
                    isGrant = false;
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        return isGrant;
    }

    public static void clearFields(TextField loginField, PasswordField passwordField){
        loginField.clear();
        passwordField.clear();
    }

    public static boolean validateWebname(String webnameValue) {
        if (webnameValue.length() < 1 | webnameValue.length() > 40) {
            AlertDialogs.fieldValidationAlert("Webname length can't be more than 40 symbols!");
            return false;
        }
        return true;
    }

    public static boolean validateUsername(String usernameValue) {
        if (usernameValue.length() < 1 | usernameValue.length() > 20) {
            AlertDialogs.fieldValidationAlert("Username length should be within range of 1-20 symbols!");
            return false;
        }
        return true;
    }

    public static boolean validatePassword(String validPassword) {
        if (validPassword.length() < 6 | validPassword.length() > 30) {
            AlertDialogs.fieldValidationAlert("Password length should be 6-30 symbols!");
            return false;
        } else if (validPassword.contains("\u2022")) {
            AlertDialogs.fieldValidationAlert("Wrong password format!");
            return false;
        } else {
            return true;
        }
    }

    public static boolean validateNotes(String notesValue) {
        if (notesValue.length() > 300) {
            AlertDialogs.fieldValidationAlert("Notes length can't be more than 300 symbols!");
            return false;
        } else {
            return true;
        }
    }

    public static String hidePassword(String password) {
        String maskedPassword = "";

        for (int i = 0; i < password.length(); i++) {
            maskedPassword += '\u2022';
        }
        return maskedPassword;
    }

    public static void clearFields(TextField one, TextField two, TextField three, TextArea four, TextField five) {
        one.clear();
        two.clear();
        three.clear();
        four.clear();
        five.clear();
    }

    public static void populateTable(ObservableList<RowData> data, TableView<RowData> table) {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:C:\\sqlite\\passwordManager.db");
            query = "SELECT * FROM list";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            while (rs.next()) {
                data.add(new RowData(
                        rs.getString("webname"),
                        rs.getString("username"),
                        hidePassword(rs.getString("password")),
                        rs.getString("notes")
                ));
                table.setItems(data);
            }

            pst.close();
            rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void start(Stage primaryStage) {

        primaryStage.setTitle("Password manager");
        primaryStage.getIcons().add(new Image(PasswordManager.class.getResourceAsStream("shield.png")));

        Scene scene1, scene2;
        primaryStage.setResizable(false);

// Scene 1
        BorderPane root = new BorderPane();
        VBox vboxPrim = new VBox();
        vboxPrim.setSpacing(10);
        vboxPrim.setAlignment(Pos.CENTER);

        HBox loginHbox = new HBox();
        loginHbox.setAlignment(Pos.CENTER);
        HBox passwordHbox = new HBox();
        passwordHbox.setAlignment(Pos.CENTER);
        HBox controlHbox = new HBox();
        controlHbox.setAlignment(Pos.CENTER);
        controlHbox.setPadding(new Insets(0, 0, 10, 0));

        Label programName = new Label("Password manager");
        programName.setId("passwordManagerLabel");
        programName.setPadding(new Insets(10, 10, 10, 10));

        Label alertMessage = new Label("");
        alertMessage.setTextFill(Color.FIREBRICK);

        root.setTop(programName);
        root.setCenter(vboxPrim);

        scene1 = new Scene(root, 300, 200);
        primaryStage.setScene(scene1);
        scene1.getStylesheets().add("pmStyling.css");

        primaryStage.show();

        TextField loginField = new TextField();
        loginField.setPromptText("Username");
        loginField.setPrefWidth(112);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

// Create User Button
        Button createUser = new Button("", new ImageView(new Image("createUser.png")));
        createUser.setOnAction((ActionEvent e) -> {
            alertMessage.setText("");
            clearFields(loginField, passwordField);
            loginField.setStyle("-fx-border-color: grey;");
            passwordField.setStyle("-fx-border-color: grey;");
            User.addUser();
        });

        Button signInButton = new Button("Log in");
        Hyperlink forgot = new Hyperlink("Forgot password?");

        loginHbox.getChildren().addAll(loginField, createUser);
        passwordHbox.getChildren().addAll(passwordField);
        controlHbox.getChildren().addAll(signInButton, forgot);

        vboxPrim.getChildren().addAll(programName, alertMessage, loginHbox, passwordHbox, controlHbox);

        HBox logoutHbox = new HBox(); // Make proper name
        logoutHbox.setAlignment(Pos.TOP_RIGHT);
        logoutHbox.setSpacing(10);

// Scene 2 border pane
        BorderPane rootSec = new BorderPane();
        rootSec.setTop(logoutHbox);

        scene2 = new Scene(rootSec, 935, 500);
        scene2.getStylesheets().add("pmStyling.css");


// Sign in button action
        signInButton.setOnAction((ActionEvent e) -> {

            if (grantAccess(loginField, passwordField, alertMessage)) {
                primaryStage.setScene(scene2);
                primaryStage.setX(20);
                primaryStage.setY(40);
                alertMessage.setText("");
                loginField.setStyle("-fx-border-color: grey;");
                passwordField.setStyle("-fx-border-color: grey;");
            }
            clearFields(loginField, passwordField);
        });
        signInButton.defaultButtonProperty().bind(signInButton.focusedProperty());

// ENTER key action
        passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    if (grantAccess(loginField, passwordField, alertMessage)) {
                        primaryStage.setScene(scene2);
                        primaryStage.setX(20);
                        primaryStage.setY(40);
                        alertMessage.setText("");
                        loginField.setStyle("-fx-border-color: grey;");
                        passwordField.setStyle("-fx-border-color: grey;");
                    }
                    clearFields(loginField, passwordField);
                }
            }
        });

// Scene 2

        Button logOut = new Button("Logout");
        logoutHbox.getChildren().add(logOut);
        BorderPane.setMargin(logoutHbox, new Insets(15, 15, 10, 10));

// Tableview

        TableView<RowData> table = new TableView<>();
        table.setDisable(true);
        final ObservableList<RowData> data = FXCollections.observableArrayList();
        table.setEditable(false);

        VBox tableAndPassGenBox = new VBox();
        tableAndPassGenBox.setSpacing(10);
        tableAndPassGenBox.setPadding(new Insets(10, 15, 0, 10));

// Password Generator
        HBox passGenBox = new HBox();
        passGenBox.setSpacing(10);
        passGenBox.setAlignment(Pos.BASELINE_LEFT);

        TextField generatedPassword = new TextField();
        generatedPassword.setPromptText("New password");
        generatedPassword.setPrefWidth(150);

        Button generateButton = new Button("Generate password");
        generateButton.setPrefWidth(120);

        Button clearGeneratedPassFld = new Button("", new ImageView(new Image("copyPass.png")));
        clearGeneratedPassFld.setPrefWidth(10);

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        clearGeneratedPassFld.setOnAction((ActionEvent e) -> {
            content.putString(generatedPassword.getText());
            clipboard.setContent(content);
        });

// Table
        TableColumn webCol = new TableColumn("Web name");
        TableColumn userCol = new TableColumn("Username");
        TableColumn passCol = new TableColumn("Password");
        TableColumn notesCol = new TableColumn("Notes");

        webCol.setPrefWidth(175);
        userCol.setPrefWidth(130);
        passCol.setPrefWidth(135);
        notesCol.setPrefWidth(255);

        webCol.setCellValueFactory(new PropertyValueFactory<>("webName"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        passCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        table.getColumns().addAll(webCol, userCol, passCol, notesCol);
        passGenBox.getChildren().addAll(clearGeneratedPassFld, generatedPassword, generateButton);
        tableAndPassGenBox.getChildren().addAll(table, passGenBox);
        rootSec.setCenter(tableAndPassGenBox);

// Left control pane
        Label addRow = new Label("Add a new row");
        addRow.setFont(Font.font("Roboto-Light", 18));

        VBox vb = new VBox();
        vb.setAlignment(Pos.TOP_LEFT);
        vb.setPrefHeight(400);
        vb.setSpacing(10);
        rootSec.setLeft(vb);
        BorderPane.setMargin(vb, new Insets(10, 10, 10, 10));

        TextField addWebFld = new TextField();
        TextField addUserFld = new TextField();
        TextField addPassFld = new TextField();
        TextArea addNotesFld = new TextArea();

        addWebFld.setPromptText("URL");
        addUserFld.setPromptText("Username");
        addPassFld.setPromptText("Password");
        addNotesFld.setPromptText("Notes");

        addPassFld.setPrefWidth(125);
        addNotesFld.setPrefWidth(150);
        addNotesFld.setPrefHeight(40);
        addNotesFld.setWrapText(true);

        HBox commandBtns = new HBox();
        commandBtns.setSpacing(5);

        HBox pass = new HBox();
        pass.setSpacing(5);

        ToggleButton showHidePassSec = new ToggleButton("", new ImageView(new Image("showPassword.png")));
        Tooltip showHidePassTTipSec = new Tooltip("Show Password");
        showHidePassSec.setTooltip(showHidePassTTipSec); // Make appear faster

        Button addButton = new Button("", new ImageView(new Image("add.png")));
        Button updateButton = new Button("", new ImageView(new Image("update.png")));
        Button deleteButton = new Button("", new ImageView(new Image("delete.png")));
        Button clearButton = new Button("", new ImageView(new Image("clear.png")));

        addButton.setPrefWidth(20);
        updateButton.setPrefWidth(20);
        deleteButton.setPrefWidth(20);
        clearButton.setPrefWidth(20);

        addButton.setDisable(true);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        clearButton.setDisable(true);

        pass.getChildren().addAll(addPassFld, showHidePassSec);
        commandBtns.getChildren().addAll(addButton, updateButton, deleteButton, clearButton);


        vb.getChildren().addAll(addRow, addWebFld, addUserFld, pass, addNotesFld, commandBtns);

        table.setOnMouseClicked((MouseEvent e) -> {
                    updateButton.setDisable(false);
                    deleteButton.setDisable(false);
                    clearButton.setDisable(false);

                    if (table.getItems() != null) {
                        try {
                            RowData rowData = (RowData) table.getSelectionModel().getSelectedItem();
                            query = "SELECT * FROM list WHERE webname = ?";
                            pst = conn.prepareStatement(query);
                            pst.setString(1, rowData.getWebName());
                            rs = pst.executeQuery();

                            while (rs.next()) {
                                addWebFld.setText(rs.getString("webname"));
                                addUserFld.setText(rs.getString("username"));
                                addPassFld.setText(rs.getString("password"));
                                addNotesFld.setText(rs.getString("notes"));
                            }

// Show/hide password toggle button
                            String passwordToHide = addPassFld.getText();
                            addPassFld.setText(hidePassword(passwordToHide));

                            showHidePassSec.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                                    if (showHidePassSec.isSelected()) {
                                        addPassFld.setText(passwordToHide);

                                    } else {
                                        addPassFld.setText(hidePassword(passwordToHide));
                                    }
                                }
                            });
                            pst.close();
                            rs.close();

                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    } else {
                        return;
                    }
                }
        );

// Add button
        addButton.setOnAction((ActionEvent e) -> {

            if (addWebFld.getText().isEmpty() |
                    addUserFld.getText().isEmpty() | addPassFld.getText().isEmpty()) {

                AlertDialogs.fieldValidationAlert("Webname, username and password should contain data!");
            } else if (!validateWebname(addWebFld.getText()) |
                    !validateUsername(addUserFld.getText()) | !validatePassword(addPassFld.getText()) |
                    !validateNotes(addNotesFld.getText())) {

            } else {
                try {
                    query = "INSERT INTO list (webname, username, password, notes) VALUES(?,?,?,?)";
                    conn = DriverManager.getConnection("jdbc:sqlite:C:\\sqlite\\passwordManager.db");
                    pst = conn.prepareStatement(query);

                    pst.setString(1, addWebFld.getText());
                    pst.setString(2, addUserFld.getText());
                    pst.setString(3, addPassFld.getText());
                    pst.setString(4, addNotesFld.getText());

                    pst.execute();
                    pst.close();

                    clearFields(addWebFld, addUserFld, addPassFld, addNotesFld, generatedPassword);

                    table.getItems().clear();

                    populateTable(data, table);

                } catch (Exception e3) {
                    e3.printStackTrace();
                }
                AlertDialogs.rowChangingAlert("Row has been added.");
            }
        });

// Update button
        updateButton.setOnAction(e -> {
            if (addWebFld.getText().isEmpty() |
                    addUserFld.getText().isEmpty() | addPassFld.getText().isEmpty()) {
                AlertDialogs.fieldValidationAlert("Webname, username and password should contain data!");
            } else if (!validatePassword(addPassFld.getText())) {
            } else {
                try {
                    Class.forName("org.sqlite.JDBC");
                    conn = DriverManager.getConnection("jdbc:sqlite:C:\\sqlite\\passwordManager.db");
                    query = "UPDATE list SET webname = ?, username = ?, " +
                            "password = ?, notes = ? WHERE webname = '" + addWebFld.getText() + "'";
                    pst = conn.prepareStatement(query);

                    pst.setString(1, addWebFld.getText());
                    pst.setString(2, addUserFld.getText());
                    pst.setString(3, addPassFld.getText());
                    pst.setString(4, addNotesFld.getText());

                    AlertDialogs.rowChangingAlert("Row has been updated.");

                    pst.execute();
                    pst.close();

                    clearFields(addWebFld, addUserFld, addPassFld, addNotesFld, generatedPassword);

                    table.getItems().clear();
                    populateTable(data, table);

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

// Delete button
        deleteButton.setOnAction(e -> {

            Alert alertConfirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
            alertConfirmDelete.setTitle("Confirm delete");
            alertConfirmDelete.setHeaderText(null);
            alertConfirmDelete.setContentText("Are you sure you want to delete the row?");
            Optional<ButtonType> action = alertConfirmDelete.showAndWait();

            if (action.get() == ButtonType.OK) {
                RowData selectedItem = table.getSelectionModel().getSelectedItem();
                table.getItems().remove(selectedItem);

                try {
                    query = "DELETE FROM list WHERE webname = ?";
                    pst = conn.prepareStatement(query);
                    pst.setString(1, selectedItem.getWebName());
                    pst.executeUpdate();
                    pst.close();

                    clearFields(addWebFld, addUserFld, addPassFld, addNotesFld, generatedPassword);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                AlertDialogs.rowChangingAlert("Row has been deleted.");
            }
        });

// Clear button
        clearButton.setOnAction((ActionEvent e) ->
                clearFields(addWebFld, addUserFld, addPassFld, addNotesFld, generatedPassword)
        );

// Load Table button
        Button load = new Button("Load Table");
        load.setOnAction((ActionEvent e) -> {
            table.setDisable(false);
            addButton.setDisable(false);
            table.getItems().clear();

            populateTable(data, table);
        });

// Generate Password
        generateButton.setOnAction((ActionEvent e) -> {
            generatedPassword.clear();
            generatedPassword.setText(PasswordGenerator.generatePassword());

        });

// LogOut Button

        logOut.setOnAction((ActionEvent e) -> {
            addButton.setDisable(true);
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
            clearButton.setDisable(true);
            table.getItems().clear();
            clearFields(addWebFld, addUserFld, addPassFld, addNotesFld, generatedPassword);
            primaryStage.setScene(scene1);
        });

        HBox hb = new HBox();
        hb.getChildren().add(load);
        rootSec.setBottom(hb);
        BorderPane.setMargin(hb, new Insets(10, 0, 10, 10));
    }


}
*/
