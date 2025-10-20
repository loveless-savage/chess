package dataaccess;

import model.*;

public class GameDAO extends MemoryDAO<GameData,Integer>{
    public GameData[] list() {
        return new GameData[]{refdata};
    }
}
