package ui;

import chess.ChessGame;
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
        String gameRaw = game.toString().split("\n",2)[1];
        char[] gameChars = gameRaw.toCharArray();
        String lightColor = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String darkColor = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        boolean isSquareLight = false;
        String outStr = "";
        for (char c : gameChars) {
            if(c == '\n') {
                outStr += EscapeSequences.RESET_BG_COLOR + "\n";
                continue;
            } else if(c == '|') {
                isSquareLight = !isSquareLight;
                continue;
            }
            outStr += (isSquareLight?lightColor:darkColor)
                      + chessPrintMap.get(c);
        }
        outStr += EscapeSequences.RESET_BG_COLOR + "\n";
        System.out.print(outStr);
    }

    static final String helpStr = """
            help output string will go here
            """;
    static final Map<Character,String> chessPrintMap = Map.ofEntries(
            entry('P',EscapeSequences.WHITE_PAWN),
            entry('N',EscapeSequences.WHITE_KNIGHT),
            entry('R',EscapeSequences.WHITE_ROOK),
            entry('Q',EscapeSequences.WHITE_QUEEN),
            entry('K',EscapeSequences.WHITE_KING),
            entry('B',EscapeSequences.WHITE_BISHOP),
            entry('p',EscapeSequences.BLACK_PAWN),
            entry('n',EscapeSequences.BLACK_KNIGHT),
            entry('r',EscapeSequences.BLACK_ROOK),
            entry('q',EscapeSequences.BLACK_QUEEN),
            entry('k',EscapeSequences.BLACK_KING),
            entry('b',EscapeSequences.BLACK_BISHOP),
            entry(' ',EscapeSequences.EMPTY),
            entry('|',"")
    );
}
