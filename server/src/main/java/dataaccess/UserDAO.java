package dataaccess;

import model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends MySQLDAO<UserData,String>{
    public UserDAO() {
        super("userData", """
                             username VARCHAR(32) NOT NULL,
                             password CHAR(60) NOT NULL,
                             email VARCHAR(32) NOT NULL,
                             PRIMARY KEY (username)
                             """);
    }

    PreparedStatement toSQL(Connection conn, UserData data) throws DataAccessException {
        if (data.username() == null || data.password() == null || data.email() == null) {
            throw new DataAccessException("no UserData fields can be null");
        } else if (data.password().length() != 60) {
            throw new DataAccessException("The provided password was not properly encrypted");
        }
        try {
            PreparedStatement out = conn.prepareStatement("INSERT INTO userData values (?,?,?)");
            out.setString(1, data.username());
            out.setString(2, data.password());
            out.setString(3, data.email());
            return out;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(),e);
        }
    }

    PreparedStatement toSQLDiff(Connection conn, UserData data) throws DataAccessException {
        if (data.username() == null || data.password() == null || data.email() == null) {
            throw new DataAccessException("no UserData fields can be null");
        }
        try {
            PreparedStatement out = conn.prepareStatement(
                    "UPDATE userData SET username=?,password=?,email=? WHERE username = ?");
            out.setString(1, data.username());
            out.setString(2, data.password());
            out.setString(3, data.email());
            out.setString(4, data.keyValue());
            return out;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    UserData fromSQL(ResultSet rs) throws SQLException {
        if(rs.next()) {
            return new UserData(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email")
                );
        } else {
            return null;
        }
    }
}
