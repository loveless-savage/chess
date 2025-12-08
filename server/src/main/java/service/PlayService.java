package service;

import java.util.Objects;
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
    private final Map<WsContext,PlayCommand> clientList = new HashMap<>();
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
        ChessGame newGame = gameData.game();
        switch (cmd.getCommandType()) {
            case CONNECT -> {
                PlayCommand connectCmd;
                try {
                    connectCmd = new Gson().fromJson(ctx.message(), PlayCommand.class);
                } catch (JsonSyntaxException e) {
                    sendError(ctx,"command cannot be parsed");
                    return;
                }
                String joinMsg = username;
                if (connectCmd.getTeam()==ChessGame.TeamColor.WHITE) {
                    joinMsg += " has joined the game as WHITE";
                } else if (connectCmd.getTeam()==ChessGame.TeamColor.BLACK) {
                    joinMsg += " has joined the game as BLACK";
                } else {
                    joinMsg += " is observing the game";
                }
                NotificationMessage notifyJoin = new NotificationMessage(joinMsg);
                clientList.keySet().stream().filter(
                        c -> c.session.isOpen() &&
                        Objects.equals(clientList.get(c).getGameID(), connectCmd.getGameID())
                ).forEach(otherCtx ->
                        otherCtx.send(gson.toJson(notifyJoin))
                );
                clientList.put(ctx,connectCmd);
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
                try {
                    newGame.makeMove(move);
                } catch (InvalidMoveException e) {
                    sendError(ctx,e.getMessage());
                    return;
                }
                newData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), newGame);
                try {
                    gameDAO.update(newData);
                } catch (DataAccessException e) {
                    sendError(ctx,"could not connect to database. Check server");
                    return;
                }
                var allClients = clientList.keySet().stream().filter(
                        c -> c.session.isOpen() &&
                        Objects.equals(clientList.get(c).getGameID(), clientList.get(ctx).getGameID())
                );
                System.out.println("sending loadGame");
                LoadGameMessage msg = new LoadGameMessage(newGame);
                clientList.keySet().stream().filter(
                        c -> c.session.isOpen() &&
                        Objects.equals(clientList.get(c).getGameID(), clientList.get(ctx).getGameID())
                ).forEach(everyCtx ->
                        everyCtx.send(gson.toJson(msg))
                );
                System.out.println("sending notifyMove");
                NotificationMessage notifyMove = new NotificationMessage(username+" just moved "+move);
                clientList.keySet().stream().filter(
                        c -> c.session.isOpen() &&
                        Objects.equals(clientList.get(c).getGameID(), clientList.get(ctx).getGameID()) &&
                        !c.equals(ctx)
                ).forEach(everyCtx ->
                        everyCtx.send(gson.toJson(notifyMove))
                );
                ChessGame.TeamColor nextTurn = newGame.getTeamTurn();
                String nextUser = nextTurn==ChessGame.TeamColor.WHITE? newData.whiteUsername():newData.blackUsername();
                if (newGame.isInCheckmate(nextTurn)) {
                    NotificationMessage notifyCheckmate = new NotificationMessage(nextUser+" is in checkmate!");
                    clientList.keySet().stream().filter(
                            c -> c.session.isOpen() &&
                            Objects.equals(clientList.get(c).getGameID(), clientList.get(ctx).getGameID())
                    ).forEach(everyCtx ->
                            everyCtx.send(gson.toJson(notifyCheckmate))
                    );
                } else if (newGame.isInStalemate(newGame.getTeamTurn())) {
                    NotificationMessage notifyStalemate = new NotificationMessage(nextUser+" is in stalemate!");
                    clientList.keySet().stream().filter(
                            c -> c.session.isOpen() &&
                            Objects.equals(clientList.get(c).getGameID(), clientList.get(ctx).getGameID())
                    ).forEach(everyCtx ->
                            everyCtx.send(gson.toJson(notifyStalemate))
                    );
                } else if (newGame.isInCheck(newGame.getTeamTurn())) {
                    NotificationMessage notifyCheck = new NotificationMessage(nextUser+" is in check");
                    clientList.keySet().stream().filter(
                            c -> c.session.isOpen() &&
                            Objects.equals(clientList.get(c).getGameID(), clientList.get(ctx).getGameID())
                    ).forEach(everyCtx ->
                            everyCtx.send(gson.toJson(notifyCheck))
                    );
                }
            }
            case LEAVE -> {
                newData = switch(clientList.get(ctx).getTeam()) {
                    case WHITE -> new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
                    case BLACK -> new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
                    case null -> gameData;
                };
                try {
                    gameDAO.update(newData);
                } catch (DataAccessException e) {
                    sendError(ctx,"could not connect to database. Check server");
                    return;
                }
                System.out.println("sending notifyLeave");
                NotificationMessage notifyLeave = new NotificationMessage(username+" has left the game");
                clientList.keySet().stream().filter(
                        c -> c.session.isOpen() &&
                        Objects.equals(clientList.get(c).getGameID(), clientList.get(ctx).getGameID()) &&
                        !c.equals(ctx)
                ).forEach(everyCtx ->
                        everyCtx.send(gson.toJson(notifyLeave))
                );
                clientList.remove(ctx);
            }
            case RESIGN -> {
                newGame.setOver();
                newData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), newGame);
                try {
                    gameDAO.update(newData);
                } catch (DataAccessException e) {
                    sendError(ctx,"could not connect to database. Check server");
                    return;
                }
                System.out.println("sending notifyResign");
                NotificationMessage notifyResign = new NotificationMessage(username+" has resigned");
                clientList.keySet().stream().filter(
                        c -> c.session.isOpen() &&
                        Objects.equals(clientList.get(c).getGameID(), clientList.get(ctx).getGameID())
                ).forEach(everyCtx ->
                    everyCtx.send(gson.toJson(notifyResign))
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
