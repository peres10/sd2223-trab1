package sd2223.trab1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.User;
import sd2223.trab1.api.service.java.Result;
import sd2223.trab1.api.service.java.Users;
import sd2223.trab1.api.service.rest.UsersService;
import sd2223.trab1.clients.Clients;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class RestUsersClient extends RestClient implements Users {

    final static Logger Log = Logger.getLogger(RestUsersClient.class.getName());
    public RestUsersClient(URI serverURI) {
        super(serverURI, UsersService.PATH);
    }

    @Override
    public Result<String> createUser(User user) {
        Response r = target
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post( Entity.entity(user, MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, new GenericType<String>(){});
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        Response r = target
                .path(userId)
                .queryParam(UsersService.PWD, password)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, new GenericType<User>(){});
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        Response r = target
                .path(userId)
                .queryParam(UsersService.PWD, password)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.entity(user,MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, new GenericType<User>(){});
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        Response r = target
                .path(userId)
                .queryParam(UsersService.PWD, password)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        return super.toJavaResult(r, new GenericType<User>(){});
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Response r = target
                .queryParam(UsersService.QUERY, pattern)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, new GenericType<List<User>>() {});
    }

}
