package dataaccess;

import com.google.gson.Gson;
import model.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GameDAO extends MySQLDAO<GameData,Integer>{
    private int nextID = 1;
    public GameDAO() {
        super("gameData", """
                             gameID INT NOT NULL AUTO_INCREMENT,
                             whiteUsername VARCHAR(32),
                             blackUsername VARCHAR(32),
                             gameName VARCHAR(32) NOT NULL,
                             game TEXT NOT NULL,
                             PRIMARY KEY (gameID)
                             """);
    }

    public void create(GameData data) throws DataAccessException {
        super.create(data);
        nextID++;
    }
    public int getNextID() {
        return nextID;
    }
    public void clear() throws DataAccessException {
        super.clear();
        nextID = 1;
    }

    public GameData[] list() throws DataAccessException {
        return null;
    }

    String toSQL(GameData data) throws DataAccessException {
        return "FIXME";
    }
    String toSQLDiff(GameData data, GameData dataOld) throws DataAccessException {
        return "FIXME";
    }
    GameData fromSQL(ResultSet rs) throws SQLException {
        return null;
    }
}
