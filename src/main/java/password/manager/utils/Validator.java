package password.manager.utils;

public class Validator {

    private static final String URL_BAD_LENGTH = "URL value should be between 3 and 60 characters!";
    private static final String NUM_LGN = "Login should contain at least 1 letter!";
    private static final String LGN_BAD_LENGTH = "Login field should be between 5 and 30 characters!";
    private static final String PWD_BAD_LENGTH = "Password field should be between 5 and 30 characters!";
    private static final String LONG_NTS = "Notes field can't be longer than 250 characters!";
    private static final String WRONG_PWD_FORMAT = "Wrong password format!";


    public static boolean validateCredentials(String login, String password) {
        if (login.length() < 5 || login.length() > 40 ||
                password.length() < 5 || password.length() > 40) {
            return false;
        }
        return true;
    }

    public static boolean validateEntryFields(String url, String login,
                                              String password, String notes) {
        boolean isValid = true;
        if (url.length() < 3 || url.length() > 60) {
            PopUp.fieldValidationAlert(URL_BAD_LENGTH);
            isValid = false;
        } else if (login.chars().allMatch( Character::isDigit )) {
            PopUp.fieldValidationAlert(NUM_LGN);
            isValid = false;
        } else if (login.length() < 5 || login.length() > 40) {
            PopUp.fieldValidationAlert(LGN_BAD_LENGTH);
            isValid = false;
        } else if (password.length() < 5 || password.length() > 40) {
            PopUp.fieldValidationAlert(PWD_BAD_LENGTH);
            isValid = false;
        } else if (notes.length() > 250) {
            PopUp.fieldValidationAlert(LONG_NTS);
            isValid = false;
        }

        if (password.contains("\u2022")) {
            PopUp.fieldValidationAlert(WRONG_PWD_FORMAT);
            isValid = false;
        }
        return isValid;
    }
}