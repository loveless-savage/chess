package service;

import dataaccess.*;
import model.*;

public class Service {
    DAO<AuthData> authDAO;
    DAO<GameData> gameDAO;
    DAO<UserData> userDAO;

    public Service() {
        authDAO = new MemoryDAO<>();
        gameDAO = new MemoryDAO<>();
        userDAO = new MemoryDAO<>();
    }

    public AuthData register(UserData newUser) { // TODO: result instead of AuthData
        userDAO.create(newUser);
        return new AuthData(newUser.username(),"fillerauth");
    }
}
