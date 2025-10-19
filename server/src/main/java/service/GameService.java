package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.UUID;

public class GameService {
    AuthDAO authDAO;
    GameDAO gameDAO;

    public GameService() {
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
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
