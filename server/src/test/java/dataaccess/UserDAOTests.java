package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class UserDAOTests {
    private static UserDAO userDAO;
    private static UserData goodData;

    @BeforeAll
    public static void init() {
        userDAO = new UserDAO();
        goodData = new UserData("correctUsername",BCrypt.hashpw("correctPassword",BCrypt.gensalt()),"correct@email");
    }
    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO.clear();
        userDAO.create(goodData);
    }
    @AfterEach
    public void takeDown() throws DataAccessException {
        userDAO.clear();
    }

    @Test
    public void createTest() {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(
                    "SELECT * FROM userData WHERE username='correctUsername'"
            );
            var rs = preparedStatement.executeQuery();
            Assertions.assertTrue(rs.next(),"TABLE userData expected to have an entry, but does not");
            Assertions.assertEquals("correctUsername",rs.getString("username"));
            Assertions.assertTrue(BCrypt.checkpw("correctPassword",rs.getString("password")));
            Assertions.assertEquals("correct@email",rs.getString("email"));
        } catch (SQLException | DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void createAlreadyExistsTest() {
        Assertions.assertThrows(DataAccessException.class,() -> userDAO.create(goodData));
    }

    @Test
    public void createBadDataTest() {
        UserData nullEmail = new UserData("badUsername",BCrypt.hashpw("badPassword",BCrypt.gensalt()),null);
        Assertions.assertThrows(DataAccessException.class,() -> userDAO.create(nullEmail));
        UserData nullPass = new UserData("badUsername",null,"bad@email");
        Assertions.assertThrows(DataAccessException.class,() -> userDAO.create(nullPass));
        UserData badPass = new UserData("badUsername","badPassword","bad@email");
        Assertions.assertThrows(DataAccessException.class,() -> userDAO.create(badPass));
        UserData nullPassEmail = new UserData("badUsername",null,null);
        Assertions.assertThrows(DataAccessException.class,() -> userDAO.create(nullPassEmail));
    }

    @Test
    public void getTest() throws DataAccessException {
        Assertions.assertEquals(goodData, userDAO.get("correctUsername"));
    }

    @Test
    public void getWrongUsernameTest() throws DataAccessException {
        Assertions.assertNull(userDAO.get("badUsername"));
    }

    @Test
    public void updateTest() throws DataAccessException {
        UserData betterData = new UserData("correctUsername",BCrypt.hashpw("updatedPassword",BCrypt.gensalt()),"updated@email");
        userDAO.update(betterData);
        Assertions.assertNotEquals(goodData, userDAO.get("correctUsername"));
        Assertions.assertEquals(betterData, userDAO.get("correctUsername"));
    }

    @Test
    public void updateNoChangeTest() throws DataAccessException {
        Assertions.assertDoesNotThrow(() -> userDAO.update(goodData));
        Assertions.assertEquals(goodData, userDAO.get("correctUsername"));
    }

    @Test
    public void updateNotFoundTest() throws DataAccessException {
        UserData badData = new UserData("badUsername",BCrypt.hashpw("correctPassword",BCrypt.gensalt()),"correct@email");
        Assertions.assertThrows(DataAccessException.class,() -> userDAO.update(badData));
        Assertions.assertEquals(goodData, userDAO.get("correctUsername"));
    }

    @Test
    public void deleteTest() throws DataAccessException {
        UserData otherData = new UserData("otherUsername",BCrypt.hashpw("otherPassword",BCrypt.gensalt()),"other@email");
        userDAO.create(otherData);
        Assertions.assertEquals(goodData, userDAO.get("correctUsername"));
        Assertions.assertEquals(otherData, userDAO.get("otherUsername"));
        userDAO.delete("correctUsername");
        Assertions.assertNull(userDAO.get("correctUsername"));
        Assertions.assertEquals(otherData, userDAO.get("otherUsername"));
    }

    // TODO: deleteNegativeTest()

    @Test
    public void clearTest() throws DataAccessException {
        userDAO.clear();
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("SELECT * FROM userData");
            var rs = preparedStatement.executeQuery();
            Assertions.assertFalse(rs.next(),"TABLE userData should be empty, but is not");
        } catch (SQLException | DataAccessException e) {
            Assertions.fail(e);
        }
    }
}
