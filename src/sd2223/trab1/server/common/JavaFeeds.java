package sd2223.trab1.server.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.server.util.Token;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static sd2223.trab1.api.java.Result.ErrorCode.*;
import static sd2223.trab1.api.java.Result.error;
import static sd2223.trab1.api.java.Result.ok;
import static sd2223.trab1.clients.Clients.UsersClients;

public class JavaFeeds implements Feeds {

    final static Logger Log = Logger.getLogger(JavaFeeds.class.getName());

    private static long lastMessageId = 0;
    final protected Map<Long, Message> allMessages = new ConcurrentHashMap<>();
    final protected Map<String, List<Long>> userMessages = new ConcurrentHashMap<>();
    final protected Map<String, List<String>> usersSubscribedTo = new ConcurrentHashMap<>();

    final protected Map<String ,List<String>> usersSubscribing = new ConcurrentHashMap<>();

    static final LoadingCache<String, User> users = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(20000))
            .build(new CacheLoader<>() {
                @Override
                public User load(String name) throws Exception {
                    return UsersClients.get(Token.get() + ":" + Users.SERVICE_NAME).findUser(name).value();
                }
            });


    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        msg.setId(generateRandomId());
        Log.info(String.format("REST: postMessage: user = %s, password = %s + MSG: ",user,pwd)+msg);

        if (JavaCommonMethods.nullValue(user) || JavaCommonMethods.nullValue(pwd)
                || JavaCommonMethods.nullValue(msg))
            return error(BAD_REQUEST);

        String userId = user.split("@")[0];
        var userPosting = getUser(userId);
        if (JavaCommonMethods.nullValue(userPosting)){
            return error(NOT_FOUND);
        }

        if(wrongPassword(userPosting,pwd)) {
            return error(FORBIDDEN);
        }

        List<Long> msgList = userMessages.get(userId);
        synchronized (userMessages) {
            if (msgList != null) {
                msgList.add(msg.getId());
            } else {
                msgList = new ArrayList<>();
                msgList.add(msg.getId());
                userMessages.put(userId, msgList);
            }
        }

        synchronized (allMessages) {
            allMessages.putIfAbsent(msg.getId(), msg);
        }

        //putMessageInAllSubscribersFeed(userId,msg.getId());

        return ok(msg.getId());
    }

    private void putMessageInAllSubscribersFeed(String userId,long mid){
        List<String> subscribers = usersSubscribing.get(userId);
        if(subscribers!= null) {
            List<Long> userMessagesList;
            String subscriberId;
            for (String u : subscribers) {
                subscriberId = u.split("@")[0];
                userMessagesList = userMessages.get(subscriberId);
                if (userMessagesList == null)
                    userMessagesList = new ArrayList<>();
                userMessages.put(subscriberId, userMessagesList);
                userMessagesList.add(mid);
            }
        }
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        Log.info(String.format("REST: getMessage: user = %s mid = ",user)+mid+ " \n");

        String userId = user.split("@")[0];

        if (JavaCommonMethods.nullValue(userId)  || JavaCommonMethods.nullValue(mid)) {
            return error(BAD_REQUEST);
        }

        Message msg = allMessages.get(mid);

        if ( JavaCommonMethods.nullValue(msg) || !getIfUserExists(userId)){
            Log.info("aaaaaa\n");
            return error(NOT_FOUND);
        }

        if(!checkIfMessageIsInUserFeed(userId,msg)) {
            Log.info("bbbbbbbbb\n");
            return error(NOT_FOUND);
        }

        return ok(msg);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        Log.info(String.format("REST: getMessages: user = %s newer than = ", user) + time + "\n");
        String userId = user.split("@")[0];

        if (JavaCommonMethods.nullValue(userId)) {
            return error(BAD_REQUEST);
        }

        var userToSearch = getUser(userId);
        if (JavaCommonMethods.nullValue(userToSearch)){
            return error(NOT_FOUND);
        }

        if (!userMessages.containsKey(userId)) {
            userMessages.put(userId, new ArrayList<>());
        }
        var messageIDsFromUser = messagesFromUserSinceTime(userMessages.get(userId), time);
        var usersFollowing = usersSubscribedTo.get(userId);
        if(usersFollowing == null) {
            usersFollowing = new ArrayList<>();
            usersSubscribedTo.putIfAbsent(userId,usersFollowing);
        }

        Log.info("--\n");
        Log.info(usersFollowing.toString());
        Log.info("--\n");
        var messagesFromSubscribers= joinMessagesFromFollowing(usersFollowing,time);

        messageIDsFromUser.addAll(messagesFromSubscribers);

        return ok(messageIDsFromUser);
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        Log.info(String.format("REST: subUser: subscriber = %s subscribing = %s \n", user,userSub));
        String userSubcriberId = user.split("@")[0];
        String userSubcribingId = userSub.split("@")[0];

        var userSubscriber = getUser(userSubcriberId);
        if(JavaCommonMethods.nullValue(userSubscriber) || wrongPassword(userSubscriber,pwd))
            return error(FORBIDDEN);

        var userSubscribing = getUser(userSubcribingId);
        if(JavaCommonMethods.nullValue(userSubscribing))
            return error(NOT_FOUND);

        List<String> subscribingList = usersSubscribedTo.get(userSubcriberId);
        if (subscribingList == null)
            subscribingList = new ArrayList<>();

        if(!subscribingList.contains(userSub))
            subscribingList.add(userSub);

        List<String> subscribersList = usersSubscribing.get(userSubscribing);
        if(subscribersList == null)
            subscribersList = new ArrayList<>();
        if(!subscribersList.contains(user))
            subscribersList.add(user);

        usersSubscribedTo.put(userSubcriberId,subscribingList);
        usersSubscribing.put(userSubcribingId,subscribersList);
        System.out.println(usersSubscribedTo.get(userSubcriberId));
        return ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return null;
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        Log.info(String.format("REST: listSub: user = %s \n", user));
        String userId = user.split("@")[0];
        if(JavaCommonMethods.nullValue(userId))
            return error(NOT_FOUND);

        var listOfSubscriptions = usersSubscribedTo.get(userId);
        if(JavaCommonMethods.nullValue(listOfSubscriptions)){
            listOfSubscriptions = new ArrayList<>();
            usersSubscribedTo.putIfAbsent(userId, listOfSubscriptions);
        }

        return ok(usersSubscribedTo.get(userId));

    }


    private User getUser(String name){
        Log.info(String.format("checkpoint 2.1\n"));
        try{
            Log.info(String.format("checkpoint 2.11\n"));
            return users.get(name);
        } catch (Exception x) {
            Log.info(String.format("entrou no error\n"));
            return null;
        }
    }

    private boolean getIfUserExists(String userId) {
        return getUser(userId) != null;
    }

    private List<Message> joinMessagesFromFollowing(List<String> usersFollowing, long time){
        List<Message> messages = new ArrayList<>();
        for(String u : usersFollowing){
            String userId = u.split("@")[0];
            Log.info("--\n");
            Log.info(userId);
            Log.info("--\n");
            Log.info(userMessages.get(userId).toString());
            Log.info("--\n");
            List<Message> uMessages = messagesFromUserSinceTime(userMessages.get(userId),time);
            messages.addAll(uMessages);
        }
        return messages;
    }

    /*private boolean checkIfMessageInFeed(String user,Message msg){
        return ;
    }*/

    private long generateRandomId(){
        long min = 0;
        long max = Long.MAX_VALUE;
        Random random = new Random();
        long randomLong = -1;
        while(randomLong < 0 || allMessages.containsKey(randomLong)){
            randomLong = (long) (random.nextFloat() * (max - min) + min);
        }
        return randomLong;
    }

    private List<Message> messagesFromUserSinceTime(List<Long> midS, long time){
        return midS.stream()
                .filter(msgId -> allMessages.containsKey(msgId) &&
                        allMessages.get(msgId).getCreationTime() >= time)
                .map(allMessages::get)
                .collect(Collectors.toList());
    }

    private boolean checkIfMessageIsInUserFeed(String userId, Message msg){
        String msgOwner = msg.getUser() + "@" + msg.getDomain();
        Log.info(msgOwner);
        Log.info("--\n");
        Log.info(userMessages.get(userId).toString());
        Log.info("--\n");
        if(usersSubscribedTo.containsKey(userId))
            Log.info(usersSubscribedTo.get(userId).toString());
        return userMessages.get(userId).contains(msg.getId()) || usersSubscribedTo.get(userId).contains(msgOwner);
    }

    private boolean wrongPassword(User user, String pwd){
        if (user == null) return true;
        return !user.getPwd().equals(pwd) ;
    }
}
