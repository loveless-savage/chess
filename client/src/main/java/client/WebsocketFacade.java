package client;

import chess.ChessMove;
import websocket.commands.*;
import websocket.messages.*;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import com.google.gson.Gson;

public class WebsocketFacade extends Endpoint {
    public Session session;
    NotificationHandler notificationHandler;

    public WebsocketFacade(String hostIn, int portIn, NotificationHandler notificationHandler) throws Exception {
        URI uri = new URI(String.format("ws://%s:%d/ws",hostIn,portIn));
        this.notificationHandler = notificationHandler;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                switch(new Gson().fromJson(message, ServerMessage.class).getServerMessageType()) {
                    case LOAD_GAME ->
                            notificationHandler.loadGame(new Gson().fromJson(message, LoadGameMessage.class).getGame());
                    case ERROR ->
                            notificationHandler.sendError(new Gson().fromJson(message, ErrorMessage.class));
                    case NOTIFICATION ->
                            notificationHandler.notify(new Gson().fromJson(message, NotificationMessage.class));
                }
            }
        });
    }

    public WebsocketFacade(NotificationHandler notificationHandler) throws Exception {
        this("localhost",8080,notificationHandler);
    }

    public void connect(String authToken, int gameID) throws IOException {
        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT,authToken,gameID);
        session.getBasicRemote().sendText(new Gson().toJson(cmd));
    }

    public void makeMove(ChessMove move, String authToken, Integer gameID) throws IOException {
        MakeMoveCommand cmd = new MakeMoveCommand(move,authToken,gameID);
        session.getBasicRemote().sendText(new Gson().toJson(cmd));
    }

    public void leave(String authToken, int gameID) throws IOException {
        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.LEAVE,authToken,gameID);
        session.getBasicRemote().sendText(new Gson().toJson(cmd));
    }

    public void resign(String authToken, int gameID) throws IOException {
        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN,authToken,gameID);
        session.getBasicRemote().sendText(new Gson().toJson(cmd));
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
