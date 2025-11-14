package ui;

import client.*;

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
                } else {
                    server.register(args);
                    return REPL.State.POSTLOGIN;
                }
            case "login":
                if (args == null || args.length != 2) {
                    System.out.println("login needs 2 arguments");
                } else {
                    server.login(args);
                    return REPL.State.POSTLOGIN;
                }
            default:
                System.out.println("Input not understood. Type 'help' for available commands");
        }
        return REPL.State.PRELOGIN;
    }
}
