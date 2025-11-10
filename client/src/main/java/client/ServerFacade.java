package client;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import model.*;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String host;
    private final int port;
    private String authToken;
    private int id;

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void register() {
        try {
            var request = requestBuilder("POST", "/user", new UserData("correctUser","pass","correct@email"), false);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[" + response.statusCode() + "]: " + response.body());
            authToken = new Gson().fromJson(response.body(), AuthData.class).authToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void login() {
        try {
            var request = requestBuilder("POST", "/session", new UserData("correctUser","pass",null), false);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[" + response.statusCode() + "]: " + response.body());
            authToken = new Gson().fromJson(response.body(), AuthData.class).authToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logout() {
        try {
            var request = requestBuilder("DELETE","/session",null,true);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[" + response.statusCode() + "]: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class ListResponse { GameData[] games; }
    public void listGames() {
        try {
            var request = requestBuilder("GET","/game",null,true);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[" + response.statusCode() + "]: " + response.body());
            GameData[] list = new Gson().fromJson(response.body(), ListResponse.class).games;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class CreateResponse { int gameID; }
    public void createGame() {
        try {
            var request = requestBuilder("POST","/game",Map.of("gameName","heey"),true);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[" + response.statusCode() + "]: " + response.body());
            id = new Gson().fromJson(response.body(), CreateResponse.class).gameID;
            System.out.println(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void joinGame() {
        try {
            var request = requestBuilder("PUT","/game",Map.of("playerColor","BLACK","gameID",String.valueOf(id)),true);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[" + response.statusCode() + "]: " + response.body());
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
            request = request.header("authorization",authToken);
        } else if (body != null) {
            request = request.header("Content-type","application/json");
        }
        return request.build();
    }
}
