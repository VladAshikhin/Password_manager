package password.manager.utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PopupUtils {

    private static final String INFORMATION = "INFORMATION";
    private static final String CONFIRMATION = "CONFIRMATION";
    private static final String WARNING = "WARNING";
    private static final String ERROR = "ERROR";

    public static void showInformationDialog(String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(INFORMATION);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static Optional<ButtonType> showConfirmationDialog(String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(CONFIRMATION);
        alert.setHeaderText(CONFIRMATION);
        alert.setContentText(contentText);
        return alert.showAndWait();
    }

    static void showWarningDialog(String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(WARNING);
        alert.setHeaderText(WARNING);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static void showErrorDialog(String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(ERROR);
        alert.setHeaderText(ERROR);
        alert.setContentText(error);
        alert.showAndWait();
    }

    /**
     * Dialog for creating a new User
     *
     * @return Map of String, Object where
     * action is a type of button clicked - "OK" or "Cancel"
     * login is a new login value
     * password is a new password value
     */
    public static Map<String, Object> addUser() {
        Map<String, Object> credentials = new HashMap<>();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(new ImageView(new Image("icons/newuser.png")));
        alert.setTitle("Adding new user");
        alert.setHeaderText("Enter login and password for the new user");

        Label userLabel = new Label("Username: ");
        Label passLabel = new Label("Password: ");
        Label message = new Label("");
        TextField userTextField = new TextField();
        userTextField.setPromptText("Username");
        PasswordField passTextField = new PasswordField();
        passTextField.setPromptText("Password");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.setVgap(5);
        grid.setHgap(5);

        grid.add(message, 1, 0);
        grid.add(userLabel, 1, 1);
        grid.add(passLabel, 2, 1);
        grid.add(userTextField, 1, 2);
        grid.add(passTextField, 2, 2);

        alert.getDialogPane().setContent(grid);

        Optional<ButtonType> action = alert.showAndWait();

        String newUsername = userTextField.getText();
        String newPassword = passTextField.getText();
        boolean isValid = Validator.validateCredentials(newUsername, newPassword);

        if (action.get() == ButtonType.OK) {
            System.out.println("OK");

            credentials.put("login", newUsername);
            credentials.put("password", newPassword);
            credentials.put("type", action);
            credentials.put("valid", isValid);
            return credentials;
        }
        return credentials;
    }
}