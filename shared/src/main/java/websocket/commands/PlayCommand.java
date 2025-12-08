package websocket.commands;

import chess.ChessGame;

public class PlayCommand extends UserGameCommand {
    ChessGame.TeamColor team;

    public PlayCommand(String authToken, Integer gameID, ChessGame.TeamColor team) {
        super(CommandType.CONNECT,authToken,gameID);
        this.team = team;
    }

    public ChessGame.TeamColor getTeam() {
        return team;
    }
}
