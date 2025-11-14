package client;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import model.*;
import service.*;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String host;
    private final int port;
    String authToken;

    public ServerFacade(String hostIn, int portIn) {
        host = hostIn;
        port = portIn;
    }
    public ServerFacade() {
        this("localhost",8080);
    }

    public void clear() {
        try {
            var request = requestBuilder("DELETE", "/db", null, false);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[" + response.statusCode() + "]: " + response.body());
            authToken = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void register(String[] params) {
        if (params.length != 3) {
            throw new RuntimeException("register() expects 3 String parameters in an array, but you provided "+params.length);
        }
        HttpResponse<String> response;
        try {
            var request = requestBuilder("POST", "/user", new UserData(params[0],params[1],params[2]), false);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("register -> [" + response.statusCode() + "]: " + response.body());
        switch (response.statusCode()) {
            case 200:
                authToken = new Gson().fromJson(response.body(), AuthData.class).authToken();
                break;
            case 400:
                throw new BadRequestException(response.body());
            case 403:
                throw new AlreadyTakenException(response.body());
            case 500:
                break;
            default:
                throw new RuntimeException("Unexpected status code");
        }
    }

    public void login(String[] params) {
        if (params.length != 2) {
            throw new RuntimeException("login() expects 2 String parameters in an array, but you provided "+params.length);
        }
        try {
            var request = requestBuilder("POST", "/session", new UserData(params[0],params[1],null), false);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("login -> [" + response.statusCode() + "]: " + response.body());
            authToken = new Gson().fromJson(response.body(), AuthData.class).authToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logout() {
        try {
            var request = requestBuilder("DELETE","/session",null,true);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("logout -> [" + response.statusCode() + "]: " + response.body());
            authToken = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class ListResponse { GameData[] games; }
    public GameData[] listGames() {
        try {
            var request = requestBuilder("GET","/game",null,true);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("listGames -> [" + response.statusCode() + "]: " + response.body());
            return new Gson().fromJson(response.body(), ListResponse.class).games;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class CreateResponse { int gameID; }
    public int createGame(String param) {
        try {
            var request = requestBuilder("POST","/game",Map.of("gameName",param),true);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("createGame -> [" + response.statusCode() + "]: " + response.body());
            return new Gson().fromJson(response.body(), CreateResponse.class).gameID;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void joinGame(String[] params) {
        if (params.length != 2) {
            throw new RuntimeException("joinGame() expects 2 String parameters in an array, but you provided "+params.length);
        }
        try {
            var request = requestBuilder("PUT","/game",Map.of("playerColor",params[0],"gameID",params[1]),true);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("joinGame -> [" + response.statusCode() + "]: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest requestBuilder(String method, String path, Object body, boolean authHeader) throws Exception {
        String urlString = String.format("http://%s:%d%s", host, port, path);
        var request = HttpRequest.newBuilder()
                .uri(new URI(urlString))
                .timeout(java.time.Duration.ofMillis(5000))
                .method(method,
                        body != null ?
                        HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)):
                        HttpRequest.BodyPublishers.noBody()
                );
        if (authHeader) {
            if (authToken == null) {
                throw new UnauthorizedException("argument authHeader cannot be true when authToken is null");
            } else {
                request = request.header("authorization", authToken);
            }
        } else if (body != null) {
            request = request.header("Content-type","application/json");
        }
        return request.build();
    }
}
