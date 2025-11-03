package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

public class AuthDAOTests {
    private static AuthDAO authDAO, daoEmpty;
    private static AuthData goodData;

    @BeforeAll
    public static void setup() {
        authDAO = new AuthDAO();
        daoEmpty = new AuthDAO();
        goodData = new AuthData("goodToken","goodUsername");
    }
    @BeforeEach
    public void setupEach() throws DataAccessException {
        authDAO.create(goodData);
    }
    @AfterEach
    public void takeDown() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    public void clearTest() throws DataAccessException {
        authDAO.clear();
        Assertions.assertEquals(daoEmpty, authDAO);
    }

    @Test
    public void createTest() throws DataAccessException {
        Assertions.assertEquals(goodData, authDAO.get("goodToken"));
    }

    @Test
    public void getTest() throws DataAccessException {
        Assertions.assertNull(authDAO.get("badToken"));
    }

    @Test
    public void updateTest() throws DataAccessException {
        AuthData betterData = new AuthData("goodToken","updatedUsername");
        authDAO.update(betterData);
        Assertions.assertNotEquals(goodData, authDAO.get("goodToken"));
        Assertions.assertEquals(betterData, authDAO.get("goodToken"));
    }

    @Test
    public void deleteTest() throws DataAccessException {
        AuthData otherData = new AuthData("otherToken","otherUsername");
        authDAO.create(otherData);
        Assertions.assertEquals(goodData, authDAO.get("goodToken"));
        Assertions.assertEquals(otherData, authDAO.get("otherToken"));
        authDAO.delete("goodToken");
        Assertions.assertNull(authDAO.get("goodToken"));
        Assertions.assertEquals(otherData, authDAO.get("otherToken"));
    }
}
