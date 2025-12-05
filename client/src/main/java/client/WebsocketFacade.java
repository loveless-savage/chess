package client;

import javax.websocket.*;
import websocket.commands.*;
import websocket.messages.*;
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
                System.out.println(message);
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

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
