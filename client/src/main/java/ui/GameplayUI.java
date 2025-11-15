package ui;

import chess.*;
import client.*;
import java.util.Map;
import static java.util.Map.entry;

public class GameplayUI {
    public static REPL.State parse(ServerFacade server, String cmdIn) {
        String[] cmd = cmdIn.split(" ",2);
        String[] args = cmd.length<2? null : cmd[1].split(" ");
        switch (cmd[0]) {
            case "help":
                System.out.println(helpStr);
                break;
        }
        return REPL.State.GAMEPLAY;
    }

    public static void printBoard(ChessGame game, ChessGame.TeamColor team) {
        ChessBoard board = game.getBoard();
        String borderColor = EscapeSequences.SET_BG_COLOR_WHITE + EscapeSequences.SET_TEXT_COLOR_BLACK;
        String lightColor = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String darkColor = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        String resetColor = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;
        String lightPieceColor = EscapeSequences.SET_TEXT_COLOR_WHITE;
        String darkPieceColor = EscapeSequences.SET_TEXT_COLOR_BLACK;

        String outStr = borderColor;
        String topNums = borderColor + (team == ChessGame.TeamColor.BLACK?
                                        "    h  g  f  e  d  c  b  a    ":
                                        "    a  b  c  d  e  f  g  h    ")
                        + resetColor + "\n";
        outStr += topNums;

        for (int i = 0; i < 8; i++) {
            outStr += borderColor + " " + ((team==ChessGame.TeamColor.BLACK)?1+i:8-i) + " ";
            for (int j = 0; j < 8; j++) {
                chess.ChessPiece p;
                if (team == ChessGame.TeamColor.BLACK) {
                    p = board.getPiece(new ChessPosition(8-i, 8-j));
                } else {
                    p = board.getPiece(new ChessPosition(1+i, 1+j));
                }
                outStr += ((i+j)/2*2==(i+j)?lightColor:darkColor);
                if(p == null){
                    outStr += "   ";
                }else{
                    char pieceIcon = ChessPiece.TYPE_TO_CHAR_MAP.get(p.getPieceType());
                    outStr += " " + (p.getTeamColor()==ChessGame.TeamColor.WHITE?
                                        lightPieceColor + Character.toUpperCase(pieceIcon):
                                        darkPieceColor + Character.toLowerCase(pieceIcon))
                                        + " ";
                }
            }
            outStr += borderColor + " " + ((team==ChessGame.TeamColor.BLACK)?1+i:8-i) + " "
                        + resetColor + "\n";
        }
        outStr += topNums;
        outStr += EscapeSequences.RESET_BG_COLOR + "\n";
        System.out.print(outStr);
    }

    static final String helpStr = """
            help output string will go here
            """;
    static final Map<ChessPiece.PieceType,String> whitePieceMap = Map.ofEntries(
            entry(ChessPiece.PieceType.PAWN,  EscapeSequences.WHITE_PAWN),
            entry(ChessPiece.PieceType.KNIGHT,EscapeSequences.WHITE_KNIGHT),
            entry(ChessPiece.PieceType.ROOK,  EscapeSequences.WHITE_ROOK),
            entry(ChessPiece.PieceType.QUEEN, EscapeSequences.WHITE_QUEEN),
            entry(ChessPiece.PieceType.KING,  EscapeSequences.WHITE_KING),
            entry(ChessPiece.PieceType.BISHOP,EscapeSequences.WHITE_BISHOP)
    );
    static final Map<ChessPiece.PieceType,String> blackPieceMap = Map.ofEntries(
            entry(ChessPiece.PieceType.PAWN,  EscapeSequences.BLACK_PAWN),
            entry(ChessPiece.PieceType.KNIGHT,EscapeSequences.BLACK_KNIGHT),
            entry(ChessPiece.PieceType.ROOK,  EscapeSequences.BLACK_ROOK),
            entry(ChessPiece.PieceType.QUEEN, EscapeSequences.BLACK_QUEEN),
            entry(ChessPiece.PieceType.KING,  EscapeSequences.BLACK_KING),
            entry(ChessPiece.PieceType.BISHOP,EscapeSequences.BLACK_BISHOP)
    );
}
