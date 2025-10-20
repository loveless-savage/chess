package service;

import chess.ChessGame;

public record GameJoinRequest(
        String authToken,
        ChessGame.TeamColor playerColor,
        int gameID
) {
}
