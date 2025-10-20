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
        Assertions.assertEquals(daoEmpty, dao);
    }

    @Test
    public void createTest() {
        Assertions.assertEquals(goodData, dao.get("goodToken"));
    }

    @Test
    public void getTest() {
        Assertions.assertNull(dao.get("badToken"));
    }

    @Test
    public void updateTest() {
        AuthData betterData = new AuthData("goodToken","updatedUsername");
        dao.update(betterData);
        Assertions.assertNotEquals(goodData, dao.get("goodToken"));
        Assertions.assertEquals(betterData, dao.get("goodToken"));
    }

    @Test
    public void deleteTest() {
        AuthData otherData = new AuthData("otherToken","otherUsername");
        dao.create(otherData);
        Assertions.assertEquals(goodData, dao.get("goodToken"));
        Assertions.assertEquals(otherData, dao.get("otherToken"));
        dao.delete("goodToken");
        Assertions.assertNull(dao.get("goodToken"));
        Assertions.assertEquals(otherData, dao.get("otherToken"));
    }
}
