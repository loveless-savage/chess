package service;

import dataaccess.*;
import model.*;

import java.util.UUID;

public class UserService {
    final UserDAO userDAO;
    final AuthDAO authDAO;

    public UserService() {
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
    }
    public UserService(UserDAO userIn, AuthDAO authIn) {
        userDAO = userIn;
        authDAO = authIn;
    }

    public AuthData register(UserData newUser) {
        if(newUser.username() == null || newUser.password() == null || newUser.email() == null) {
            throw new BadRequestException("bad request");
        }
        if(userDAO.get(newUser.username()) != null) {
            throw new AlreadyTakenException("username already taken");
        }
        userDAO.create(newUser);
        AuthData loginInfo = new AuthData(generateToken(), newUser.username());
        authDAO.create(loginInfo);
        return loginInfo;
    }

    public AuthData login(LoginRequest loginRequest) {
        if(loginRequest.username() == null || loginRequest.password() == null) {
            throw new BadRequestException("bad request");
        }
        var userRetrieved = userDAO.get(loginRequest.username());
        if(userRetrieved == null ||
                    !userRetrieved.password().equals(loginRequest.password())) {
            throw new UnauthorizedException("unauthorized");
        }
        AuthData loginInfo = new AuthData(generateToken(), loginRequest.username());
        authDAO.create(loginInfo);
        return loginInfo;
    }

    public void logout(String authToken) {
        if(authDAO.get(authToken) == null) {
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
