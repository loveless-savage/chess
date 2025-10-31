package model;

public record UserData(
        String username,
        String password,
        String email
) implements ModelData<String> {
    static public String keyName() {
        return "username";
    }
    public String keyValue() {
        return username;
    }
}
