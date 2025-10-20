package service;

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
    public void setup() {
        userService.register(goodData);
    }
    @AfterEach
    public void takeDown() {
        userService.clear();
    }

    @Test
    public void registerTest() {
        userService.clear();
        Assertions.assertEquals("correctUsername", userService.register(goodData).username());
        Assertions.assertEquals("otherUsername", userService.register(otherData).username());
    }
    @Test
    public void registerAlreadyTakenTest() {
        Assertions.assertThrows(AlreadyTakenException.class,() -> userService.register(goodData));
    }

    @Test
    public void loginTest() {
        Assertions.assertEquals(loginRequest.username(), userService.login(loginRequest).username());
    }
    @Test
    public void loginUserNotFoundTest() {
        LoginRequest badLoginRequest = new LoginRequest("badUsername","correctPassword");
        Assertions.assertThrows(NotFoundException.class,() -> userService.login(badLoginRequest));
    }
    @Test
    public void loginBadPasswordTest() {
        LoginRequest badLoginRequest = new LoginRequest("correctUsername","badPassword");
        Assertions.assertThrows(UnauthorizedException.class,() -> userService.login(badLoginRequest));
    }

    @Test
    public void logoutTest() {
        String goodToken = userService.login(loginRequest).authToken();
        userService.logout(goodToken);
        Assertions.assertThrows(UnauthorizedException.class,() -> userService.logout(goodToken));
    }
    @Test
    public void logoutUnauthorizedTest() {
        String badToken = userService.login(loginRequest).authToken() + "_bad";
        Assertions.assertThrows(UnauthorizedException.class,() -> userService.logout(badToken));
    }

    @Test
    public void clearTest() {
        userService.register(otherData);
        userService.clear();
        LoginRequest loginRequest = new LoginRequest("correctUsername","correctPassword");
        Assertions.assertThrows(NotFoundException.class,() -> userService.login(loginRequest));
        LoginRequest otherLoginRequest = new LoginRequest("otherUsername","otherPassword");
        Assertions.assertThrows(NotFoundException.class,() -> userService.login(otherLoginRequest));
    }
}
