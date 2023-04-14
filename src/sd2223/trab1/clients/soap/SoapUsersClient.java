package sd2223.trab1.clients.soap;

import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;

import java.net.URI;
import java.util.List;

public class SoapUsersClient implements Users {

    public SoapUsersClient(URI serverUri){

    }

    @Override
    public Result<String> createUser(User user) {
        return null;
    }

    @Override
    public Result<User> getUser(String name, String password) {
        return null;
    }

    @Override
    public Result<User> updateUser(String name, String password, User user) {
        return null;
    }

    @Override
    public Result<User> deleteUser(String name, String password) {
        return null;
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return null;
    }

    @Override
    public Result<User> findUser(String name) {
        return null;
    }

}
