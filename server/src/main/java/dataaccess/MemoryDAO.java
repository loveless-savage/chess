package dataaccess;

public class MemoryDAO<T> implements DAO<T> {
    T refdata;

    public void create(T data) {
        refdata = data;
        System.out.println("memory not actually created lol");
    }

    public T get(String key) {
        return refdata;
    }

    public void update(T data) {
        refdata = data;
    }

    public void clear() {
        refdata = null;
    }
}
