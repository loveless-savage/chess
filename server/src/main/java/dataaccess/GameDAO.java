package dataaccess;

import model.*;

public class GameDAO extends MemoryDAO<GameData,Integer>{
    public GameData[] list() {
        return refdata.values().toArray(GameData[]::new);
    }
}
