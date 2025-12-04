package service;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import websocket.messages.*;

public class PlayService implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket connected");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        System.out.println("Received message " + ctx.message());
        NotificationMessage msg = new NotificationMessage(ctx.message());
        ctx.send(new Gson().toJson(msg));
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }
}
