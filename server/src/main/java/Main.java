import chess.*;
import dataaccess.*;
import model.*;
import server.*;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);
    }
}