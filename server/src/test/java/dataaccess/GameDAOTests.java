package dataaccess;

import chess.ChessGame;
import model.GameData;
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
        Assertions.assertEquals(dao, daoEmpty);
    }

    @Test
    public void createTest() {
        Assertions.assertEquals(dao.get(200),goodData);
    }

    @Test
    public void getTest() {
        Assertions.assertNull(dao.get(201));
    }

    @Test
    public void updateTest() {
        GameData betterData = new GameData(200,"newWhitePlayer","newBlackPlayer","updatedGame",new ChessGame());
        dao.update(betterData);
        Assertions.assertNotEquals(dao.get(200),goodData);
        Assertions.assertEquals(dao.get(200),betterData);
    }

    @Test
    public void listTest() {
        Assertions.assertArrayEquals(dao.list(),new GameData[]{goodData});
        GameData otherData = new GameData(201,"alice","bob","otherGame",new ChessGame());
        dao.create(otherData);
        Assertions.assertArrayEquals(dao.list(),new GameData[]{goodData,otherData});
    }
}
