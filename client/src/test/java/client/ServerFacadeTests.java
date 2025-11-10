package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {
    private static ServerFacade facade;
    private static Server server;

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
    }

    @Test
    public void registerTest() {
    }

    @Test
    public void loginTest() {
    }

    @Test
    public void logoutTest() {
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
