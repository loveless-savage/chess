package service;

import chess.ChessGame;

public record GameJoinRequest(
        ChessGame.TeamColor playerColor,
        int gameID
) {
}
