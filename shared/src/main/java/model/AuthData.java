package model;

public record AuthData(
        String authToken,
        String username
) implements ModelData<String> {
    public String key() {
        return authToken;
    }
};
