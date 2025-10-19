package service;

import chess.*;
import dataaccess.*;
import model.*;
import java.util.UUID;

public class UserService {
    AuthDAO authDAO;
    UserDAO userDAO;

    public UserService() {
        authDAO = new AuthDAO();
        userDAO = new UserDAO();
    }
    public UserService(AuthDAO authIn, UserDAO userIn) {
        authDAO = authIn;
        userDAO = userIn;
    }

    public AuthData register(UserData newUser) { // TODO: result instead of AuthData
        userDAO.create(newUser);
        return new AuthData(newUser.username(),"fillerauth");
    }
    public AuthData login(AuthData usernameAndPassword) {
        return usernameAndPassword;
    }
    public void logout(String authToken) {
        return;
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
