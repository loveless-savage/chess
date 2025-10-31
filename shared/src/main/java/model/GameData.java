package model;

import chess.ChessGame;

public record GameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game
) implements ModelData<Integer> {
    public String keyName() {
        return "gameID";
    }
    public Integer keyValue() {
        return gameID;
    }
}
