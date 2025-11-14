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
                throw new ServerException(response.body());
            default:
                throw new RuntimeException("Unexpected status code");
        }
    }

    public void login(String[] params) {
        if (params.length != 2) {
            throw new RuntimeException("login() expects 2 String parameters in an array, but you provided "+params.length);
        }
        HttpResponse<String> response;
        try {
            var request = requestBuilder("POST", "/session", new UserData(params[0],params[1],null), false);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("login -> [" + response.statusCode() + "]: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (response.statusCode()) {
            case 200:
                authToken = new Gson().fromJson(response.body(), AuthData.class).authToken();
                break;
            case 400:
                throw new BadRequestException(response.body());
            case 401:
                throw new UnauthorizedException(response.body());
            case 500:
                throw new ServerException(response.body());
            default:
                throw new RuntimeException("Unexpected status code");
        }
    }

    public void logout() {
        HttpResponse<String> response;
        try {
            var request = requestBuilder("DELETE","/session",null,true);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("logout -> [" + response.statusCode() + "]: " + response.body());
            authToken = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (response.statusCode()) {
            case 200:
                authToken = new Gson().fromJson(response.body(), AuthData.class).authToken();
                break;
            case 401:
                throw new UnauthorizedException(response.body());
            case 500:
                throw new ServerException(response.body());
            default:
                throw new RuntimeException("Unexpected status code");
        }
    }

    static class ListResponse { GameData[] games; }
    public GameData[] listGames() {
        HttpResponse<String> response;
        try {
            var request = requestBuilder("GET","/game",null,true);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("listGames -> [" + response.statusCode() + "]: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (response.statusCode()) {
            case 200:
                return new Gson().fromJson(response.body(), ListResponse.class).games;
            case 401:
                throw new UnauthorizedException(response.body());
            case 500:
                throw new ServerException(response.body());
            default:
                throw new RuntimeException("Unexpected status code");
        }
    }

    static class CreateResponse { int gameID; }
    public int createGame(String param) {
        HttpResponse<String> response;
        try {
            var request = requestBuilder("POST","/game",Map.of("gameName",param),true);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("createGame -> [" + response.statusCode() + "]: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (response.statusCode()) {
            case 200:
                return new Gson().fromJson(response.body(), CreateResponse.class).gameID;
            case 400:
                throw new BadRequestException(response.body());
            case 401:
                throw new UnauthorizedException(response.body());
            case 500:
                throw new ServerException(response.body());
            default:
                throw new RuntimeException("Unexpected status code");
        }
    }

    public void joinGame(String[] params) {
        if (params.length != 2) {
            throw new RuntimeException("joinGame() expects 2 String parameters in an array, but you provided "+params.length);
        }
        HttpResponse<String> response;
        try {
            var request = requestBuilder("PUT","/game",Map.of("gameID",params[0],"playerColor",params[1]),true);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("joinGame -> [" + response.statusCode() + "]: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (response.statusCode()) {
            case 200:
                break;
            case 400:
                throw new BadRequestException(response.body());
            case 401:
                throw new UnauthorizedException(response.body());
            case 403:
                throw new AlreadyTakenException(response.body());
            case 500:
                throw new ServerException(response.body());
            default:
                throw new RuntimeException("Unexpected status code");
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
