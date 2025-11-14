package client;

import ui.*;
import java.util.Scanner;

public class REPL {
    ServerFacade server = new ServerFacade();
    public enum State {
        PRELOGIN,
        POSTLOGIN,
        GAMEPLAY
    }
    State state = State.PRELOGIN;

    public void run() {
        System.out.println("Welcome to 240 chess!");
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.print(">>> ");
            String line = scanner.nextLine();
            if (line.equals("quit")) {
                if (state == State.POSTLOGIN) {
                    server.logout();
                }
                System.out.println("Goodbye.");
                return;
            }
            switch (state) {
                case PRELOGIN -> state = PreloginUI.parse(server, line);
                case POSTLOGIN -> state = PostloginUI.parse(server, line);
                case GAMEPLAY -> state = GameplayUI.parse(server, line);
            }
            System.out.println(line);
        }
    }
}
