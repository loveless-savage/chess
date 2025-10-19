package dataaccess;

import model.UserData;

public class GameDAO extends MemoryDAO<UserData,Integer>{
    public UserData[] list(String authToken) {
        return new UserData[]{refdata};
    }
}
