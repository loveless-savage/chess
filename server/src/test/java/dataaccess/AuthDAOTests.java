package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.UUID;

public class AuthDAOTests {
    private static AuthDAO authDAO, daoEmpty;
    private static AuthData goodData;

    @BeforeAll
    public static void setup() {
        authDAO = new AuthDAO();
        daoEmpty = new AuthDAO();
        goodData = new AuthData(UUID.randomUUID().toString(),"correctUsername");
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
    public void createTest() {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(
                    "SELECT * FROM authData WHERE authToken='" + goodData.authToken() + "'"
            );
            var rs = preparedStatement.executeQuery();
            Assertions.assertTrue(rs.next(),"TABLE userData expected to have an entry, but does not");
            Assertions.assertEquals(goodData.authToken(),rs.getString("authToken"));
            Assertions.assertEquals("correctUsername",rs.getString("username"));
        } catch (SQLException | DataAccessException e) {
            Assertions.fail(e);
        }
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

    @Test
    public void clearTest() throws DataAccessException {
        authDAO.clear();
        Assertions.assertEquals(daoEmpty, authDAO);
    }
}
