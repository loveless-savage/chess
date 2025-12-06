package service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import chess.*;
import model.*;
import dataaccess.*;
import websocket.commands.*;
import websocket.messages.*;

public class PlayService implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    final AuthDAO authDAO;
    final GameDAO gameDAO;
    private final Map<WsContext,String> clientList = new HashMap<>();
    private static final Gson gson = new Gson();

    public PlayService() {
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
    }
    public PlayService(AuthDAO authIn, GameDAO gameIn) {
        authDAO = authIn;
        gameDAO = gameIn;
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket user connected");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        UserGameCommand cmd;
        try {
            cmd = gson.fromJson(ctx.message(), UserGameCommand.class);
        } catch (JsonSyntaxException e) {
            sendError(ctx,"command cannot be parsed");
            return;
        }
        String username;
        GameData gameData, newData;
        try {
            AuthData authData = authDAO.get(cmd.getAuthToken());
            if (authData == null) {
                sendError(ctx,"bad authtoken. Try signing out and in again.");
                return;
            }
            username = authData.username();
            gameData = gameDAO.get(cmd.getGameID());
            if (gameData == null) {
                sendError(ctx,"game cannot be found");
                return;
            }
        } catch (DataAccessException e) {
            sendError(ctx,"could not connect to database. Check server");
            return;
        }
        switch (cmd.getCommandType()) {
            case CONNECT -> {
                NotificationMessage notifyJoin = new NotificationMessage(username+" has joined the game");
                clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(otherCtx ->
                        otherCtx.send(gson.toJson(notifyJoin))
                );
                clientList.put(ctx,cmd.getAuthToken());
                System.out.println("sending loadGame");
                LoadGameMessage msg = new LoadGameMessage(gameData.game());
                ctx.send(gson.toJson(msg));
            }
            case MAKE_MOVE -> {
                ChessMove move;
                try {
                    move = gson.fromJson(ctx.message(), MakeMoveCommand.class).getMove();
                } catch (JsonSyntaxException e) {
                    sendError(ctx,"command cannot be parsed");
                    return;
                }
                ChessGame updatedGame = gameData.game();
                try {
                    updatedGame.makeMove(move);
                } catch (InvalidMoveException e) {
                    sendError(ctx,e.getMessage());
                    return;
                }
                System.out.println("sending notifyMove");
                NotificationMessage notifyMove = new NotificationMessage(username+" just moved "+move);
                clientList.keySet().stream().filter(c -> c.session.isOpen())
                        .filter(c -> !c.equals(ctx)).forEach(everyCtx ->
                        everyCtx.send(gson.toJson(notifyMove))
                );
                // TODO: are we in check / checkmate / stalemate?
                newData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), updatedGame);
                try {
                    gameDAO.update(newData);
                } catch (DataAccessException e) {
                    sendError(ctx,"could not connect to database. Check server");
                    return;
                }
                System.out.println("sending loadGame");
                LoadGameMessage msg = new LoadGameMessage(updatedGame);
                clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(everyCtx ->
                        everyCtx.send(gson.toJson(msg))
                );
            }
            case LEAVE -> {
                if (username.equals(gameData.whiteUsername())) {
                    newData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
                } else if (username.equals(gameData.blackUsername())) {
                    newData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
                } else {
                    newData = gameData;
                }
                try {
                    gameDAO.update(newData);
                } catch (DataAccessException e) {
                    sendError(ctx,"could not connect to database. Check server");
                    return;
                }
                System.out.println("sending notifyLeave");
                clientList.remove(ctx);
                if (!clientList.isEmpty()) {
                    NotificationMessage notifyLeave = new NotificationMessage(username+" has left the game");
                    clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(otherCtx ->
                            otherCtx.send(gson.toJson(notifyLeave))
                    );
                }
            }
            case RESIGN -> {
                // TODO: update game
                System.out.println("sending notifyResign");
                NotificationMessage notifyResign = new NotificationMessage(username+" has resigned");
                clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(otherCtx ->
                    otherCtx.send(gson.toJson(notifyResign))
                );
            }
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void sendError(WsContext ctx, String errorMsg) {
        ErrorMessage msg = new ErrorMessage("ERROR: "+errorMsg);
        ctx.send(gson.toJson(msg));
    }
}
