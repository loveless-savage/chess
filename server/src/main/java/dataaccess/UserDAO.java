package dataaccess;

import model.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends MySQLDAO<UserData,String>{
    public UserDAO() {
        super("userData","username", """
                            username VARCHAR(32) NOT NULL,
                            password VARCHAR(64) NOT NULL,
                            email VARCHAR(32) NOT NULL,
                            PRIMARY KEY (username)
                            """);
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
