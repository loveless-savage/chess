package datamodel;

import chess.ChessGame;

public record GameData(int GameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}
