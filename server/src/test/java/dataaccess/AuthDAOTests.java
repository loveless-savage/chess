package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

public class AuthDAOTests {
    private static AuthDAO dao, daoEmpty;
    private static AuthData goodData;

    @BeforeAll
    public static void setup() {
        dao = new AuthDAO();
        daoEmpty = new AuthDAO();
        goodData = new AuthData("goodToken","goodUsername");
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
        Assertions.assertEquals(dao.get("goodToken"),goodData);
    }

    @Test
    public void getTest() {
        Assertions.assertNull(dao.get("badToken"));
    }

    @Test
    public void updateTest() {
        AuthData betterData = new AuthData("goodToken","updatedUsername");
        dao.update(betterData);
        Assertions.assertNotEquals(dao.get("goodToken"),goodData);
        Assertions.assertEquals(dao.get("goodToken"),betterData);
    }
}
