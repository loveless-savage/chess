package dataaccess;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;
import java.sql.SQLException;

public class GameDAOTests {
    private static GameDAO gameDAO;
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
    public void createBadDataTest() {
        GameData nullName = new GameData(0,null,null,null,new ChessGame());
        Assertions.assertThrows(DataAccessException.class,() -> gameDAO.create(nullName));
        GameData nullGame = new GameData(0,null,null,"badGame",null);
        Assertions.assertThrows(DataAccessException.class,() -> gameDAO.create(nullGame));
        GameData nullNameGame = new GameData(0,null,null,null,null);
        Assertions.assertThrows(DataAccessException.class,() -> gameDAO.create(nullNameGame));
    }

    @Test
    public void getLastIDTest() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("SELECT * FROM gameData");
            var rs = preparedStatement.executeQuery();
            if(!rs.next()) {
                throw new DataAccessException("gameData should not be empty but it is");
            }
            Assertions.assertEquals(rs.getInt("gameID"),gameDAO.getLastID());
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @Test
    public void getTest() throws DataAccessException {
        GameData returnedData = new GameData(gameDAO.getLastID(),null,null,"correctGame",new ChessGame());
        Assertions.assertEquals(returnedData,gameDAO.get(gameDAO.getLastID()));
    }

    @Test
    public void getWrongIDTest() throws DataAccessException {
        Assertions.assertNull(gameDAO.get(gameDAO.getLastID()+1));
    }

    @Test
    public void updateTest() throws DataAccessException {
        GameData betterData = new GameData(gameDAO.getLastID(),"newWhitePlayer","newBlackPlayer","updatedGame",new ChessGame());
        gameDAO.update(betterData);
        Assertions.assertEquals(betterData, gameDAO.get(gameDAO.getLastID()));
    }

    @Test
    public void updateBadDataTest() {
        GameData nullName = new GameData(gameDAO.getLastID(),"newWhitePlayer","newBlackPlayer",null,new ChessGame());
        Assertions.assertThrows(DataAccessException.class,() -> gameDAO.update(nullName));
        GameData nullGame = new GameData(gameDAO.getLastID(),"newWhitePlayer","newBlackPlayer","badGame",null);
        Assertions.assertThrows(DataAccessException.class,() -> gameDAO.update(nullGame));
        GameData nullNameGame = new GameData(gameDAO.getLastID(),"newWhitePlayer","newBlackPlayer",null,null);
        Assertions.assertThrows(DataAccessException.class,() -> gameDAO.update(nullNameGame));
    }

    @Test
    public void listTest() throws DataAccessException {
        Assertions.assertEquals(1,gameDAO.list().length);
        GameData otherData = new GameData(0,null,null,"otherGame",new ChessGame());
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
    public void listCorruptedDataTest() throws DataAccessException {
        String statement = "INSERT INTO gameData (gameName,game) values (\"badData\",\"0\")";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
        Assertions.assertThrows(JsonSyntaxException.class,() -> gameDAO.list());
    }

    @Test
    public void deleteTest() throws DataAccessException {
        int goodID = gameDAO.getLastID();
        GameData otherData = new GameData(0,null,null,"otherGame",new ChessGame());
        gameDAO.create(otherData);
        int otherID = gameDAO.getLastID();

        Assertions.assertEquals(new GameData(goodID,null,null,"correctGame",new ChessGame()), gameDAO.get(goodID));
        Assertions.assertEquals(new GameData(otherID,null,null,"otherGame",new ChessGame()), gameDAO.get(otherID));
        gameDAO.delete(goodID);
        Assertions.assertNull(gameDAO.get(goodID));
        Assertions.assertEquals(new GameData(otherID,null,null,"otherGame",new ChessGame()), gameDAO.get(otherID));
    }

    @Test
    public void deleteNoDataTest() {
        Assertions.assertDoesNotThrow(() -> gameDAO.delete(gameDAO.getLastID()+1));
    }

    @Test
    public void clearTest() throws DataAccessException {
        gameDAO.clear();
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("SELECT * FROM gameData");
            var rs = preparedStatement.executeQuery();
            Assertions.assertFalse(rs.next());
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
