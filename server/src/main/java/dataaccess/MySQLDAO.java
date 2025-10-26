package dataaccess;

import model.ModelData;

public class MySQLDAO<T extends ModelData<K>,K> implements DAO<T,K> {
    public void create(T data) {
    }

    public T get(K key) {
        return null;
    }

    public void update(T data) {
    }

    public void delete(K key) {
    }

    public void clear() {
    }
}
