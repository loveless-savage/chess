package dataaccess;

import model.*;

import java.util.Objects;

public abstract class MemoryDAO<T extends ModelData<K>,K> implements DAO<T,K> {
    T refdata;

    public void create(T data) {
        refdata = data;
        System.out.println("memory not actually created lol");
    }

    public T get(K key) {
        return refdata;
    }

    public void update(T data) {
        refdata = data;
    }

    public void delete(K key) {
        refdata = null;
    }

    public void clear() {
        refdata = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemoryDAO<?, ?> that = (MemoryDAO<?, ?>) o;
        return Objects.equals(refdata, that.refdata);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(refdata);
    }
}
