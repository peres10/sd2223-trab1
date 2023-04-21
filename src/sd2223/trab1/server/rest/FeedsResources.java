package sd2223.trab1.server.rest;

import jakarta.inject.Singleton;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.server.common.JavaFeeds;
import sd2223.trab1.server.util.ServerMIDToken;

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
        Log.info(String.format("REST: postMessage: user = %s, password = %s + MSG: ",user,pwd)+msg);

        return super.fromJavaResult(impl.postMessage(user,pwd,msg));
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {
        Log.info(String.format("REST: removeFromPersonalFeed: user = %s mid = ",user)+mid);

        super.fromJavaResult(impl.removeFromPersonalFeed(user,mid,pwd));
    }

    @Override
    public Message getMessage(String user, long mid) {
        Log.info(String.format("REST: getMessage: user = %s mid = ",user)+mid+ " \n");

        return super.fromJavaResult(impl.getMessage(user,mid));
    }

    @Override
    public List<Message> getMessages(String user, long time) {
        Log.info(String.format("REST: getMessages: user = %s newer than = ", user) + time + "\n");

        return super.fromJavaResult(impl.getMessages(user,time));
    }

    @Override
    public void subUser(String user, String userSub, String pwd) {
        Log.info(String.format("REST: subUser: subscriber = %s subscribing = %s \n", user,userSub));

        super.fromJavaResult(impl.subUser(user,userSub,pwd));
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {
        Log.info(String.format("REST: unsubscribeUser: subscriber = %s subscribing = %s \n", user,userSub));

        super.fromJavaResult(impl.unsubscribeUser(user,userSub,pwd));
    }

    @Override
    public List<String> listSubs(String user) {
        Log.info(String.format("REST: listSub: user = %s \n", user));

        return super.fromJavaResult(impl.listSubs(user)) ;
    }

    @Override
    public List<Message> ownMessages(String user, long time) {
        Log.info(String.format("REST: ownMessages: user = %s newer than = ", user) + time + "\n");

        return super.fromJavaResult(impl.ownMessages(user,time));
    }
}
