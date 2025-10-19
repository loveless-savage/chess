package service;

import chess.*;
import dataaccess.*;
import model.*;

public class GameService {
    AuthDAO authDAO;
    GameDAO gameDAO;

    public GameService() {
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
    }
    public GameService(AuthDAO authIn, GameDAO gameIn) {
        authDAO = authIn;
        gameDAO = gameIn;
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
