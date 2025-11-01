package service;

import chess.*;
import dataaccess.*;
import model.*;

public class GameService {
    final AuthDAO authDAO;
    final GameDAO gameDAO;

    public GameService() {
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
    }
    public GameService(AuthDAO authIn, GameDAO gameIn) {
        authDAO = authIn;
        gameDAO = gameIn;
    }

    public GameData[] listGames(String authToken) throws DataAccessException {
        if(authDAO.get(authToken) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        return gameDAO.list();
    }
    public int createGame(String authToken, GameCreateRequest createRequest) throws DataAccessException {
        if(authToken == null || createRequest.gameName() == null) {
            throw new BadRequestException("bad request");
        }
        if(authDAO.get(authToken) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        int newGameID = gameDAO.getNextID();
        GameData newGame = new GameData(newGameID,null,null,createRequest.gameName(),new ChessGame());
        gameDAO.create(newGame);
        return newGameID;
    }
    public void joinGame(String authToken, GameJoinRequest joinRequest) throws DataAccessException {
        AuthData userAuth = authDAO.get(authToken);
        if(userAuth == null) {
            throw new UnauthorizedException("unauthorized");
        }
        GameData currentGame = gameDAO.get(joinRequest.gameID());
        if(currentGame == null) {
            throw new BadRequestException("no game with given ID");
        }
        GameData updatedGame;
        if(joinRequest.playerColor() == ChessGame.TeamColor.WHITE) {
            if(currentGame.whiteUsername() != null) {
                throw new AlreadyTakenException("team already taken");
            }
            updatedGame = new GameData(currentGame.gameID(),
                                       userAuth.username(), currentGame.blackUsername(),
                                       currentGame.gameName(),
                                       currentGame.game());
        } else if(joinRequest.playerColor() == ChessGame.TeamColor.BLACK) {
            if(currentGame.blackUsername() != null) {
                throw new AlreadyTakenException("team already taken");
            }
            updatedGame = new GameData(currentGame.gameID(),
                                       currentGame.whiteUsername(), userAuth.username(),
                                       currentGame.gameName(),
                                       currentGame.game());
        } else {
            throw new BadRequestException("bad team color");
        }
        gameDAO.update(updatedGame);
    }

    public void clear() throws DataAccessException {
        authDAO.clear();
        gameDAO.clear();
    }
}
