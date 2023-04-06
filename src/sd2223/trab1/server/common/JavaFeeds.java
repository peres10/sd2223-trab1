package sd2223.trab1.server.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.service.java.Feeds;
import sd2223.trab1.api.service.java.Result;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static sd2223.trab1.api.service.java.Result.ErrorCode.*;
import static sd2223.trab1.api.service.java.Result.error;
import static sd2223.trab1.api.service.java.Result.ok;
import static sd2223.trab1.clients.Clients.UsersClients;

public class JavaFeeds implements Feeds {

    final static Logger Log = Logger.getLogger(JavaFeeds.class.getName());

    private static long lastMessageId = 0;
    final protected Map<Long, Message> allMessages = new ConcurrentHashMap<>();
    final protected Map<String, List<Long>> userMessages = new ConcurrentHashMap<>();
    final protected Map<String, List<User>> usersFollowed = new ConcurrentHashMap<>();

    static final LoadingCache<UserCredentials, Result<User>> loggedUsersCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(20000))
            .build(new CacheLoader<>() {
                @Override
                public Result<User> load(UserCredentials userCredentials) throws Exception {
                    var res = UsersClients.get().getUser( userCredentials.userId, userCredentials.pwd());
                    if( res.error() == Result.ErrorCode.TIMEOUT)
                        return error(BAD_REQUEST);
                    else
                        return res;
                }
            });

    static final LoadingCache<String, Result<List<User>>> allUsersByPatternsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(20000))
            .build(new CacheLoader<String, Result<List<User>>>() {
                @Override
                public Result<List<User>> load(String userId) throws Exception {
                    var res = UsersClients.get().searchUsers( userId );
                    if( res.error() == TIMEOUT)
                        return error(BAD_REQUEST);
                    else
                        return res;
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
        var userPosting = getUser(userId, pwd);
        if(!userPosting.isOK()) {
            return error(userPosting.error());
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
            allMessages.put(msg.getId(), msg);
        }
        return ok(msg.getId());
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        Log.info(String.format("REST: getMessage: user = %s mid = ",user)+mid+ " \n");

        String userId = user.split("@")[0];

        if (JavaCommonMethods.nullValue(userId)  || JavaCommonMethods.nullValue(mid)) {
            return error(BAD_REQUEST);
        }

        System.out.println(allMessages.keySet());
        Message msg = allMessages.get(mid);
        Log.info(msg+"\n");

        if ( JavaCommonMethods.nullValue(msg) ) {
            return error(NOT_FOUND);
        }

        if(!userMessages.containsKey(userId)){
            return error(NOT_FOUND);
        }

        if(!checkIfMessageBelongsToUser(userId,mid)) {
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

        try {
            if (!userMessages.containsKey(userId)) {
                if (!getIfUserExists(userId))
                    return error(NOT_FOUND);
                else
                    userMessages.put(userId, new ArrayList<>());
            }
        } catch(ExecutionException e) {
            return error(INTERNAL_ERROR);
        }

        var messageIDsFromUser = messagesFromUserSinceTime(userMessages.get(userId), time);

        return ok(messageIDsFromUser);
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return null;
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return null;
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        return null;
    }

    static record UserCredentials(String userId, String pwd){
    }

    private Result<User> getUser(String userId, String pwd){
        try{
            UserCredentials userCrds = new UserCredentials(userId,pwd);
            return loggedUsersCache.get(userCrds);
        } catch (Exception x) {
            return error(INTERNAL_ERROR);
        }
    }

    private boolean getIfUserExists(String userId) throws ExecutionException {
        Result<List<User>> usersWithPattern = allUsersByPatternsCache.get(userId);
        Log.info(usersWithPattern.value().toString());
        return usersWithPattern.value().contains(userId);
    }


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

    private boolean checkIfMessageBelongsToUser(String userId, long mid){
        return userMessages.get(userId).contains(mid);
    }
}
