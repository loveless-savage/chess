package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

public class UserDAOTests {
    private static UserDAO dao, daoEmpty;
    private static UserData goodData;

    @BeforeAll
    public static void init() {
        dao = new UserDAO();
        daoEmpty = new UserDAO();
        goodData = new UserData("correctUsername","correctPassword","correct@email");
    }
    @BeforeEach
    public void setup() {
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
        Assertions.assertEquals(goodData, dao.get("correctUsername"));
    }

    @Test
    public void getTest() {
        Assertions.assertNull(dao.get("badUsername"));
    }

    @Test
    public void updateTest() {
        UserData betterData = new UserData("correctUsername","updatedPassword","updated@email");
        dao.update(betterData);
        Assertions.assertNotEquals(goodData, dao.get("correctUsername"));
        Assertions.assertEquals(betterData, dao.get("correctUsername"));
    }

    @Test
    public void deleteTest() {
        UserData otherData = new UserData("otherUsername","otherPassword","other@email");
        dao.create(otherData);
        Assertions.assertEquals(goodData, dao.get("correctUsername"));
        Assertions.assertEquals(otherData, dao.get("otherUsername"));
        dao.delete("correctUsername");
        Assertions.assertNull(dao.get("correctUsername"));
        Assertions.assertEquals(otherData, dao.get("otherUsername"));
    }
}
