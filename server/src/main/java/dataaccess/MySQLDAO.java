package dataaccess;

import model.ModelData;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class MySQLDAO<T extends ModelData<K>,K> implements DAO<T,K> {
    final String tableName, tableParams;

    MySQLDAO(String tableNameIn, String tableParamsIn) {
        tableName = tableNameIn;
        tableParams = tableParamsIn;
        /*
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableParams + ")");
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
        */
    }

    public void create(T data) throws DataAccessException {
        String statement = "INSERT INTO " + tableName + " values " + toValues(data);
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public T get(K key) {
        return null;
    }

    public void update(T data) {
    }

    public void delete(K key) {
    }

    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("DELETE FROM " + tableName);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private String toValues(T o) {
        String out = "('";
        String[] f = o.toString().split("[\\[=,\\]]");
        ArrayList<String> fnew = new ArrayList<>();
        for (int i=2; i<f.length; i+=2) {
            fnew.add(f[i]);
        }
        out += String.join("','",fnew);
        out += "')";
        return out;
    }
}
