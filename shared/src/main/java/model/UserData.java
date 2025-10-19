package model;

public record UserData(
        String username,
        String password,
        String email
) implements ModelData<String> {
    public String key() {
        return username;
    }
};
