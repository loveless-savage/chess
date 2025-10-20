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
        authDAO.get(authToken); // FIXME: UnauthorizedException [401]
        return gameDAO.list();
    }
    public int createGame(String authToken, String gameName) {
        authDAO.get(authToken); // FIXME: UnauthorizedException [401]
        int newGameID = gameDAO.getNextID();
        GameData newGame = new GameData(newGameID,"","",gameName,new ChessGame());
        gameDAO.create(newGame);
        return newGameID;
    }
    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) {
        AuthData userAuth = authDAO.get(authToken); // FIXME: UnauthorizedException [401]
        GameData currentGame = gameDAO.get(gameID); // FIXME: NotFoundException [404]
        GameData updatedGame;
        if(playerColor == ChessGame.TeamColor.WHITE) {
            // TODO: AlreadyTakenException [403]
            updatedGame = new GameData(currentGame.gameID(),
                                       userAuth.username(), currentGame.blackUsername(),
                                       currentGame.gameName(),
                                       currentGame.game());
        } else {
            updatedGame = new GameData(currentGame.gameID(),
                                       currentGame.whiteUsername(), userAuth.username(),
                                       currentGame.gameName(),
                                       currentGame.game());
        }
        gameDAO.update(updatedGame);
    }
    public void clear() {
        authDAO.clear();
        gameDAO.clear();
    }
}
