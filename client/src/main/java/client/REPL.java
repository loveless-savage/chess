package client;

import chess.ChessGame;
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
        PreloginUI.parse(null,"help");
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.print(state==State.PRELOGIN? "[LOGGED OUT] >>> " : ">>> ");
            String line = scanner.nextLine();
            if (line.equals("quit")) {
                if (state != State.PRELOGIN) {
                    server.logout();
                }
                System.out.println("Goodbye.");
                return;
            }
            switch (state) {
                case PRELOGIN:
                    state = PreloginUI.parse(server, line);
                    break;
                case POSTLOGIN:
                    state = PostloginUI.parse(server, line);
                    if (state == State.GAMEPLAY) {
                        // replace with websocket
                        if (line.startsWith("join") && line.split(" ")[2].equalsIgnoreCase("BLACK")) {
                            GameplayUI.printBoard(new ChessGame(), ChessGame.TeamColor.BLACK);
                        } else {
                            GameplayUI.printBoard(new ChessGame(), ChessGame.TeamColor.WHITE);
                        }
                        state = State.POSTLOGIN;
                    }
                    break;
                case GAMEPLAY:
                    state = GameplayUI.parse(server, line);
                    break;
            }
        }
    }
}
