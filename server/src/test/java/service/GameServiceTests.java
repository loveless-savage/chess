package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

public class GameServiceTests {
    private static GameService gameService;
    private static AuthDAO authDAO;
    private static AuthData currentAuth;
    private static GameDAO gameDAO;
    private static GameData firstGame, otherGame, takenGame;

    @BeforeAll
    public static void init(){
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        gameService = new GameService(authDAO, gameDAO);
        currentAuth = new AuthData("fillerAuthToken","currentUser");

        firstGame = new GameData(0,"","","firstGame",new ChessGame());
        otherGame = new GameData(1,"","","otherGame",new ChessGame());
        takenGame = new GameData(2,"enemyWhite","enemyBlack","takenGame",new ChessGame());
    }
    @BeforeEach
    public void setup() {
        authDAO.create(currentAuth);
        gameDAO.create(firstGame);
    }
    @AfterEach
    public void takeDown() {
        gameService.clear();
    }

    @Test
    public void createGameTest() {
        gameDAO.clear();
        Assertions.assertEquals(0, gameService.createGame(currentAuth.authToken(),"firstGame"));
        Assertions.assertEquals(1, gameService.createGame(currentAuth.authToken(),"otherGame"));
        Assertions.assertEquals(firstGame, gameDAO.get(0));
        Assertions.assertEquals(otherGame, gameDAO.get(1));
    }
    @Test
    public void createGameUnauthorizedTest() {
        Assertions.assertThrows(UnauthorizedException.class,() -> gameService.createGame("wrongToken","otherGame"));
    }

    @Test
    public void listGamesTest() {
        Assertions.assertArrayEquals(new GameData[]{firstGame}, gameService.listGames(currentAuth.authToken()));
        gameService.createGame(currentAuth.authToken(),"otherGame");
        Assertions.assertArrayEquals(new GameData[]{firstGame, otherGame}, gameService.listGames(currentAuth.authToken()));
        gameService.clear();
        authDAO.create(currentAuth);
        Assertions.assertArrayEquals(new GameData[]{}, gameService.listGames(currentAuth.authToken()));
    }
    @Test
    public void listGamesUnauthorizedTest() {
        Assertions.assertThrows(UnauthorizedException.class,() -> gameService.listGames("wrongToken"));
    }

    @Test
    public void joinGameAsWhiteTest() {
        gameService.joinGame(currentAuth.authToken(), ChessGame.TeamColor.WHITE, 0);
        Assertions.assertEquals(currentAuth.username(), gameDAO.get(0).whiteUsername());
    }
    @Test
    public void joinGameAsBlackTest() {
        gameService.joinGame(currentAuth.authToken(), ChessGame.TeamColor.BLACK, 0);
        Assertions.assertEquals(currentAuth.username(), gameDAO.get(0).blackUsername());
    }
    @Test
    public void joinGameUnauthorizedTest() {
        Assertions.assertThrows(UnauthorizedException.class,() ->
                gameService.joinGame("wrongToken", ChessGame.TeamColor.WHITE, 0)
        );
    }
    @Test
    public void joinGameAsWhiteAlreadyTakenTest() {
        gameDAO.create(takenGame);
        Assertions.assertThrows(AlreadyTakenException.class,() ->
                gameService.joinGame(currentAuth.authToken(), ChessGame.TeamColor.WHITE, 2)
        );
    }
    @Test
    public void joinGameAsBlackAlreadyTakenTest() {
        gameDAO.create(takenGame);
        Assertions.assertThrows(AlreadyTakenException.class,() ->
                gameService.joinGame(currentAuth.authToken(), ChessGame.TeamColor.BLACK, 2)
        );
    }

    @Test
    public void clearTest() {
        gameDAO.create(otherGame);
        gameService.clear();
        Assertions.assertArrayEquals(new GameData[]{}, gameDAO.list());
    }
}
