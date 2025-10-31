package dataaccess;

import model.*;

public class UserDAO extends MySQLDAO<UserData,String>{
    public UserDAO() {
        super("userData","""
                            username VARCHAR(32) NOT NULL,
                            password VARCHAR(64) NOT NULL,
                            email VARCHAR(32) NOT NULL,
                            PRIMARY KEY (username)
                            """);
    }
}
