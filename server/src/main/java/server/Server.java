package server;

import com.google.gson.Gson;
import io.javalin.*;

public class Server {

    private final Javalin javalin;
    private final Gson serializer;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        serializer = new Gson();

        javalin.delete("/db", ctx -> {
            System.out.println(ctx.body().getClass());
            ctx.status(418);
            ctx.contentType("text/plain");
            ctx.result(serializer.toJson("heey"));
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
