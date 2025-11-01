package dataaccess;

import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;

public class GameDAOTests {
    private static GameDAO gameDAO, daoEmpty;
    private static GameData goodData;

    @BeforeAll
    public static void init() {
        gameDAO = new GameDAO();
        daoEmpty = new GameDAO();
        goodData = new GameData(200,"whitePlayer","blackPlayer","correctGame",new ChessGame());
    }
    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO.create(goodData);
    }
    @AfterEach
    public void takeDown() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    public void clearTest() throws DataAccessException {
        gameDAO.clear();
        Assertions.assertEquals(daoEmpty, gameDAO);
    }

    @Test
    public void createTest() throws DataAccessException {
        Assertions.assertEquals(new GameData(gameDAO.getNextID(),null,null, goodData.gameName(), goodData.game()), gameDAO.get(gameDAO.getNextID()));
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
}
