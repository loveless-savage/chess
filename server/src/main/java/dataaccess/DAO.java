package dataaccess;

import model.*;

public interface DAO<T> {
    void create(T data);
    T get(String key);
    void update(T data);
    void clear();
}
