package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import java.util.UUID;

public class GameServiceTests {
    private static GameService gameService;
    private static AuthDAO authDAO;
    private static AuthData currentAuth;
    private static GameDAO gameDAO;
    private static GameData firstGame, otherGame, takenGame;
    private int firstID, otherID;

    @BeforeAll
    public static void init(){
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        gameService = new GameService(authDAO, gameDAO);
        currentAuth = new AuthData(UUID.randomUUID().toString(),"currentUser");

        firstGame = new GameData(1,null,null,"firstGame",new ChessGame());
        otherGame = new GameData(2,null,null,"otherGame",new ChessGame());
        takenGame = new GameData(3,"enemyWhite","enemyBlack","takenGame",new ChessGame());
    }
    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO.create(currentAuth);
        gameDAO.create(firstGame);
        firstID = gameDAO.getLastID();
    }
    @AfterEach
    public void takeDown() throws DataAccessException {
        gameService.clear();
    }

    @Test
    public void createGameTest() throws DataAccessException {
        gameDAO.clear();
        firstID = gameService.createGame(currentAuth.authToken(),new GameCreateRequest("firstGame"));
        Assertions.assertEquals(gameDAO.getLastID(), firstID);
        otherID = gameService.createGame(currentAuth.authToken(),new GameCreateRequest("otherGame"));
        Assertions.assertEquals(gameDAO.getLastID(), otherID);
        Assertions.assertEquals(updateID(firstGame,firstID), gameDAO.get(firstID));
        Assertions.assertEquals(updateID(otherGame,otherID), gameDAO.get(otherID));
    }
    @Test
    public void createGameUnauthorizedTest() {
        Assertions.assertThrows(UnauthorizedException.class,() ->
                gameService.createGame(UUID.randomUUID().toString(),new GameCreateRequest("otherGame")));
    }

    @Test
    public void listGamesTest() throws DataAccessException {
        Assertions.assertArrayEquals(new GameData[]{updateID(firstGame,firstID)}, gameService.listGames(currentAuth.authToken()));
        otherID = gameService.createGame(currentAuth.authToken(),new GameCreateRequest("otherGame"));
        Assertions.assertArrayEquals(new GameData[]{updateID(firstGame,firstID),updateID(otherGame,otherID)}, gameService.listGames(currentAuth.authToken()));
        gameService.clear();
        authDAO.create(currentAuth);
        Assertions.assertArrayEquals(new GameData[]{}, gameService.listGames(currentAuth.authToken()));
    }
    @Test
    public void listGamesUnauthorizedTest() {
        Assertions.assertThrows(UnauthorizedException.class,() -> gameService.listGames("wrongToken"));
    }

    @Test
    public void joinGameAsWhiteTest() throws DataAccessException {
        GameJoinRequest joinRequest = new GameJoinRequest(ChessGame.TeamColor.WHITE, firstID);
        gameService.joinGame(currentAuth.authToken(), joinRequest);
        Assertions.assertEquals(currentAuth.username(), gameDAO.get(firstID).whiteUsername());
    }
    @Test
    public void joinGameAsBlackTest() throws DataAccessException {
        GameJoinRequest joinRequest = new GameJoinRequest(ChessGame.TeamColor.BLACK, firstID);
        gameService.joinGame(currentAuth.authToken(), joinRequest);
        Assertions.assertEquals(currentAuth.username(), gameDAO.get(firstID).blackUsername());
    }
    @Test
    public void joinGameUnauthorizedTest() {
        GameJoinRequest joinRequest = new GameJoinRequest(ChessGame.TeamColor.WHITE, firstID);
        Assertions.assertThrows(UnauthorizedException.class,() ->
                gameService.joinGame("wrongToken", joinRequest)
        );
    }
    @Test
    public void joinGameAsWhiteAlreadyTakenTest() throws DataAccessException {
        gameDAO.create(takenGame);
        gameDAO.update(updateID(takenGame,gameDAO.getLastID()));
        GameJoinRequest joinRequest = new GameJoinRequest(ChessGame.TeamColor.WHITE, gameDAO.getLastID());
        Assertions.assertThrows(AlreadyTakenException.class,() ->
                gameService.joinGame(currentAuth.authToken(), joinRequest)
        );
    }
    @Test
    public void joinGameAsBlackAlreadyTakenTest() throws DataAccessException {
        gameDAO.create(takenGame);
        gameDAO.update(updateID(takenGame,gameDAO.getLastID()));
        GameJoinRequest joinRequest = new GameJoinRequest(ChessGame.TeamColor.BLACK, gameDAO.getLastID());
        Assertions.assertThrows(AlreadyTakenException.class,() ->
                gameService.joinGame(currentAuth.authToken(), joinRequest)
        );
    }

    @Test
    public void clearTest() throws DataAccessException {
        gameDAO.create(otherGame);
        gameService.clear();
        Assertions.assertArrayEquals(new GameData[]{}, gameDAO.list());
    }

    private GameData updateID(GameData data,int newID) {
        return new GameData(newID,data.whiteUsername(), data.blackUsername(), data.gameName(),data.game());
    }
}
