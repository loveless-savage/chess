package server;

import com.google.gson.Gson;
import model.*;
import io.javalin.*;
import service.Service;

public class Server {

    private final Javalin javalin;
    private final Gson serializer;
    private final Service service;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        serializer = new Gson();
        service = new Service();

        javalin.delete("/db", ctx -> {
            System.out.println(ctx.body().getClass());
            ctx.status(200);
            ctx.contentType("text/plain");
            ctx.result(serializer.toJson("heey"));
        });

        javalin.post("/user", ctx -> {
            UserData bod = serializer.fromJson(ctx.body(),UserData.class);
            System.out.println(bod);
            ctx.status(418);
            ctx.contentType("application/json");
            var registerResult = service.register(bod);
            ctx.result(serializer.toJson(registerResult));
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
