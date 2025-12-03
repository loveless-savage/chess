package client;

import model.*;
import service.*;

public class ServerFacade {
    final HttpCommunicator http;
    String authToken = null;

    public ServerFacade(String hostIn, int portIn) {
        http = new HttpCommunicator(hostIn, portIn);
    }
    public ServerFacade() {
        http = new HttpCommunicator("localhost",8080);
    }

    public void clear() {
        http.clear();
        authToken = null;
    }

    public void register(String[] params) {
        http.register(params);
        authToken = http.getAuthToken();
    }

    public void login(String[] params) {
        http.login(params);
        authToken = http.getAuthToken();
    }

    public void logout() {
        http.logout();
        authToken = null;
    }

    public GameData[] listGames() {
        return http.listGames();
    }

    public int createGame(String param) {
        return http.createGame(param);
    }

    public void joinGame(String[] params) {
        http.joinGame(params);
    }

}
