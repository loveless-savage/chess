package ui;

import client.*;
import service.*;

public class PreloginUI {
    private static final String helpStr =
              EscapeSequences.SET_TEXT_COLOR_YELLOW
            + "register <username> <password> <email>"
            + EscapeSequences.RESET_TEXT_COLOR
            + " - create an account\n"
            + EscapeSequences.SET_TEXT_COLOR_YELLOW
            + "login <username> <password>"
            + EscapeSequences.RESET_TEXT_COLOR
            + " - login to see and play chess games\n"
            + EscapeSequences.SET_TEXT_COLOR_YELLOW
            + "quit"
            + EscapeSequences.RESET_TEXT_COLOR
            + " - logout and exit the program\n"
            + EscapeSequences.SET_TEXT_COLOR_YELLOW
            + "help"
            + EscapeSequences.RESET_TEXT_COLOR
            + " - list commands\n";
    private static final String badRequestStr =
              "Input not understood. Type "
            + EscapeSequences.SET_TEXT_COLOR_YELLOW
            + "help"
            + EscapeSequences.RESET_TEXT_COLOR
            + " for available commands";

    public static REPL.State parse(ServerFacade server, String cmdIn) {
        String[] cmd = cmdIn.split(" ",2);
        String[] args = cmd.length<2? null : cmd[1].split(" ");
        switch (cmd[0]) {
            case "help":
                System.out.print(helpStr);
                break;
            case "register":
                if (args == null || args.length != 3) {
                    System.out.println("register needs 3 arguments");
                    break;
                }
                try {
                    server.register(args);
                    return REPL.State.POSTLOGIN;
                } catch (BadRequestException e) {
                    System.out.println(badRequestStr);
                } catch (AlreadyTakenException e) {
                    System.out.println("Username taken");
                } catch (ServerException e) {
                    System.out.println("Server error. Check the network logs for more details");
                }
                break;
            case "login":
                if (args == null || args.length != 2) {
                    System.out.println("login needs 2 arguments");
                    break;
                }
                try {
                    server.login(args);
                    return REPL.State.POSTLOGIN;
                } catch (BadRequestException e) {
                    System.out.println(badRequestStr);
                } catch (UnauthorizedException e) {
                    System.out.println("Unauthorized. Check your username and/or password");
                } catch (ServerException e) {
                    System.out.println("Server error. Check the network logs for more details");
                }
                break;
            case "create":
            case "list":
            case "join":
            case "observe":
                System.out.println("You need to log in first");
                break;
            case "logout":
                System.out.println("Not logged in");
                break;
            default:
                System.out.println(badRequestStr);
        }
        return REPL.State.PRELOGIN;
    }
}
