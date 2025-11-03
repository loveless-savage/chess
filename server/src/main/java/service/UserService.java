package service;

import dataaccess.*;
import model.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;

public class UserService {
    final UserDAO userDAO;
    final AuthDAO authDAO;

    public UserService() {
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
    }
    public UserService(UserDAO userIn, AuthDAO authIn) {
        userDAO = userIn;
        authDAO = authIn;
    }

    public AuthData register(UserData newUser) throws DataAccessException {
        if(newUser.username() == null || newUser.password() == null || newUser.email() == null) {
            throw new BadRequestException("bad request");
        }
        if(userDAO.get(newUser.username()) != null) {
            throw new AlreadyTakenException("username already taken");
        }
        var encryptedUser = new UserData(
                newUser.username(),
                BCrypt.hashpw(newUser.password(),BCrypt.gensalt()),
                newUser.email());
        userDAO.create(encryptedUser);
        AuthData loginInfo = new AuthData(generateToken(), newUser.username());
        authDAO.create(loginInfo);
        return loginInfo;
    }

    public AuthData login(LoginRequest loginRequest) throws DataAccessException {
        if(loginRequest.username() == null || loginRequest.password() == null) {
            throw new BadRequestException("bad request");
        }
        var userRetrieved = userDAO.get(loginRequest.username());
        if(userRetrieved == null ||
                    !BCrypt.checkpw(loginRequest.password(),userRetrieved.password())) {
            throw new UnauthorizedException("unauthorized");
        }
        AuthData loginInfo = new AuthData(generateToken(), loginRequest.username());
        authDAO.create(loginInfo);
        return loginInfo;
    }

    public void logout(String authToken) throws DataAccessException {
        if(authDAO.get(authToken) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        authDAO.delete(authToken);
    }

    public void clear() throws DataAccessException {
        authDAO.clear();
        userDAO.clear();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
