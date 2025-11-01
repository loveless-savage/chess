package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;
import java.sql.SQLException;

public class GameDAOTests {
    private static GameDAO gameDAO, daoEmpty;
    private static GameData goodData;

    @BeforeAll
    public static void init() {
        gameDAO = new GameDAO();
        goodData = new GameData(200,"whitePlayer","blackPlayer","correctGame",new ChessGame());
    }
    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO.clear();
        gameDAO.create(goodData);
    }
    @AfterEach
    public void takeDown() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    public void createTest() {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(
                    "SELECT * FROM gameData WHERE gameID=" + gameDAO.getLastID()
            );
            var rs = preparedStatement.executeQuery();
            Assertions.assertTrue(rs.next(),"TABLE gameData expected to have an entry, but does not");
            Assertions.assertEquals("whitePlayer",rs.getString("whiteUsername"));
            Assertions.assertEquals("blackPlayer",rs.getString("blackUsername"));
            Assertions.assertEquals("correctGame",rs.getString("gameName"));
            Assertions.assertEquals(new ChessGame(),new Gson().fromJson(rs.getString("game"),ChessGame.class));
        } catch (SQLException | DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void getTest() throws DataAccessException {
        Assertions.assertNull(gameDAO.get(201));
    }

    @Test
    public void updateTest() throws DataAccessException {
        GameData betterData = new GameData(200,"newWhitePlayer","newBlackPlayer","updatedGame",new ChessGame());
        gameDAO.update(betterData);
        Assertions.assertNotEquals(goodData, gameDAO.get(200));
        Assertions.assertEquals(betterData, gameDAO.get(200));
    }

    @Test
    public void listTest() throws DataAccessException {
        Assertions.assertArrayEquals(new GameData[]{goodData}, gameDAO.list());
        GameData otherData = new GameData(201,"alice","bob","otherGame",new ChessGame());
        gameDAO.create(otherData);
        Assertions.assertArrayEquals(new GameData[]{goodData,otherData}, gameDAO.list());
    }

    @Test
    public void deleteTest() throws DataAccessException {
        GameData otherData = new GameData(201,"alice","bob","otherGame",new ChessGame());
        gameDAO.create(otherData);
        Assertions.assertEquals(goodData, gameDAO.get(200));
        Assertions.assertEquals(otherData, gameDAO.get(201));
        gameDAO.delete(200);
        Assertions.assertNull(gameDAO.get(200));
        Assertions.assertEquals(otherData, gameDAO.get(201));
    }

    @Test
    public void clearTest() throws DataAccessException {
        gameDAO.clear();
        Assertions.assertEquals(daoEmpty, gameDAO);
    }
}
