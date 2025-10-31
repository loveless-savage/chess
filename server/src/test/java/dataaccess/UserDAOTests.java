package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

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
    public void setup() throws DataAccessException {
        dao.clear();
        dao.create(goodData);
    }
    @AfterEach
    public void takeDown() throws DataAccessException {
        dao.clear();
    }

    @Test
    public void createTest() {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(
                    "SELECT * FROM userData WHERE username='correctUsername'"
            );
            var rs = preparedStatement.executeQuery();
            Assertions.assertTrue(rs.next());
            Assertions.assertEquals("correctUsername",rs.getString("username"));
            Assertions.assertEquals("correctPassword",rs.getString("password"));
            Assertions.assertEquals("correct@email",rs.getString("email"));
        } catch (SQLException | DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void createWithNullValuesTest() throws DataAccessException {
        UserData nullEmail = new UserData("badUsername","badPassword",null);
        Assertions.assertThrows(DataAccessException.class, () -> dao.create(nullEmail));
        UserData nullPass = new UserData("badUsername",null,"bad@email");
        Assertions.assertThrows(DataAccessException.class, () -> dao.create(nullPass));
        UserData nullPassEmail = new UserData("badUsername",null,null);
        Assertions.assertThrows(DataAccessException.class, () -> dao.create(nullPassEmail));
    }

    @Test
    public void getTest() throws DataAccessException {
        Assertions.assertEquals(goodData,dao.get("correctUsername"));
    }

    @Test
    public void getWrongUsernameTest() throws DataAccessException {
        Assertions.assertNull(dao.get("badUsername"));
    }

    @Test
    public void updateTest() throws DataAccessException {
        UserData betterData = new UserData("correctUsername","updatedPassword","updated@email");
        dao.update(betterData);
        Assertions.assertNotEquals(goodData, dao.get("correctUsername"));
        Assertions.assertEquals(betterData, dao.get("correctUsername"));
    }

    @Test
    public void deleteTest() throws DataAccessException {
        UserData otherData = new UserData("otherUsername","otherPassword","other@email");
        dao.create(otherData);
        Assertions.assertEquals(goodData, dao.get("correctUsername"));
        Assertions.assertEquals(otherData, dao.get("otherUsername"));
        dao.delete("correctUsername");
        Assertions.assertNull(dao.get("correctUsername"));
        Assertions.assertEquals(otherData, dao.get("otherUsername"));
    }

    @Test
    public void clearTest() throws DataAccessException {
        dao.clear();
        Assertions.assertEquals(daoEmpty, dao); // FIXME
    }
}
