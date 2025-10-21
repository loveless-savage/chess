package dataaccess;

import model.*;

public class GameDAO extends MemoryDAO<GameData,Integer>{
    private int nextID = 1;

    public void create(GameData data) {
        super.create(data);
        nextID++;
    }
    public int getNextID() {
        return nextID;
    }
    public void clear() {
        super.clear();
        nextID = 1;
    }

    public GameData[] list() {
        return refdata.values().toArray(GameData[]::new);
    }
}
