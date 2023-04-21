package sd2223.trab1.clients.common;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;

import java.util.List;

public class RetryFeedsClient extends RetryClient implements Feeds {

    final Feeds impl;

    public RetryFeedsClient( Feeds impl) {
        this.impl = impl;
    }
    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        return reTry( () -> impl.postMessage(user,pwd,msg));
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return reTry( () -> impl.removeFromPersonalFeed(user,mid,pwd));
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return reTry( () -> impl.getMessage(user,mid));
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        return reTry( () -> impl.getMessages(user,time));
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return reTry( () -> impl.subUser(user,userSub,pwd));
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return reTry( () -> impl.unsubscribeUser(user,userSub,pwd));
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        return reTry( () -> impl.listSubs(user));
    }

    @Override
    public Result<List<Message>> ownMessages(String user, long time) {
        return reTry( () -> impl.ownMessages(user,time));
    }
}
