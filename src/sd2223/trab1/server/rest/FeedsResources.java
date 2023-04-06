package sd2223.trab1.server.rest;

import jakarta.inject.Singleton;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.service.java.Feeds;
import sd2223.trab1.api.service.rest.FeedsService;
import sd2223.trab1.server.common.JavaFeeds;

import java.util.List;
import java.util.logging.Logger;

@Singleton
public class FeedsResources extends RestResource implements FeedsService {
    private static Logger Log = Logger.getLogger(FeedsResources.class.getName());
    final Feeds impl;

    public FeedsResources(){
        this.impl = new JavaFeeds();
    }

    @Override
    public long postMessage(String user, String pwd, Message msg) {
        return super.fromJavaResult(impl.postMessage(user,pwd,msg));
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {
    }

    @Override
    public Message getMessage(String user, long mid) {
        return super.fromJavaResult(impl.getMessage(user,mid));
    }

    @Override
    public List<Message> getMessages(String user, long time) {
        return super.fromJavaResult(impl.getMessages(user,time));
    }

    @Override
    public void subUser(String user, String userSub, String pwd) {
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {
    }

    @Override
    public List<String> listSubs(String user) {
        return null;
    }
}