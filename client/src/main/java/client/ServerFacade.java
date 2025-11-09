package client;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import model.*;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String host = "localhost";
    private final int port = 8080;

    public void clear() {
        try {
            var request = requestBuilder("DELETE", "/db", null);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[" + response.statusCode() + "]: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest requestBuilder(String method, String path, Object body) throws Exception {
        String urlString = String.format("http://%s:%d%s", host, port, path);
        var request = HttpRequest.newBuilder()
                .uri(new URI(urlString))
                .timeout(java.time.Duration.ofMillis(5000))
                .method(method,
                        body != null ?
                        HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)):
                        HttpRequest.BodyPublishers.noBody()
                );
        return request.build();
    }
}
