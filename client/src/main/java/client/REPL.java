package client;

import chess.ChessGame;
import ui.*;
import java.util.Scanner;

public class REPL {
    HttpFacade httpServer = new HttpFacade();
    GameplayUI gameplayUI = new GameplayUI();

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
                    httpServer.logout();
                }
                System.out.println("Goodbye.");
                return;
            }
            switch (state) {
                case PRELOGIN:
                    state = PreloginUI.parse(httpServer, line);
                    break;
                case POSTLOGIN:
                    state = PostloginUI.parse(httpServer, line);
                    if (state == State.GAMEPLAY) {
                        ChessGame.TeamColor team = ChessGame.TeamColor.WHITE; // FIXME
                        gameplayUI.open(team);
                        // replace with websocket
                        if (line.startsWith("join") && line.split(" ")[2].equalsIgnoreCase("BLACK")) {
                            GameplayUI.printBoard(new ChessGame(), ChessGame.TeamColor.BLACK);
                        } else {
                            GameplayUI.printBoard(new ChessGame(), ChessGame.TeamColor.WHITE);
                        }
                    }
                    break;
                case GAMEPLAY:
                    if (!gameplayUI.isOpen()) {
                        System.out.println("No game is open.");
                        state = State.POSTLOGIN;
                        break;
                    }
                    state = gameplayUI.parse(line);
            }
        }
    }
}
