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
    private static final String INSERT_INTO_DATA = "INSERT INTO data (url, login, password, notes, user_id) VALUES (?, ?, ?, ?, ?)";
    private static final String GET_ID = "SELECT id FROM data WHERE url = ? and login = ?";

    private static final String DELETE_BY_ID = "DELETE FROM data WHERE id = ?";


    private final Connection connection;
    private PreparedStatement pst;
    private ResultSet rs;

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

    public Data addEntry(Data newEntry) throws SQLException {

        pst = connection.prepareStatement(INSERT_INTO_DATA);

        pst.setString(1, newEntry.getUrl());
        pst.setString(2, newEntry.getLogin());
        pst.setString(3, newEntry.getPassword());
        pst.setString(4, newEntry.getNotes());
        pst.setInt(5, newEntry.getUserId());

        pst = connection.prepareStatement(GET_ID);
        pst.setString(1, newEntry.getUrl());
        pst.setString(2, newEntry.getLogin());

        rs = pst.executeQuery();

        while (rs.next()) {
            newEntry.setId(rs.getInt("id"));
        }
        newEntry.setHiddenPassword(Utils.hidePassword(newEntry.getPassword()));

        pst.execute();
        pst.close();

        return newEntry;
    }

    public void deleteEntry(Data data) throws SQLException {
        pst = connection.prepareStatement(DELETE_BY_ID);

        pst.setInt(1, data.getId());
        pst.executeUpdate();
        pst.close();
    }
}