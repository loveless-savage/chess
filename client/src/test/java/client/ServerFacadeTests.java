package client;

import dataaccess.*;
import model.*;
import service.*;
import chess.ChessGame;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import server.Server;


public class ServerFacadeTests {
    private static ServerFacade facade;
    private static Server server;
    private static final String[] REGISTER_PARAMS = {"correctUsername","correctPassword","correct@email"};

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("localhost",port);
    }
    @BeforeEach
    public void setup() {
        facade.clear();
    }
    @AfterAll
    static void stop() {
        server.stop();
    }

    @Test
    public void clearTest() throws DataAccessException {
        facade.register(REGISTER_PARAMS);
        String oldToken = facade.authToken;
        var userDAO = new UserDAO();
        var authDAO = new AuthDAO();
        var gameDAO = new GameDAO();
        gameDAO.create(new GameData(0,null,null,"correctGame",new ChessGame()));
        facade.clear();
        Assertions.assertNull(facade.authToken,"ServerFacade should erase its authToken when clearing");
        Assertions.assertNull(userDAO.get(REGISTER_PARAMS[0]));
        Assertions.assertNull(authDAO.get(oldToken));
        Assertions.assertNull(gameDAO.get(gameDAO.getLastID()));
    }

    @Test
    public void registerTest() throws DataAccessException {
        facade.register(REGISTER_PARAMS);
        var userDAO = new UserDAO();
        Assertions.assertNotNull(userDAO.get(REGISTER_PARAMS[0]));
        var authDAO = new AuthDAO();
        Assertions.assertNotNull(authDAO.get(facade.authToken));
        Assertions.assertNotNull(facade.authToken,"ServerFacade did not save the returned value of authToken");
    }

    @Test
    public void registerAlreadyTakenTest() {
        facade.register(REGISTER_PARAMS);
        Assertions.assertThrows(AlreadyTakenException.class,() -> facade.register(REGISTER_PARAMS));
    }

    @Test
    public void loginTest() throws DataAccessException {
        var userDAO = new UserDAO();
        userDAO.create(new UserData(
                REGISTER_PARAMS[0],
                BCrypt.hashpw(REGISTER_PARAMS[1],BCrypt.gensalt()),
                REGISTER_PARAMS[2]));
        String[] loginParams = {REGISTER_PARAMS[0], REGISTER_PARAMS[1]};
        facade.login(loginParams);
        var authDAO = new AuthDAO();
        Assertions.assertNotNull(authDAO.get(facade.authToken));
    }

    @Test
    public void loginUserNotFoundTest() {
        String[] loginParams = {REGISTER_PARAMS[0]+"_bad", REGISTER_PARAMS[1]};
        Assertions.assertThrows(UnauthorizedException.class,() -> facade.login(loginParams));
    }

    @Test
    public void loginBadPasswordTest() {
        String[] loginParams = {REGISTER_PARAMS[0], REGISTER_PARAMS[1]+"_bad"};
        Assertions.assertThrows(UnauthorizedException.class,() -> facade.login(loginParams));
    }

    @Test
    public void logoutTest() throws DataAccessException {
        facade.register(REGISTER_PARAMS);
        String oldToken = facade.authToken;
        facade.logout();
        Assertions.assertNull(facade.authToken,"ServerFacade should erase its authToken when logging out");
        var userDAO = new UserDAO();
        Assertions.assertNotNull(userDAO.get(REGISTER_PARAMS[0]));
        var authDAO = new AuthDAO();
        Assertions.assertNull(authDAO.get(oldToken));
    }

    @Test
    public void logoutUnauthorizedTest() {
        facade.register(REGISTER_PARAMS);
        facade.authToken += "_bad";
        Assertions.assertThrows(UnauthorizedException.class,() -> facade.logout());
    }

    @Test
    public void listGamesTest() throws DataAccessException {
        facade.register(REGISTER_PARAMS);
        var gameDAO = new GameDAO();
        gameDAO.create(new GameData(0,null,null,"correctGame",new ChessGame()));
        gameDAO.create(new GameData(0,null,null,"otherGame",new ChessGame()));
        GameData[] gamelist = facade.listGames();
        Assertions.assertEquals(2,gamelist.length);
        Assertions.assertEquals("correctGame",gamelist[0].gameName());
        Assertions.assertEquals("otherGame",gamelist[1].gameName());
    }

    @Test
    public void listGamesUnauthorizedTest() {
        facade.register(REGISTER_PARAMS);
        facade.authToken += "_bad";
        Assertions.assertThrows(UnauthorizedException.class,() -> facade.listGames());
    }

    @Test
    public void createGameTest() throws DataAccessException {
        facade.register(REGISTER_PARAMS);
        int gameID = facade.createGame("correctGame");
        GameData targetData = new GameData(gameID,null,null,"correctGame",new ChessGame());
        var gameDAO = new GameDAO();
        Assertions.assertEquals(targetData,gameDAO.get(gameID));
    }

    @Test
    public void createGameUnauthorizedTest() {
        facade.register(REGISTER_PARAMS);
        facade.authToken += "_bad";
        Assertions.assertThrows(UnauthorizedException.class,() -> facade.createGame("correctGame"));
    }

    @Test
    public void joinGameTest() throws DataAccessException {
        facade.register(REGISTER_PARAMS);
        int gameID = facade.createGame("correctGame");
        String[] joinParams = {String.valueOf(gameID),"WHITE"};
        facade.joinGame(joinParams);
        GameData targetData = new GameData(gameID, REGISTER_PARAMS[0],null,"correctGame",new ChessGame());
        var gameDAO = new GameDAO();
        Assertions.assertEquals(targetData,gameDAO.get(gameID));
        joinParams[1] = "BLACK";
        facade.joinGame(joinParams);
        targetData = new GameData(gameID, REGISTER_PARAMS[0], REGISTER_PARAMS[0], "correctGame",new ChessGame());
        Assertions.assertEquals(targetData,gameDAO.get(gameID));
    }

    @Test
    public void joinGameUnauthorizedTest() {
        facade.register(REGISTER_PARAMS);
        int gameID = facade.createGame("correctGame");
        String[] joinParams = {String.valueOf(gameID),"WHITE"};
        facade.authToken += "_bad";
        Assertions.assertThrows(UnauthorizedException.class,() -> facade.joinGame(joinParams));
    }

    @Test
    public void joinGameAlreadyTakenTest() {
        facade.register(REGISTER_PARAMS);
        int gameID = facade.createGame("correctGame");
        String[] joinParams = {String.valueOf(gameID),"WHITE"};
        facade.joinGame(joinParams);
        Assertions.assertThrows(AlreadyTakenException.class,() -> facade.joinGame(joinParams));
        joinParams[1] = "BLACK";
        facade.joinGame(joinParams);
        Assertions.assertThrows(AlreadyTakenException.class,() -> facade.joinGame(joinParams));
    }
}
