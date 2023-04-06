package sd2223.trab1.clients.common;

import sd2223.trab1.api.User;
import sd2223.trab1.api.service.java.Result;
import sd2223.trab1.api.service.java.Users;
import sd2223.trab1.server.common.JavaUsers;

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
    public Result<User> getUser(String userId, String password) {
        return reTry(()-> impl.getUser(userId, password));
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        return reTry(()-> impl.updateUser(userId, password, user));
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        return reTry(()-> impl.deleteUser(userId, password));
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return reTry(()-> impl.searchUsers(pattern));
    }

}
