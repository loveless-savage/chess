package dataaccess;

import model.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthDAO extends MySQLDAO<AuthData,String>{
    public AuthDAO() {
        super("authData", """
                             authToken VARCHAR(36) NOT NULL,
                             username VARCHAR(32) NOT NULL,
                             PRIMARY KEY (authToken)
                             """);
    }

    String toSQL(AuthData data) throws DataAccessException {
        if (data.authToken() == null || data.authToken().length() != 36) {
            throw new DataAccessException("The authToken '" + data.authToken() + "' was not properly generated");
        } else if (data.username() == null) {
                throw new DataAccessException("username cannot be null");
        } else {
            return "('" + data.authToken() + "','" + data.username() + "')";
        }
    }
    String toSQLDiff(AuthData data, AuthData dataOld) throws DataAccessException {
        if (data.authToken() == null || data.username() == null) {
            throw new DataAccessException("no AuthData fields can be null");
        }
        String out = "authToken = '" + data.authToken() + "'";
        if (!data.username().equals(dataOld.username())) {
            out += ", username = '" + data.username() + "'";
        }
        return out;
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
