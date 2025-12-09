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
    private final Map<WsMessageContext,PlayCommand> clientList = new HashMap<>();
    private static final Gson GSON = new Gson();

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
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        UserGameCommand cmd;
        try {
            cmd = GSON.fromJson(ctx.message(), UserGameCommand.class);
        } catch (JsonSyntaxException e) {
            sendError(ctx,"command cannot be parsed");
            return;
        }
        String username;
        GameData gameData;
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
            case CONNECT -> connect(ctx,username,gameData,cmd);
            case MAKE_MOVE -> makeMove(ctx,username,gameData);
            case LEAVE -> leave(ctx,username,gameData);
            case RESIGN -> resign(ctx,username,gameData);
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {}

    private void connect(WsMessageContext ctx, String username, GameData gameData, UserGameCommand cmd) {
        ChessGame.TeamColor team = null;
        String joinMsg = username;
        if (username.equals(gameData.whiteUsername())) {
            team = ChessGame.TeamColor.WHITE;
            joinMsg += " has joined the game as WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            team = ChessGame.TeamColor.BLACK;
            joinMsg += " has joined the game as BLACK";
        } else {
            joinMsg += " is observing the game";
        }
        PlayCommand connectCmd = new PlayCommand(cmd.getAuthToken(),cmd.getGameID(),team);
        NotificationMessage notifyJoin = new NotificationMessage(joinMsg);
        notifyAll(cmd.getGameID(),notifyJoin);
        clientList.put(ctx,connectCmd);
        LoadGameMessage msg = new LoadGameMessage(gameData.game());
        ctx.send(GSON.toJson(msg));
    }

    private void makeMove(WsMessageContext ctx, String username, GameData gameData) {
        ChessMove move;
        try {
            move = GSON.fromJson(ctx.message(), MakeMoveCommand.class).getMove();
        } catch (JsonSyntaxException e) {
            sendError(ctx,"command cannot be parsed");
            return;
        }

        ChessGame newGame = gameData.game();
        ChessGame.TeamColor team = clientList.get(ctx).getTeam();
        try {
            newGame.makeMove(move,team);
        } catch (InvalidMoveException e) {
            sendError(ctx,e.getLocalizedMessage());
            return;
        }
        GameData newData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), newGame);
        try {
            gameDAO.update(newData);
        } catch (DataAccessException e) {
            sendError(ctx,"could not connect to database. Check server");
            return;
        }
        int gameID = clientList.get(ctx).getGameID();
        LoadGameMessage gameMsg = new LoadGameMessage(newGame);
        notifyAll(gameID,gameMsg);
        NotificationMessage notifyMove = new NotificationMessage(username+" just moved "+move);
        notifyOthers(ctx,gameID,notifyMove);
        ChessGame.TeamColor nextTurn = newGame.getTeamTurn();
        String nextUser = nextTurn==ChessGame.TeamColor.WHITE? newData.whiteUsername():newData.blackUsername();
        if (newGame.isInCheckmate(nextTurn)) {
            NotificationMessage notifyCheckmate = new NotificationMessage(nextUser+" is in checkmate!");
            notifyAll(gameID,notifyCheckmate);
        } else if (newGame.isInStalemate(newGame.getTeamTurn())) {
            NotificationMessage notifyStalemate = new NotificationMessage(nextUser+" is in stalemate!");
            notifyAll(gameID,notifyStalemate);
        } else if (newGame.isInCheck(newGame.getTeamTurn())) {
            NotificationMessage notifyCheck = new NotificationMessage(nextUser+" is in check");
            notifyAll(gameID,notifyCheck);
        }
    }

    private void leave(WsMessageContext ctx, String username, GameData gameData) {
        GameData newData = switch(clientList.get(ctx).getTeam()) {
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
        NotificationMessage notifyLeave = new NotificationMessage(username+" has left the game");
        notifyOthers(ctx,clientList.get(ctx).getGameID(),notifyLeave);
        clientList.remove(ctx);
    }

    private void resign(WsMessageContext ctx, String username, GameData gameData) {
        ChessGame newGame = gameData.game();
        if (clientList.get(ctx).getTeam() == null) {
            sendError(ctx,"you are not a player");
            return;
        }
        if (gameData.game().isOver()) {
            sendError(ctx,"game is already over");
            return;
        }
        newGame.setOver();
        GameData newData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), newGame);
        try {
            gameDAO.update(newData);
        } catch (DataAccessException e) {
            sendError(ctx,"could not connect to database. Check server");
            return;
        }
        NotificationMessage notifyResign = new NotificationMessage(username+" has resigned");
        notifyAll(clientList.get(ctx).getGameID(),notifyResign);
    }

    private void notifyAll(int gameID, ServerMessage notify) {
        clientList.keySet().stream().filter(
                c -> c.session.isOpen() && clientList.get(c).getGameID() == gameID
        ).forEach(everyCtx ->
                everyCtx.send(GSON.toJson(notify))
        );
    }

    private void notifyOthers(WsMessageContext ctx, int gameID, ServerMessage notify) {
        clientList.keySet().stream().filter(
                c -> c.session.isOpen() && clientList.get(c).getGameID() == gameID && !c.equals(ctx)
        ).forEach(everyCtx ->
                everyCtx.send(GSON.toJson(notify))
        );
    }

    private void sendError(WsMessageContext ctx, String errorMsg) {
        ErrorMessage msg = new ErrorMessage("ERROR: "+errorMsg);
        ctx.send(GSON.toJson(msg));
    }
}
