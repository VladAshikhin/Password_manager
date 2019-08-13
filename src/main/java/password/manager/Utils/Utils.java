package password.manager.Utils;

import java.security.SecureRandom;
import java.util.Random;

public class Utils {

    public static final Random random = new SecureRandom();

    public static String generatePassword() {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String result = "";
        int length = 10;
        for (int i = 0; i < length; i++) {
            int index = (int) (random.nextDouble() * characters.length());
            result += characters.substring(index, index + 1);
        }
        return result;
    }

    public static String hidePassword(String password) {
        String maskedPassword = "";

        for (int i = 0; i < password.length(); i++) {
            maskedPassword += '\u2022';
        }
        return maskedPassword;
    }
}