package service;

import chess.*;
import dataaccess.*;
import model.*;
import java.util.UUID;

public class UserService {
    UserDAO userDAO;
    AuthDAO authDAO;

    public UserService() {
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
    }
    public UserService(UserDAO userIn, AuthDAO authIn) {
        userDAO = userIn;
        authDAO = authIn;
    }

    public AuthData register(UserData newUser) {
        userDAO.get(newUser.username()); // FIXME: AlreadyTakenException [403]
        userDAO.create(newUser);
        AuthData loginInfo = new AuthData(generateToken(), newUser.username());
        authDAO.create(loginInfo);
        return loginInfo;
    }
    public AuthData login(AuthData loginRequest) {
        userDAO.get(loginRequest.username()); // FIXME: NotFoundException [404], UnauthorizedException [401]
        return new AuthData(generateToken(), loginRequest.username());
    }
    public void logout(String authToken) {
        authDAO.get(authToken); // FIXME: UnauthorizedException [401]
        authDAO.delete(authToken);
    }
    public void clear() {
        authDAO.clear();
        userDAO.clear();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
