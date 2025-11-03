package dataaccess;

import model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthDAO extends MySQLDAO<AuthData,String>{
    public AuthDAO() {
        super("authData", """
                             authToken CHAR(36) NOT NULL,
                             username VARCHAR(32) NOT NULL,
                             PRIMARY KEY (authToken)
                             """);
    }

    PreparedStatement toSQL(Connection conn, AuthData data) throws DataAccessException {
        if (data.authToken() == null || data.authToken().length() != 36) {
            throw new DataAccessException("The authToken '" + data.authToken() + "' was not properly generated");
        } else if (data.username() == null) {
            throw new DataAccessException("username cannot be null");
        }
        try {
            PreparedStatement out = conn.prepareStatement("INSERT INTO authData values (?,?)");
            out.setString(1, data.authToken());
            out.setString(2, data.username());
            return out;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(),e);
        }
    }

    PreparedStatement toSQLDiff(Connection conn, AuthData data) throws DataAccessException {
        if (data.authToken() == null || data.username() == null) {
            throw new DataAccessException("no AuthData fields can be null");
        }
        try {
            PreparedStatement out = conn.prepareStatement(
                    "UPDATE authData SET authToken=?,username=? WHERE authToken = ?");
            out.setString(1, data.authToken());
            out.setString(2, data.username());
            out.setString(3, data.keyValue());
            return out;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    AuthData fromSQL(ResultSet rs) throws SQLException {
        if(rs.next()) {
            return new AuthData(
                    rs.getString("authToken"),
                    rs.getString("username")
            );
        } else {
            return null;
        }
    }
}
