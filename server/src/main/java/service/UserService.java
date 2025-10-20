package service;

import chess.*;
import dataaccess.*;
import model.*;

import java.util.Objects;
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

    public AuthData register(UserData newUser) throws AlreadyTakenException {
        if(userDAO.get(newUser.username()) != null) { // TODO: AlreadyTakenException [403]
            throw new AlreadyTakenException("already taken");
        }
        userDAO.create(newUser);
        AuthData loginInfo = new AuthData(generateToken(), newUser.username());
        authDAO.create(loginInfo);
        return loginInfo;
    }

    public AuthData login(LoginRequest loginRequest) {
        var userRetrieved = userDAO.get(loginRequest.username());
        if(userRetrieved == null) { // TODO: NotFoundException [404]
            throw new NotFoundException("username not found");
        }
        if(!userRetrieved.password().equals(loginRequest.password())) { // TODO: UnauthorizedException [401]
            throw new UnauthorizedException("unauthorized");
        }
        return new AuthData(generateToken(), loginRequest.username());
    }

    public void logout(String authToken) {
        if(authDAO.get(authToken) == null) { // TODO: UnauthorizedException [401]
            throw new UnauthorizedException("unauthorized");
        }
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
