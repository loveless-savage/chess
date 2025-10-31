package dataaccess;

import model.*;

public interface DAO<T extends ModelData<K>,K> {
    void create(T data) throws DataAccessException;
    T get(K key);
    void update(T data);
    void delete(K key);
    void clear() throws DataAccessException;
}
