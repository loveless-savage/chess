package dataaccess;

import model.*;

public interface DAO<T extends ModelData<K>,K> {
    void create(T data) throws DataAccessException;
    T get(K key) throws DataAccessException;
    void update(T data) throws DataAccessException;
    void delete(K key) throws DataAccessException;
    void clear() throws DataAccessException;
}
