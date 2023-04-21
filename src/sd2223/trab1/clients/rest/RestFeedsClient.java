package sd2223.trab1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.rest.FeedsService;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class RestFeedsClient extends RestClient implements Feeds {

    final static Logger Log = Logger.getLogger(RestFeedsClient.class.getName());

    public RestFeedsClient(URI serverURI) {
        super(serverURI, FeedsService.PATH);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        Response r = target
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post( Entity.entity(msg,MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, new GenericType<Long>(){});
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        Response r = target
                .path(user).path(Long.toString(mid))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        return super.toJavaResult(r, new GenericType<Void>(){});
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        Response r = target
                .path(user)
                .path(Long.toString(mid))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        return super.toJavaResult(r, new GenericType<Message>(){});
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        Response r = target
                .path(user)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, new GenericType<List<Message>>(){});
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        Response r = target
                .path("sub")
                .path(user)
                .path(userSub)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post( Entity.entity(userSub,MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, new GenericType<Void>(){});
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        Response r = target
                .path("sub")
                .path(user)
                .path(userSub)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        return super.toJavaResult(r, new GenericType<Void>(){});
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        Response r = target
                .path("sub")
                .path("list")
                .path(user)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, new GenericType<List<String>>(){});
    }

    @Override
    public Result<List<Message>> ownMessages(String user, long time) {
        Response r = target
                .path(user)
                .path("myMessages")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, new GenericType<List<Message>>(){});
    }
}
