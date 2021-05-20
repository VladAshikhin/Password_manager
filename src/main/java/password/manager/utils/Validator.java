package password.manager.utils;

import password.manager.entity.Data;

public class Validator {

    //TODO unify names
    private static final String URL_BAD_LENGTH = "URL value should be between 3 and 60 characters!";
    private static final String NUM_LGN = "Login should contain at least 1 letter!";
    private static final String LGN_BAD_LENGTH = "Login field should be between 5 and 30 characters!";
    private static final String PWD_BAD_LENGTH = "Password field should be between 5 and 30 characters!";
    private static final String LONG_NOTES = "Notes field can't be longer than 250 characters!";
    private static final String WRONG_PWD_FORMAT = "Wrong password format!";


    public static boolean validateCredentials(String login, String password) {

        if (login.length() < 5 || login.length() > 40) {

        }

        return login.length() >= 5 && login.length() <= 40 &&
                password.length() >= 5 && password.length() <= 40;
    }

    //TODO split to several methods like 'validateUrl'/'validateLogin' and invoke from here
    public static boolean validateEntryFields(Data entry) {

        if (entry.getPassword().contains("\u2022")) {
            PopupUtils.showWarningDialog(WRONG_PWD_FORMAT);
            return false;
        }

        if (entry.getUrl().length() < 3 || entry.getUrl().length() > 60) {
            PopupUtils.showWarningDialog(URL_BAD_LENGTH);
            return false;
        } else if (entry.getLogin().chars().allMatch(Character::isDigit)) {
            PopupUtils.showWarningDialog(NUM_LGN);
            return false;
        } else if (entry.getLogin().length() < 5 || entry.getLogin().length() > 40) {
            PopupUtils.showWarningDialog(LGN_BAD_LENGTH);
            return false;
        } else if (entry.getPassword().length() < 5 || entry.getPassword().length() > 40) {
            PopupUtils.showWarningDialog(PWD_BAD_LENGTH);
            return false;
        } else if (entry.getNotes().length() > 250) {
            PopupUtils.showWarningDialog(LONG_NOTES);
            return false;
        } else {
            return true;
        }
    }
}