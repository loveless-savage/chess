package dataaccess;

import model.*;

public interface DAO<T,K> {
    void create(T data);
    T get(K key);
    void update(T data);
    void clear();
}
