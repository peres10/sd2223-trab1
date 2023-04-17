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
import java.util.*;
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
    final protected Map<String, Set<String>> usersSubscribedTo = new ConcurrentHashMap<>();

    final protected Map<String ,List<String>> usersSubscribing = new ConcurrentHashMap<>();

    static final LoadingCache<String, User> users = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(1000))
            .build(new CacheLoader<>() {
                @Override
                public User load(String name) throws Exception {
                    return UsersClients.get(Token.get() + ":" + Users.SERVICE_NAME).findUser(name).value();
                }
            });


    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        msg.setId(lastMessageId++);
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

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        if(JavaCommonMethods.nullValue(user) || JavaCommonMethods.nullValue(mid) ||
        JavaCommonMethods.nullValue(pwd))
            return error(BAD_REQUEST);

        String userId = user.split("@")[0];
        var userOwner = getUser(userId);

        if (JavaCommonMethods.nullValue(userOwner)){
            return error(FORBIDDEN);
        }

        if(wrongPassword(userOwner,pwd)){
            return error(FORBIDDEN);
        }

        List<Long> ownerMessages =userMessages.get(userId);

        if(!allMessages.containsKey(mid) || !ownerMessages.contains(mid))
            return error(BAD_REQUEST);

        ownerMessages.remove(mid);
        allMessages.remove(mid);

        return ok();
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
            usersFollowing = new HashSet<>();
            usersSubscribedTo.putIfAbsent(userId,usersFollowing);
        }

        Log.info("--\n");
        Log.info(usersFollowing.toString());
        Log.info("--\n");
        var messagesFromSubscribers= joinMessagesFromFollowing(usersFollowing.stream().toList(),time);

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

        Set<String> subscribingList = usersSubscribedTo.get(userSubcriberId);
        if (subscribingList == null)
            subscribingList = new HashSet<>();

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
        Log.info(String.format("REST: unsubscribeUser: subscriber = %s subscribing = %s \n", user,userSub));
        String userSubcriberId = user.split("@")[0];
        String userUnsubcribingId = userSub.split("@")[0];

        var userSubscriber = getUser(userSubcriberId);
        if(JavaCommonMethods.nullValue(userSubscriber) || wrongPassword(userSubscriber,pwd))
            return error(FORBIDDEN);

        var userUnsubscribing = getUser(userUnsubcribingId);
        if(JavaCommonMethods.nullValue(userUnsubcribingId)) {
            Log.info("not found 1\n");
            return error(NOT_FOUND);
        }

        var usersSubscribed = usersSubscribedTo.get(userSubcriberId);
        Log.info(usersSubscribed.toString());
        if(!usersSubscribed.contains(userSub)) {
            Log.info("not found 2\n");
            return error(NOT_FOUND);
        }

        else
            usersSubscribed.remove(userUnsubcribingId);

        return ok();
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        Log.info(String.format("REST: listSub: user = %s \n", user));
        String userId = user.split("@")[0];
        if(JavaCommonMethods.nullValue(userId) || getUser(userId) == null)
            return error(NOT_FOUND);

        var listOfSubscriptions = usersSubscribedTo.get(userId);
        if(JavaCommonMethods.nullValue(listOfSubscriptions)){
            listOfSubscriptions = new HashSet<>();
            usersSubscribedTo.putIfAbsent(userId, listOfSubscriptions);
        }

        return ok(usersSubscribedTo.get(userId).stream().toList());

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
        Log.info("aaaaaa\n");
        List<Message> messages = new ArrayList<>();
        for(String u : usersFollowing){
            String userId = u.split("@")[0];
            Log.info("--\n");
            Log.info(userId);
            Log.info("--\n");
            //Log.info(userMessages.get(userId).toString());
            Log.info("vvvvv\n");
            List<Long> userMessagesMids = userMessages.get(userId);
            if(userMessagesMids == null)
                continue;
            List<Message> uMessages = messagesFromUserSinceTime(userMessagesMids,time);
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
        Log.info("lllllllll\n");
        return midS.stream()
                .filter(msgId -> allMessages.containsKey(msgId) &&
                        allMessages.get(msgId).getCreationTime() > time)
                .map(allMessages::get)
                .collect(Collectors.toList());

        /*List<Message> messages = new ArrayList<>();
        for( long mid : midS){
            Message msg = allMessages.get(mid);
            if (msg == null)
                continue;
            if (msg.getCreationTime() < time)
                continue;
            else
                messages.add(msg);
        }
        Log.info("ppppppppppppp\n");
        return messages;*/
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
}
