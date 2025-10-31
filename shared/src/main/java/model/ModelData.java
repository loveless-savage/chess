package model;

public interface ModelData<K> {
    static String keyName() {
        return "";
    };
    K keyValue();
}
