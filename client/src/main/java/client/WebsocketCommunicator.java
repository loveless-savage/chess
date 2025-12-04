package client;

import javax.websocket.*;
import websocket.messages.*;
import java.io.IOException;
import java.net.URI;
import com.google.gson.Gson;

public class WebsocketCommunicator extends Endpoint {
    public Session session;
    NotificationHandler notificationHandler;

    public WebsocketCommunicator(String hostIn, int portIn, NotificationHandler notificationHandler) throws Exception {
        URI uri = new URI(String.format("ws://%s:%d/ws",hostIn,portIn));
        this.notificationHandler = notificationHandler;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                var msg = new Gson().fromJson(message, NotificationMessage.class);
                notificationHandler.notify(null); // FIXME
                System.out.println(msg);
            }
        });
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
