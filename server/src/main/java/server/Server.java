package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import io.javalin.*;
import service.*;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final Gson serializer;
    private final UserService userService;
    private final GameService gameService;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        serializer = new Gson();
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);

        javalin.delete("/db", ctx -> { // Clear application
            gameService.clear();
            userService.clear();
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
        });

        javalin.post("/user", ctx -> { // Register
            UserData registerRequest = serializer.fromJson(ctx.body(),UserData.class);
            var registerResult = userService.register(registerRequest);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(serializer.toJson(registerResult));
        });

        javalin.post("/session", ctx -> { // Login
            LoginRequest loginRequest = serializer.fromJson(ctx.body(),LoginRequest.class);
            var loginResult = userService.login(loginRequest);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(serializer.toJson(loginResult));
        });

        javalin.delete("/session", ctx -> { // Logout
            String authToken = ctx.header("authorization");
            userService.logout(authToken);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
        });

        javalin.get("/game", ctx -> { // List games
            String authToken = ctx.header("authorization");
            GameData[] listResult = gameService.listGames(authToken);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(serializer.toJson(listResult));
        });

        javalin.post("/game", ctx -> { // Create Game
            String authToken = ctx.header("authorization");
            int gameIDResponse = gameService.createGame(authToken,ctx.body());
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{gameID:"+gameIDResponse+"}");
        });

        javalin.put("/game", ctx -> { // Join Game
            String authToken = ctx.header("authorization");
            GameJoinRequest joinRequest = serializer.fromJson(ctx.body(),GameJoinRequest.class);
            gameService.joinGame("",null,0);
            ctx.status(200);
            ctx.result("{}");
        });


        javalin.exception(BadRequestException.class, (e,ctx) -> {
            ctx.status(400);
            ctx.contentType("application/json");
            ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage() )));
        });

        javalin.exception(UnauthorizedException.class, (e,ctx) -> {
            ctx.status(401);
            ctx.contentType("application/json");
            ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage() )));
        });

        javalin.exception(AlreadyTakenException.class, (e,ctx) -> {
            ctx.status(403);
            ctx.contentType("application/json");
            ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage() )));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
