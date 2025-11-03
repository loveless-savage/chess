package dataaccess;

import model.*;
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

    String toSQL(UserData data) throws DataAccessException {
        if (data.username() == null || data.password() == null || data.email() == null) {
            throw new DataAccessException("no UserData fields can be null");
        } else if (data.password().length() != 60) {
            throw new DataAccessException("The provided password was not properly encrypted");
        } else {
            return "('" + data.username() + "','" + data.password() + "','" + data.email() + "')";
        }
    }
    String toSQLDiff(UserData data, UserData dataOld) throws DataAccessException {
        if (data.username() == null || data.password() == null || data.email() == null) {
            throw new DataAccessException("no UserData fields can be null");
        }
        String out = "username = '" + data.username() + "'";
        if (!data.password().equals(dataOld.password())) {
            out += ", password = '" + data.password() + "'";
        }
        if (!data.email().equals(dataOld.email())) {
            out += ", email = '" + data.email() + "'";
        }
        return out;
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
