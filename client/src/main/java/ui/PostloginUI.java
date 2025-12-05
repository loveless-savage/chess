package ui;

import client.*;
import model.*;
import service.*;

public class PostloginUI {
    public static REPL.State parse(HttpFacade server, String cmdIn) {
        String[] cmd = cmdIn.split(" ",2);
        String[] args = cmd.length<2? null : cmd[1].split(" ");
        switch (cmd[0]) {
            case "help":
                System.out.print(HELP_STR);
                break;
            case "create":
                if (args == null || args.length != 1) {
                    System.out.println("create needs 1 argument");
                    break;
                }
                return tryWithErrorDialog(() -> {
                    server.createGame(args[0]);
                    return REPL.State.POSTLOGIN;
                });
            case "list":
                return tryWithErrorDialog(() -> {
                    GameData[] gameList = server.listGames();
                    gameIDs = new int[gameList.length];
                    if (gameList.length == 0) {
                        System.out.println("No games exist");
                    } else {
                        printGames(gameList);
                    }
                    return REPL.State.POSTLOGIN;
                });
            case "join":
                if (args == null || args.length != 2) {
                    System.out.println("join needs 2 arguments");
                    break;
                }
                int idx;
                try {
                    idx = Integer.parseInt(args[0]);
                    if (idx < 1 || idx > gameIDs.length) {
                        System.out.println(BAD_GAMEID_STR);
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Argument 1 must be a game ID number");
                    break;
                }
                String teamColor = args[1].toUpperCase();
                if (!teamColor.equals("WHITE") && !teamColor.equals("BLACK")) {
                    String outStr = "Argument 2 must be a team color "
                            + EscapeSequences.SET_TEXT_COLOR_YELLOW
                            + "[WHITE|BLACK]"
                            + EscapeSequences.RESET_TEXT_COLOR;
                    System.out.println(outStr);
                    break;
                }
                return tryWithErrorDialog(() -> {
                    server.joinGame(new String[]{
                            String.valueOf(gameIDs[idx-1]),teamColor});
                    return REPL.State.GAMEPLAY;
                });
            case "observe":
                if (args == null || args.length != 1) {
                    System.out.println("observe needs 1 argument");
                    break;
                }
                try {
                    idx = Integer.parseInt(args[0]);
                    if (idx < 1 || idx > gameIDs.length) {
                        System.out.println(BAD_GAMEID_STR);
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Argument 1 must be a game ID number");
                    break;
                }
                return REPL.State.GAMEPLAY;
            case "logout":
                try {
                    server.logout();
                    return REPL.State.PRELOGIN;
                } catch (UnauthorizedException e) {
                    System.out.println("Unauthorized. You might already be logged out");
                } catch (ServerException e) {
                    System.out.println("Server error. Check the network logs for more details");
                }
                break;
            case "register":
                System.out.println("Log out before you can register as a new user");
                break;
            case "login":
                System.out.println("Already logged in");
                break;
            default:
                System.out.println(BAD_REQUEST_STR);
        }
        return REPL.State.POSTLOGIN;
    }

    public static void printGames(GameData[] gameList) {
        for (int i=0; i<gameList.length; i++) {
            GameData game = gameList[i];
            gameIDs[i] = game.gameID();
            String listStr = EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "[%d]"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " %s\t(white=%s,black=%s)\n";
            String nullUserStr = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY
                    + "unclaimed"
                    + EscapeSequences.RESET_TEXT_COLOR;
            System.out.printf(listStr,
                    i+1,
                    game.gameName(),
                    game.whiteUsername()==null? nullUserStr:game.whiteUsername(),
                    game.blackUsername()==null? nullUserStr:game.blackUsername()
            );
        }
    }

    interface TryArg { REPL.State run(); }
    private static REPL.State tryWithErrorDialog(PreloginUI.TryArg tryArg) {
        try {
            return tryArg.run();
        } catch (BadRequestException e) {
            System.out.println(BAD_REQUEST_STR);
        } catch (AlreadyTakenException e) {
            System.out.println("That position is taken");
        } catch (UnauthorizedException e) {
            System.out.println("Unauthorized. Are you logged in?");
        } catch (ServerException e) {
            System.out.println("Server error. Check the network logs for more details");
        }
        return REPL.State.POSTLOGIN;
    }

    private static final String HELP_STR =
            EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "create <name>"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - create a game\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "list"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - existing games and their players\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "join <ID> [WHITE|BLACK]"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - play a game \n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "observe <ID>"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - watch a game being played\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "logout"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - logout\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "quit"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - logout and exit the program\n"
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "help"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " - list commands\n";
    private static final String BAD_REQUEST_STR =
            "Input not understood. Type "
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "help"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " for available commands";
    private static final String BAD_GAMEID_STR =
            "Pick a valid game ID number, as reported by the "
                    + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "list"
                    + EscapeSequences.RESET_TEXT_COLOR
                    + " command";
    static int[] gameIDs = new int[0];
}
