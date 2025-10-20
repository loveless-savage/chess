package service;

import chess.ChessGame;

public record GameCreateRequest(
        String authToken,
        String gameName
) {
}
