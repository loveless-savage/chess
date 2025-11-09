package client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();

    public void clear() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/db"))
                .timeout(java.time.Duration.ofMillis(5000))
                .DELETE()
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("["+response.statusCode()+"]: "+response.body());
    }
}
