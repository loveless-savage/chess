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
        goodData = new GameData(0,null,null,"correctGame",new ChessGame());
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
            Assertions.assertNull(rs.getString("whiteUsername"));
            Assertions.assertNull(rs.getString("blackUsername"));
            Assertions.assertEquals("correctGame",rs.getString("gameName"));
            Assertions.assertEquals(new ChessGame(),new Gson().fromJson(rs.getString("game"),ChessGame.class));
        } catch (SQLException | DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void getTest() throws DataAccessException {
        GameData returnedData = new GameData(gameDAO.getLastID(),null,null,"correctGame",new ChessGame());
        Assertions.assertEquals(returnedData,gameDAO.get(gameDAO.getLastID()));
    }

    @Test
    public void updateTest() throws DataAccessException {
        GameData betterData = new GameData(gameDAO.getLastID(),"newWhitePlayer","newBlackPlayer","updatedGame",new ChessGame());
        gameDAO.update(betterData);
        Assertions.assertNotEquals(goodData, gameDAO.get(gameDAO.getLastID()));
        Assertions.assertEquals(betterData, gameDAO.get(gameDAO.getLastID()));
    }

    @Test
    public void listTest() throws DataAccessException {
        Assertions.assertEquals(1,gameDAO.list().length);
        GameData otherData = new GameData(201,null,null,"otherGame",new ChessGame());
        gameDAO.create(otherData);
        GameData[] result = gameDAO.list();
        Assertions.assertEquals(2,result.length);
        Assertions.assertNull(result[0].whiteUsername());
        Assertions.assertNull(result[0].blackUsername());
        Assertions.assertEquals("correctGame",result[0].gameName());
        Assertions.assertEquals(new ChessGame(),result[0].game());
        Assertions.assertNull(result[1].whiteUsername());
        Assertions.assertNull(result[1].blackUsername());
        Assertions.assertEquals("otherGame",result[1].gameName());
        Assertions.assertEquals(new ChessGame(),result[1].game());
    }

    @Test
    public void deleteTest() throws DataAccessException {
        GameData otherData = new GameData(201,null,null,"otherGame",new ChessGame());
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
