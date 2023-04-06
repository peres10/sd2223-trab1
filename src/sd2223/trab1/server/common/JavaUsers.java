package sd2223.trab1.server.common;

import sd2223.trab1.api.User;
import sd2223.trab1.api.service.java.Result;
import sd2223.trab1.api.service.java.Users;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static sd2223.trab1.api.service.java.Result.ErrorCode.*;
import static sd2223.trab1.api.service.java.Result.error;
import static sd2223.trab1.api.service.java.Result.ok;

public class JavaUsers implements Users {
    final protected Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public Result<String> createUser(User user) {
        if( JavaCommonMethods.badUser(user))
            return error( BAD_REQUEST );

        var userId = user.getName();
        var res = users.putIfAbsent(userId, user);

        if(res != null)
            return error(CONFLICT);
        else
            return ok(userId + '@' + user.getDomain());
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        if (JavaCommonMethods.nullValue(userId) || JavaCommonMethods.nullValue(password))
            return error(BAD_REQUEST);
        var user = users.get(userId);

        if(user == null)
            return error(NOT_FOUND);

        if(JavaCommonMethods.wrongPwd(user, password))
            return error(FORBIDDEN);
        else
            return ok(user);
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        var olduser = users.get(userId);

        if(olduser == null)
            return error(NOT_FOUND);

        if(JavaCommonMethods.nullValue(password) || JavaCommonMethods.wrongPwd(olduser, password))
            return error(FORBIDDEN);
        else{
            olduser.updateUser(user);
            return ok(olduser);
        }
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        if (JavaCommonMethods.nullValue(userId) || JavaCommonMethods.nullValue(password))
            return error(BAD_REQUEST);

        var user = users.get(userId);

        if(user == null)
            return error(NOT_FOUND);

        if(JavaCommonMethods.wrongPwd(user, password))
            return error(FORBIDDEN);
        else {
            users.remove(userId);
            return ok(user);
        }

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

}
