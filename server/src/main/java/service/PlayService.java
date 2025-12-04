package service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import chess.*;
import websocket.commands.*;
import websocket.messages.*;

public class PlayService implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final Map<WsContext,String> clientList = new HashMap<>();

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket user connected");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        System.out.println("Received message " + ctx.message());
        UserGameCommand cmd;
        try {
            cmd = new Gson().fromJson(ctx.message(), UserGameCommand.class);
        } catch (JsonSyntaxException e) {
            ErrorMessage msg = new ErrorMessage("ERROR: command cannot be parsed");
            ctx.send(new Gson().toJson(msg));
            return;
        }
        switch (cmd.getCommandType()) {
            case CONNECT -> {
                System.out.println("Connecting user "+cmd.getAuthToken()+" to game "+cmd.getGameID()); // FIXME
                NotificationMessage notifyJoin = new NotificationMessage(cmd.getAuthToken()+" has joined the game");
                clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(otherCtx -> {
                    otherCtx.send(new Gson().toJson(notifyJoin));
                });
                clientList.put(ctx,cmd.getAuthToken());
                LoadGameMessage msg = new LoadGameMessage(new ChessGame()); // FIXME
                ctx.send(new Gson().toJson(msg));
            }
            case MAKE_MOVE -> {
                cmd = new Gson().fromJson(ctx.message(),MakeMoveCommand.class);
            }
            case LEAVE -> {}
            case RESIGN -> {}
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }
}
