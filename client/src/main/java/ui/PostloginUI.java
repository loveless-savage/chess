package ui;

import client.*;
import model.*;

import java.util.Objects;

public class PostloginUI {
    static final String helpStr = """
            FIXME: help output
            """;
    public static REPL.State parse(ServerFacade server, String cmdIn) {
        String[] cmd = cmdIn.split(" ",2);
        String[] args = cmd.length<2? null : cmd[1].split(" ");
        switch (cmd[0]) {
            case "help":
                System.out.println(helpStr);
                break;
            case "create":
                if (args == null || args.length != 1) {
                    System.out.println("create needs 1 argument");
                } else {
                    server.createGame(args[0]);
                }
                break;
            case "list":
                GameData[] gameList = server.listGames();
                for (GameData game : gameList) {
                    // TODO: gameIDs, unclaimed string
                    System.out.printf("[%d] %s\t(white=%s,black=%s)\n",
                            game.gameID(),game.gameName(),game.whiteUsername(),game.blackUsername());
                }
                break;
            case "join":
                if (args == null || args.length != 2) {
                    System.out.println("join needs 2 arguments");
                    break;
                }
                try {
                    Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("argument 1 must be a game ID number");
                    break;
                }
                if (!Objects.equals(args[1],"WHITE") && !Objects.equals(args[1],"BLACK")) {
                    System.out.println("argument 2 must be a team color [WHITE|BLACK]");
                    break;
                }
                server.joinGame(args);
                return REPL.State.GAMEPLAY;
            case "observe":
                if (args == null || args.length != 1) {
                    System.out.println("observe needs 1 argument");
                    break;
                }
                try {
                    Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("argument 1 must be a game ID number");
                    break;
                }
                return REPL.State.GAMEPLAY;
            case "logout":
                server.logout();
                return REPL.State.PRELOGIN;
            default:
                System.out.println("Input not understood. Type 'help' for available commands");
        }
        return REPL.State.POSTLOGIN;
    }
}
