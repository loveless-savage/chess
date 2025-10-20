package dataaccess;

import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;

public class GameDAOTests {
    private static GameDAO dao, daoEmpty;
    private static GameData goodData;

    @BeforeAll
    public static void setup() {
        dao = new GameDAO();
        daoEmpty = new GameDAO();
        goodData = new GameData(200,"whitePlayer","blackPlayer","correctGame",new ChessGame());
    }
    @BeforeEach
    public void setupEach() {
        dao.create(goodData);
    }
    @AfterEach
    public void takeDown() {
        dao.clear();
    }

    @Test
    public void clearTest() {
        dao.clear();
        Assertions.assertEquals(daoEmpty, dao);
    }

    @Test
    public void createTest() {
        Assertions.assertEquals(goodData, dao.get(200));
    }

    @Test
    public void getTest() {
        Assertions.assertNull(dao.get(201));
    }

    @Test
    public void updateTest() {
        GameData betterData = new GameData(200,"newWhitePlayer","newBlackPlayer","updatedGame",new ChessGame());
        dao.update(betterData);
        Assertions.assertNotEquals(goodData, dao.get(200));
        Assertions.assertEquals(betterData, dao.get(200));
    }

    @Test
    public void listTest() {
        Assertions.assertArrayEquals(new GameData[]{goodData}, dao.list());
        GameData otherData = new GameData(201,"alice","bob","otherGame",new ChessGame());
        dao.create(otherData);
        Assertions.assertArrayEquals(new GameData[]{goodData,otherData}, dao.list());
    }

    @Test
    public void deleteTest() {
        GameData otherData = new GameData(201,"alice","bob","otherGame",new ChessGame());
        dao.create(otherData);
        Assertions.assertEquals(goodData, dao.get(200));
        Assertions.assertEquals(otherData, dao.get(201));
        dao.delete(200);
        Assertions.assertNull(dao.get(200));
        Assertions.assertEquals(otherData, dao.get(201));
    }
}
