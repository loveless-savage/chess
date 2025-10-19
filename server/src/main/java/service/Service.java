package service;

import chess.*;
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
    public AuthData login(AuthData usernameAndPassword) {
        return usernameAndPassword;
    }
    public void logout(String authToken) {
        return;
    }
    public GameData[] listGames(String authToken) {
        return null;
    }
    public int createGame(String authToken, String gameName) {
        return 0;
    }
    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {
        return;
    }
    public void clear() {
        return;
    }
}
