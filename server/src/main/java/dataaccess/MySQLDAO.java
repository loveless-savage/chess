package dataaccess;

import model.ModelData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class MySQLDAO<T extends ModelData<K>,K> implements DAO<T,K> {
    final String tableName, tableParams, keyName;

    MySQLDAO(String tableNameIn, String tableParamsIn) {
        tableName = tableNameIn;
        tableParams = tableParamsIn;
        keyName = tableParams.split("PRIMARY KEY \\(")[1].split("\\)")[0];

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
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = toSQL(conn,data);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public T get(K key) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE " + keyName + " = ?");
            preparedStatement.setString(1,key.toString());
            var rs = preparedStatement.executeQuery();
            return fromSQL(rs);
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void update(T data) throws DataAccessException {
        T dataOld = get(data.keyValue());
        if (dataOld == null) {
            throw new DataAccessException("no entry could be found to update");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = toSQLDiff(conn,data);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void delete(K key) throws DataAccessException {
        String statement = "DELETE FROM " + tableName + " WHERE " + keyName + "='" + key + "'";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("DELETE FROM " + tableName);
            preparedStatement.execute();
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    abstract PreparedStatement toSQL(Connection conn, T data) throws DataAccessException;
    abstract PreparedStatement toSQLDiff(Connection conn, T data) throws DataAccessException;
    abstract T fromSQL(ResultSet rs) throws SQLException;
}
