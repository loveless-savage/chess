package service;

import dataaccess.DataAccessException;
import model.*;
import org.junit.jupiter.api.*;

public class UserServiceTests {
    private static UserService userService;
    private static UserData goodData, otherData;
    private static LoginRequest loginRequest;

    @BeforeAll
    public static void init(){
        userService = new UserService();
        goodData = new UserData("correctUsername","correctPassword","correct@email");
        otherData = new UserData("otherUsername","otherPassword","other@email");
        loginRequest = new LoginRequest("correctUsername","correctPassword");
    }
    @BeforeEach
    public void setup() throws DataAccessException {
        userService.register(goodData);
    }
    @AfterEach
    public void takeDown() throws DataAccessException {
        userService.clear();
    }

    @Test
    public void registerTest() throws DataAccessException {
        userService.clear();
        Assertions.assertEquals("correctUsername", userService.register(goodData).username());
        Assertions.assertEquals("otherUsername", userService.register(otherData).username());
    }
    @Test
    public void registerAlreadyTakenTest() {
        Assertions.assertThrows(AlreadyTakenException.class,() -> userService.register(goodData));
    }

    @Test
    public void loginTest() throws DataAccessException {
        Assertions.assertEquals(loginRequest.username(), userService.login(loginRequest).username());
    }
    @Test
    public void loginUserNotFoundTest() {
        LoginRequest badLoginRequest = new LoginRequest("badUsername","correctPassword");
        Assertions.assertThrows(UnauthorizedException.class,() -> userService.login(badLoginRequest));
    }
    @Test
    public void loginBadPasswordTest() {
        LoginRequest badLoginRequest = new LoginRequest("correctUsername","badPassword");
        Assertions.assertThrows(UnauthorizedException.class,() -> userService.login(badLoginRequest));
    }

    @Test
    public void logoutTest() throws DataAccessException {
        userService.clear();
        String goodToken = userService.register(goodData).authToken();
        userService.logout(goodToken);
        Assertions.assertThrows(UnauthorizedException.class,() -> userService.logout(goodToken));
    }
    @Test
    public void logoutUnauthorizedTest() throws DataAccessException {
        userService.clear();
        String badToken = userService.register(goodData).authToken() + "_bad";
        Assertions.assertThrows(UnauthorizedException.class,() -> userService.logout(badToken));
    }

    @Test
    public void clearTest() throws DataAccessException {
        userService.register(otherData);
        userService.clear();
        LoginRequest loginRequest = new LoginRequest("correctUsername","correctPassword");
        Assertions.assertThrows(UnauthorizedException.class,() -> userService.login(loginRequest));
        LoginRequest otherLoginRequest = new LoginRequest("otherUsername","otherPassword");
        Assertions.assertThrows(UnauthorizedException.class,() -> userService.login(otherLoginRequest));
    }
}
