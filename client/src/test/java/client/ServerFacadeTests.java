package client;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import server.Server;


public class ServerFacadeTests {
    private static ServerFacade facade;
    private static Server server;
    private static final String[] registerParams = {"correctUsername","correctPassword","correct@email"};

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
    public void clearTest() {
        facade.register(registerParams);
        String oldToken = facade.authToken;
        facade.clear();
        Assertions.assertNull(facade.authToken,"ServerFacade should erase its authToken when clearing");
        try {
            var userDAO = new UserDAO();
            Assertions.assertNull(userDAO.get(registerParams[0]));
            var authDAO = new AuthDAO();
            Assertions.assertNull(authDAO.get(oldToken));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void registerTest() {
        facade.register(registerParams);
        try {
            var userDAO = new UserDAO();
            Assertions.assertNotNull(userDAO.get(registerParams[0]));
            var authDAO = new AuthDAO();
            Assertions.assertNotNull(authDAO.get(facade.authToken));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        Assertions.assertNotNull(facade.authToken,"ServerFacade did not save the returned value of authToken");
    }

    @Test
    public void loginTest() {
        try {
            var userDAO = new UserDAO();
            userDAO.create(new UserData(
                    registerParams[0],
                    BCrypt.hashpw(registerParams[1],BCrypt.gensalt()),
                    registerParams[2]));
            String[] loginParams = {registerParams[0],registerParams[1]};
            facade.login(loginParams);
            var authDAO = new AuthDAO();
            Assertions.assertNotNull(authDAO.get(facade.authToken));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void logoutTest() {
        facade.register(registerParams);
        String oldToken = facade.authToken;
        facade.logout();
        Assertions.assertNull(facade.authToken,"ServerFacade should erase its authToken when logging out");
        try {
            var userDAO = new UserDAO();
            Assertions.assertNotNull(userDAO.get(registerParams[0]));
            var authDAO = new AuthDAO();
            Assertions.assertNull(authDAO.get(oldToken));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void listGamesTest() {
    }

    @Test
    public void createGameTest() {
    }

    @Test
    public void joinGameTest() {
    }
}
