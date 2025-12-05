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
                clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(otherCtx ->
                        otherCtx.send(new Gson().toJson(notifyJoin))
                );
                clientList.put(ctx,cmd.getAuthToken());
                System.out.println("sending loadGame");
                LoadGameMessage msg = new LoadGameMessage(new ChessGame()); // FIXME
                ctx.send(new Gson().toJson(msg));
            }
            case MAKE_MOVE -> {
                ChessMove move;
                try {
                    move = new Gson().fromJson(ctx.message(), MakeMoveCommand.class).getMove();
                } catch (JsonSyntaxException e) {
                    ErrorMessage msg = new ErrorMessage("ERROR: command cannot be parsed");
                    ctx.send(new Gson().toJson(msg));
                    return;
                }
                // TODO: update game
                System.out.println("sending loadGame");
                LoadGameMessage msg = new LoadGameMessage(new ChessGame());
                clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(everyCtx ->
                    everyCtx.send(new Gson().toJson(msg))
                );
                System.out.println("sending notifyMove");
                NotificationMessage notifyMove = new NotificationMessage(cmd.getAuthToken()+" just moved "+move);
                clientList.keySet().stream().filter(c -> c.session.isOpen())
                        .filter(c -> !c.equals(ctx)).forEach(everyCtx ->
                        everyCtx.send(new Gson().toJson(notifyMove))
                );
                // TODO: are we in check / checkmate / stalemate?
            }
            case LEAVE -> {
                // TODO: remove player from game
                System.out.println("sending notifyLeave");
                clientList.remove(ctx);
                NotificationMessage notifyLeave = new NotificationMessage(cmd.getAuthToken()+" has left the game");
                clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(otherCtx ->
                    otherCtx.send(new Gson().toJson(notifyLeave))
                );
            }
            case RESIGN -> {
                // TODO: update game
                System.out.println("sending notifyResign");
                NotificationMessage notifyResign = new NotificationMessage(cmd.getAuthToken()+" has resigned");
                clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(otherCtx ->
                    otherCtx.send(new Gson().toJson(notifyResign))
                );
            }
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }
}
