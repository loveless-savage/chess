package dataaccess;

import model.*;

import java.util.HashMap;
import java.util.Objects;

public abstract class MemoryDAO<T extends ModelData<K>,K> implements DAO<T,K> {
    final HashMap<K,T> refdata;
    MemoryDAO() {
        refdata = new HashMap<>();
    }

    public void create(T data) {
        refdata.put(data.keyValue(),data);
    }

    public T get(K key) {
        return refdata.get(key);
    }

    public void update(T data) {
        refdata.replace(data.keyValue(),data);
    }

    public void delete(K key) {
        refdata.remove(key);
    }

    public void clear() {
        refdata.clear();
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
