package client;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WebsocketCommunicator extends Endpoint {
    public Session session;
    public WebsocketCommunicator(String hostIn, int portIn) throws Exception {
        URI uri = new URI(String.format("ws://%s:%d/ws",hostIn,portIn));
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
