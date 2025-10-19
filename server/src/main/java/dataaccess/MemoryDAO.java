package dataaccess;

public abstract class MemoryDAO<T,K> implements DAO<T,K> {
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

    public void clear() {
        refdata = null;
    }
}
