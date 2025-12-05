package client;

import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(String notification);
}