package dataaccess;

import model.ModelData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class MySQLDAO<T extends ModelData<K>,K> implements DAO<T,K> {
    final String tableName, keyName, tableParams;

    MySQLDAO(String tableNameIn, String keyNameIn, String tableParamsIn) {
        tableName = tableNameIn;
        keyName = keyNameIn;
        tableParams = tableParamsIn;

        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                var preparedStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableParams + ")");
                preparedStatement.execute();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
        String statement = "SELECT * FROM " + tableName + " WHERE " + keyName + "='" + key + "'";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            var rs = preparedStatement.executeQuery();
            return fromSQL(rs);
        } catch (SQLException | DataAccessException e) {
            return null;
            //throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void update(T data) {
    }

    public void delete(K key) {
    }

    public void clear() {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("DELETE FROM " + tableName);
            preparedStatement.execute();
        } catch (SQLException | DataAccessException e) {
            //throw new DataAccessException(e.getMessage(), e);
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

    abstract T fromSQL(ResultSet rs) throws SQLException;
}
