package ui;

import chess.*;
import client.*;
import websocket.messages.*;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class GameplayUI implements NotificationHandler {
    WebsocketFacade ws = null;
    String authToken;
    int gameID;
    ChessGame.TeamColor team;
    ChessGame gameCache = null;

    public void open(String authToken, int gameID, ChessGame.TeamColor team) {
        this.authToken = authToken;
        this.gameID = gameID;
        System.out.println("Connecting to the server...");
        try {
            ws = new WebsocketFacade(this);
            this.team = team;
            ws.connect(authToken,gameID);
        } catch (Exception e) {
            System.out.println("Failed to open a connection. Is the server running?");
        }
    }

    public boolean isOpen() {
        return ws.session.isOpen();
    }

    public void close() {
        try {
            ws.session.close();
        } catch (IOException e) {
            System.out.println(SERVER_ERROR_STR);
        }
    }

    public REPL.State parse(String cmdIn) {
        String[] cmd = cmdIn.split(" ",2);
        String[] args = cmd.length<2? null : cmd[1].split(" ");
        switch (cmd[0]) {
            case "help":
                System.out.println(HELP_STR);
                break;
            case "redraw":
            case "r":
                printBoard(gameCache, team);
                break;
            case "leave":
                System.out.println("Leaving the game.");
                try {
                    ws.leave(authToken,gameID);
                } catch (IOException e) {
                    System.out.println(SERVER_ERROR_STR);
                }
                close();
                return REPL.State.POSTLOGIN;
            case "move":
                // TODO: gameover, turn
                if (args == null || args.length<2) {
                    System.out.println("move needs 2 position arguments");
                    break;
                }
                ChessMove move;
                try {
                    move = new ChessMove(parsePos(args[0]), parsePos(args[1]));
                } catch (InvalidMoveException e) {
                    System.out.println(e.getLocalizedMessage());
                    break;
                }
                try {
                    ws.makeMove(move,authToken,gameID);
                } catch (IOException e) {
                    System.out.println(SERVER_ERROR_STR);
                }
                break;
            case "resign":
                // TODO: prompt confirmation, then forfeit
                try {
                    ws.resign(authToken,gameID);
                } catch (IOException e) {
                    System.out.println(SERVER_ERROR_STR);
                }
                break;
            case "highlight":
            case "h":
                // TODO: gameover
                if (args == null || args.length<1) {
                    System.out.println("highlight needs 1 position argument");
                    break;
                }
                ChessPosition focusPos;
                try {
                    focusPos = parsePos(args[0]);
                } catch (InvalidMoveException e) {
                    System.out.println(e.getMessage());
                    break;
                }
                Collection<ChessMove> moves = gameCache.validMoves(focusPos);
                if (moves == null || moves.isEmpty()) {
                    printBoard(gameCache, team, focusPos, new HashSet<>());
                    // TODO: no piece there
                    System.out.println("This piece cannot move.");
                    // TODO: "Note that this is not your piece"
                } else {
                    Collection<ChessPosition> targets = moves.stream().map(ChessMove::getEndPosition).collect(Collectors.toSet());
                    printBoard(gameCache,team,focusPos,targets);
                }
        }
        return REPL.State.GAMEPLAY;
    }

    @Override
    public void loadGame(ChessGame game) {
        gameCache = game;
        System.out.print("\n");
        printBoard(game,team);
        System.out.print(">>> ");
    }
    @Override
    public void sendError(ErrorMessage errorMsg) {
        System.out.print("\n");
        System.out.println(errorMsg.getErrorMessage());
        System.out.print(">>> ");
    }
    @Override
    public void notify(NotificationMessage notification) {
        System.out.print("\n");
        System.out.println(notification.getMessage());
        System.out.print(">>> ");
    }

    private static ChessPosition parsePos(String posIn) throws InvalidMoveException {
        if(posIn==null || posIn.length() != 2) {
            throw new InvalidMoveException(posIn+" is not a valid board position");
        }
        int row = posIn.charAt(1)-'0';
        int col = 1+Character.toLowerCase(posIn.charAt(0))-'a';
        if(row<1 || row>8 || col<1 || col>8) {
            throw new InvalidMoveException(posIn+" is not a valid board position");
        }
        return new ChessPosition(row,col);
    }

    public static void printBoard(ChessGame game, ChessGame.TeamColor team) {
        printBoard(game, team, new ChessPosition(0,0), new HashSet<>());
    }
    public static void printBoard(ChessGame game, ChessGame.TeamColor team, ChessPosition focusPos, Collection<ChessPosition> moves) {
        ChessBoard board = game.getBoard();
        String borderColor = EscapeSequences.SET_BG_COLOR_WHITE + EscapeSequences.SET_TEXT_COLOR_BLACK;
        String lightColor = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String darkColor = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        String focusHighlightColor = EscapeSequences.SET_BG_COLOR_BLUE;
        String lightHighlightColor = EscapeSequences.SET_BG_COLOR_GREEN;
        String darkHighlightColor = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
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
                ChessPosition pos;
                if (team == ChessGame.TeamColor.BLACK) {
                    pos = new ChessPosition(1+i, 8-j);
                } else {
                    pos = new ChessPosition(8-i, 1+j);
                }
                chess.ChessPiece p = board.getPiece(pos);
                if (pos.equals(focusPos)) {
                    outStr += focusHighlightColor;
                } else if (moves.contains(pos)) {
                    outStr += ((i + j) / 2 * 2 == (i + j) ? lightHighlightColor : darkHighlightColor);
                } else {
                    outStr += ((i + j) / 2 * 2 == (i + j) ? lightColor : darkColor);
                }
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
        outStr += EscapeSequences.RESET_BG_COLOR;
        System.out.print(outStr);
    }

    private static final String HELP_STR =
            EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "redraw|r"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - redraw chess board\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "leave"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - leave the game\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "move <startPos> <endPos>"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - move relevant piece. Format arguments like 'g4' or 'a6'\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "resign"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - forfeit the game. Does not leave\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "highlight|h <startPos>"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - highlight legal moves for selected piece. Format argument like 'g4' or 'a6'\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "help"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - list commands\n";
    private static final String SERVER_ERROR_STR = "There was an error trying to close the gameplay connection. Check the server.";
}
