package password.manager.utils;

public class Validator {

    //TODO unify names
    private static final String URL_BAD_LENGTH = "URL value should be between 3 and 60 characters!";
    private static final String NUM_LGN = "Login should contain at least 1 letter!";
    private static final String LGN_BAD_LENGTH = "Login field should be between 5 and 30 characters!";
    private static final String PWD_BAD_LENGTH = "Password field should be between 5 and 30 characters!";
    private static final String LONG_NOTES = "Notes field can't be longer than 250 characters!";
    private static final String WRONG_PWD_FORMAT = "Wrong password format!";


    public static boolean validateCredentials(String login, String password) {
        return login.length() >= 5 && login.length() <= 40 &&
                password.length() >= 5 && password.length() <= 40;
    }

    //TODO split to several methods like 'validateUrl'/'validateLogin' and invoke from here
    public static boolean validateEntryFields(String url, String login,
                                              String password, String notes) {

        if (password.contains("\u2022")) {
            PopupUtils.fieldValidationAlert(WRONG_PWD_FORMAT);
            return false;
        }

        if (url.length() < 3 || url.length() > 60) {
            PopupUtils.fieldValidationAlert(URL_BAD_LENGTH);
            return false;
        } else if (login.chars().allMatch(Character::isDigit)) {
            PopupUtils.fieldValidationAlert(NUM_LGN);
            return false;
        } else if (login.length() < 5 || login.length() > 40) {
            PopupUtils.fieldValidationAlert(LGN_BAD_LENGTH);
            return false;
        } else if (password.length() < 5 || password.length() > 40) {
            PopupUtils.fieldValidationAlert(PWD_BAD_LENGTH);
            return false;
        } else if (notes.length() > 250) {
            PopupUtils.fieldValidationAlert(LONG_NOTES);
            return false;
        } else {
            return true;
        }
    }
}