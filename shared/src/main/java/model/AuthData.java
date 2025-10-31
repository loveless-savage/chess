package model;

public record AuthData(
        String authToken,
        String username
) implements ModelData<String> {
    public String keyName() {
        return "authToken";
    }
    public String keyValue() {
        return authToken;
    }
}
