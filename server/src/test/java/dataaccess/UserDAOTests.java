package dataaccess;

import model.*;
import org.junit.jupiter.api.*;

public class UserDAOTests {
    private static UserDAO dao, daoEmpty;
    private static UserData goodData;

    @BeforeAll
    public static void setup() {
        dao = new UserDAO();
        daoEmpty = new UserDAO();
        goodData = new UserData("correctUsername","correctPassword","correct@email");
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
        Assertions.assertEquals(dao.get("correctUsername"),goodData);
    }

    @Test
    public void getTest() {
        Assertions.assertNull(dao.get("badUsername"));
    }

    @Test
    public void updateTest() {
        UserData betterData = new UserData("correctUsername","updatedPassword","updated@email");
        dao.update(betterData);
        Assertions.assertNotEquals(dao.get("correctUsername"),goodData);
        Assertions.assertEquals(dao.get("correctUsername"),betterData);
    }
}
