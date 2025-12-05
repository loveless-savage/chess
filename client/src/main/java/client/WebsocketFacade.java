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
                notificationHandler.notify(message);
            }
        });
    }

    public WebsocketFacade(NotificationHandler notificationHandler) throws Exception {
        this("localhost",8080,notificationHandler);
    }

    public void send(String message) throws IOException, EncodeException {
        if(message.equals("connect")) {
            UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT,"fillerAuth",31);
            session.getBasicRemote().sendText(new Gson().toJson(cmd));
        } else {
            session.getBasicRemote().sendText(message);
        }
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
