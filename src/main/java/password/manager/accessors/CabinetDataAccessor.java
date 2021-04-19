package password.manager.accessors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import password.manager.entity.Data;
import password.manager.utils.Utils;

import java.sql.*;

public class CabinetDataAccessor {

    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/kstore?serverTimezone=UTC";
    private static final String USERNAME = "vashikhi";
    private static final String PASSWORD = "Maxtomcat91!";
    private static final Integer ROOT_USER_ID = 999;

    private static final String FIND_ALL_DATA = "SELECT * FROM data";
    private static final String FIND_ALL_DATA_BY_ID = "SELECT * FROM data WHERE user_id = ?";

    private Connection connection;
    private PreparedStatement pst;
    private static ResultSet rs;

    public CabinetDataAccessor() throws SQLException, ClassNotFoundException {
        Class.forName(DB_DRIVER);
        connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }

    public ObservableList<Data> getAllData(Integer loggedInUser) throws SQLException {
        ObservableList<Data> data = FXCollections.observableArrayList();

        if (loggedInUser.equals(ROOT_USER_ID)) {
            pst = connection.prepareStatement(FIND_ALL_DATA);
        } else {
            pst = connection.prepareStatement(FIND_ALL_DATA_BY_ID);
            pst.setInt(1, loggedInUser);
        }

        rs = pst.executeQuery();

        while (rs.next()) {
            data.add(new Data(
                    rs.getInt("id"),
                    rs.getString("url"),
                    rs.getString("login"),
                    rs.getString("password"),
                    Utils.hidePassword(rs.getString("password")),
                    rs.getString("notes"),
                    rs.getInt("user_id")
            ));
        }

        pst.close();
        rs.close();

        return data;
    }
}