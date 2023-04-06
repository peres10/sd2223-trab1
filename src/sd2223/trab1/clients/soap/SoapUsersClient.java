package sd2223.trab1.clients.soap;

import sd2223.trab1.api.User;
import sd2223.trab1.api.service.java.Result;
import sd2223.trab1.api.service.java.Users;

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
    public Result<User> getUser(String userId, String password) {
        return null;
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        return null;
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        return null;
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return null;
    }

}
