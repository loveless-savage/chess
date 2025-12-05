package client;

import chess.ChessGame;
import websocket.messages.*;

public interface NotificationHandler {
    void notify(NotificationMessage notification);
    void sendError(ErrorMessage errorMsg);
    void loadGame(ChessGame game);
}