package password.manager.accessors;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import password.manager.entity.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoginDataAccessor {

    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/kstore?serverTimezone=UTC";
    private static final String USERNAME = "vashikhi";
    private static final String PASSWORD = "Maxtomcat91!";

    private static final String SELECT_ALL_USERS = "SELECT * FROM user";
    private static final String INSERT_USER = "INSERT INTO user (username, password, active) VALUES (?, ?, ?)";

    private Connection connection;
    private PreparedStatement pst;

    private Logger logger = Logger.getLogger("Log");
    private Appender fh;

    public LoginDataAccessor() throws SQLException, ClassNotFoundException {
        Class.forName(DB_DRIVER);
        connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }

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


    public List<User> getUserList() throws SQLException {
        List<User> users = new ArrayList<>();

        pst = connection.prepareStatement(SELECT_ALL_USERS);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getBoolean("active")
            ));
        }
        return users;
    }

    public void insertUser(String login, String password) throws SQLException {
        pst = connection.prepareStatement(INSERT_USER);

        pst.setString(1, login);
        pst.setString(2, password);
        pst.setBoolean(3, true);
        pst.execute();
        pst.close();
    }

    private void shutdown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        pst.close();
    }

}
