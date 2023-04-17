package sd2223.trab1.server.common;

import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static sd2223.trab1.api.java.Result.ErrorCode.*;
import static sd2223.trab1.api.java.Result.error;
import static sd2223.trab1.api.java.Result.ok;

public class JavaUsers implements Users {
    final protected Map<String, User> users = new ConcurrentHashMap<>();

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());
    @Override
    public Result<String> createUser(User user) {
        if( JavaCommonMethods.badUser(user))
            return error( BAD_REQUEST );

        var userId = user.getName();
        synchronized (users) {
            var res = users.putIfAbsent(userId, user);
            if (res != null)
                return error(CONFLICT);
        }

        return ok(userId + '@' + user.getDomain());
    }

    @Override
    public Result<User> getUser(String name, String password) {
        if (JavaCommonMethods.nullValue(name) || JavaCommonMethods.nullValue(password))
            return error(BAD_REQUEST);
        var user = users.get(name);

        if(user == null)
            return error(NOT_FOUND);
        if(JavaCommonMethods.wrongPwd(user, password))
            return error(FORBIDDEN);
        else
            return ok(user);
    }

    @Override
    public Result<User> updateUser(String name, String password, User user) {
        var olduser = users.get(name);

        if(!user.getName().equals(name))
            return error(BAD_REQUEST);

        if(olduser == null)
            return error(NOT_FOUND);

        if(JavaCommonMethods.nullValue(password) || JavaCommonMethods.wrongPwd(olduser, password))
            return error(FORBIDDEN);

        synchronized (users) {
            olduser.updateUser(user);
        }
        return ok(olduser);
    }

    @Override
    public Result<User> deleteUser(String name, String password) {
        if (JavaCommonMethods.nullValue(name) || JavaCommonMethods.nullValue(password))
            return error(BAD_REQUEST);

        var user = users.get(name);

        if(user == null)
            return error(NOT_FOUND);

        if(JavaCommonMethods.wrongPwd(user, password))
            return error(FORBIDDEN);
        synchronized (users) {
            users.remove(name);
        }

        return ok(user);

    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        if ( JavaCommonMethods.nullValue( pattern ))
            return error(BAD_REQUEST);

        var hits = users.values()
                .stream()
                .filter( u -> u.getName().toLowerCase().contains(pattern.toLowerCase()))
                .toList();

        return ok(hits);
    }

    @Override
    public Result<User> findUser(String name) {
        if ( JavaCommonMethods.nullValue( name ))
            return error(BAD_REQUEST);

        var user = users.get(name);

        if(user==null)
            return error(NOT_FOUND);
        else
            return ok(user);
    }

}
