package ui;

import client.*;
import service.*;

public class PreloginUI {
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
            case "register":
                if (args == null || args.length != 3) {
                    System.out.println("register needs 3 arguments");
                    break;
                }
                try {
                    server.register(args);
                    return REPL.State.POSTLOGIN;
                } catch (BadRequestException e) {
                    System.out.println("Input not understood. Type 'help' for available commands");
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
                    System.out.println("Input not understood. Type 'help' for available commands");
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
                System.out.println("Input not understood. Type 'help' for available commands");
        }
        return REPL.State.PRELOGIN;
    }
}
