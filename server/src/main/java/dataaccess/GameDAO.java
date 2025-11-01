package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class GameDAO extends MySQLDAO<GameData,Integer>{
    private int lastID = 1;

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
        String statement = "INSERT INTO " + tableName + " (gameName,game) values " + toSQL(data);
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.executeUpdate();
            var rs = preparedStatement.getGeneratedKeys();
            if (!rs.next()) {
                throw new DataAccessException("could not retrieve last generated gameID");
            }
            lastID = rs.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public int getLastID() {
        return lastID;
    }

    public GameData[] list() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("SELECT * FROM gameData");
            var rs = preparedStatement.executeQuery();
            ArrayList<GameData> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        new Gson().fromJson(rs.getString("game"),ChessGame.class)
                ));
            }
            return out.toArray(GameData[]::new);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    String toSQL(GameData data) {
        return "('" + data.gameName() + "','" + new Gson().toJson(data.game()) + "')";
    }
    String toSQLDiff(GameData data, GameData dataOld) throws DataAccessException {
        if (data.gameName() == null || data.game() == null) {
            throw new DataAccessException("no UserData fields can be null");
        }
        String out = "gameName = '" + data.gameName() + "'";
        if (dataOld.whiteUsername() == null && data.whiteUsername() != null) {
            out += ", whiteUsername = '" + data.whiteUsername() + "'";
        }
        if (dataOld.blackUsername() == null && data.blackUsername() != null) {
            out += ", blackUsername = '" + data.blackUsername() + "'";
        }
        if (!data.game().equals(dataOld.game())) {
            out += ", game = '" + new Gson().toJson(data.game()) + "'";
        }
        return out;
    }
    GameData fromSQL(ResultSet rs) throws SQLException {
        if(rs.next()) {
            return new GameData(
                    rs.getInt("gameID"),
                    rs.getString("whiteUsername"),
                    rs.getString("blackUsername"),
                    rs.getString("gameName"),
                    new Gson().fromJson(rs.getString("game"), ChessGame.class)
            );
        } else {
            return null;
        }
    }
}
