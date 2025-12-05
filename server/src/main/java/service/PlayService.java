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
            ErrorMessage msg = new ErrorMessage("ERROR: command cannot be parsed");
            ctx.send(gson.toJson(msg));
            return;
        }
        String username;
        GameData gameData;
        try {
            username = authDAO.get(cmd.getAuthToken()).username();
            gameData = gameDAO.get(cmd.getGameID());
        } catch (DataAccessException e) {
            // TODO: unauthorized
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
                    ErrorMessage msg = new ErrorMessage("ERROR: command cannot be parsed");
                    ctx.send(gson.toJson(msg));
                    return;
                }
                ChessGame updatedGame = gameData.game();
                try {
                    updatedGame.makeMove(move);
                } catch (InvalidMoveException e) {
                    ErrorMessage errorMsg = new ErrorMessage("ERROR: "+e.getMessage());
                    ctx.send(gson.toJson(errorMsg));
                    return;
                }
                System.out.println("sending notifyMove");
                NotificationMessage notifyMove = new NotificationMessage(username+" just moved "+move);
                clientList.keySet().stream().filter(c -> c.session.isOpen())
                        .filter(c -> !c.equals(ctx)).forEach(everyCtx ->
                        everyCtx.send(gson.toJson(notifyMove))
                );
                // TODO: are we in check / checkmate / stalemate?
                GameData newData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), updatedGame);
                try {
                    gameDAO.update(newData);
                } catch (DataAccessException e) {
                    // TODO: unauthorized
                    return;
                }
                System.out.println("sending loadGame");
                LoadGameMessage msg = new LoadGameMessage(updatedGame);
                clientList.keySet().stream().filter(c -> c.session.isOpen()).forEach(everyCtx ->
                        everyCtx.send(gson.toJson(msg))
                );
            }
            case LEAVE -> {
                GameData newData;
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
                    // TODO: unauthorized
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
}
