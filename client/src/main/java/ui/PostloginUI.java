package ui;

import client.*;

public class PostloginUI {
    static final String helpStr = """
            FIXME: help output
            """;
    public static REPL.State parse(ServerFacade server, String cmdIn) {
        String[] cmd = cmdIn.split(" ",2);
        String[] args = cmd[1].split(" ");
        switch (cmd[0]) {
            case "help":
                System.out.println(helpStr);
        }
        return REPL.State.POSTLOGIN;
    }
}
