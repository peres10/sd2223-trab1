package sd2223.trab1.api.java;

import sd2223.trab1.api.User;

import java.util.List;

public interface Users {
    public static String SERVICE_NAME = "users";

    Result<String> createUser(User user);

    Result<User> getUser(String name, String password);

    Result<User> updateUser(String name, String password, User user);

    Result<User> deleteUser(String name, String password);

    Result<List<User>> searchUsers(String pattern);

    Result<User> findUser(String name);

}
