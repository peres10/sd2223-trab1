package sd2223.trab1.clients.common;

import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;

import java.util.List;

public class RetryUsersClient extends RetryClient implements Users {

    final Users impl;

    public RetryUsersClient( Users impl ){
        this.impl = impl;
    }
    @Override
    public Result<String> createUser(User user) {
        return reTry(()-> impl.createUser(user));
    }

    @Override
    public Result<User> getUser(String name, String password) {
        return reTry(()-> impl.getUser(name, password));
    }

    @Override
    public Result<User> updateUser(String name, String password, User user) {
        return reTry(()-> impl.updateUser(name, password, user));
    }

    @Override
    public Result<User> deleteUser(String name, String password) {
        return reTry(()-> impl.deleteUser(name, password));
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return reTry(()-> impl.searchUsers(pattern));
    }

    @Override
    public Result<User> findUser(String name) {
        return reTry(() -> impl.findUser(name));
    }

}
