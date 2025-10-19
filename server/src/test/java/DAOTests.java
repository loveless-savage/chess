import model.*;
import dataaccess.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DAOTests {
    @ParameterizedTest
    @ValueSource(strings = {"UserDAO", "AuthDAO", "GameDAO"})
    public void createTest(String daoType) throws Exception {
        try {
            var dao = Class.forName("dataaccess."+daoType).newInstance();
            Assertions.assertNotNull(dao);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.out.println(e);
        }
    }
}
