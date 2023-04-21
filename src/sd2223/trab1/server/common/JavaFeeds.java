package sd2223.trab1.server.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.rest.RestUsersClient;
import sd2223.trab1.server.util.Domain;
import sd2223.trab1.server.util.ServerMIDToken;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static sd2223.trab1.api.java.Result.ErrorCode.*;
import static sd2223.trab1.api.java.Result.error;
import static sd2223.trab1.api.java.Result.ok;
import static sd2223.trab1.clients.Clients.FeedsClients;
import static sd2223.trab1.clients.Clients.UsersClients;

public class JavaFeeds implements Feeds {

    final static Logger Log = Logger.getLogger(JavaFeeds.class.getName());

    //private static long lastMessageId = 0;
    final protected Map<Long, Message> allMessages = new ConcurrentHashMap<>();
    final protected Map<String, List<Long>> userMessages = new ConcurrentHashMap<>();

    final protected Map<String, Map<String, Set<String>>> usersSubscribedByDomain = new ConcurrentHashMap<>();

    private long msgIdBase = ServerMIDToken.tokenValue();
    private long msgIdSeq = 0;

    private long generateMid(){
        msgIdSeq++;
        return msgIdSeq * 256 + msgIdBase;
    }
    static final LoadingCache<String, User> users = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(1000))
            .build(new CacheLoader<>() {
                @Override
                public User load(String name) throws Exception {
                    return UsersClients.get(Domain.get() + ":" + Users.SERVICE_NAME).findUser(name).value();
                }
            });


    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        String[] usernameTokens = user.split("@");

        String userId = usernameTokens[0];
        String userDomain = usernameTokens[1];


        if (JavaCommonMethods.nullValue(user) || JavaCommonMethods.nullValue(pwd)
                || JavaCommonMethods.nullValue(msg) || !userDomain.equals(Domain.get()) )
            return error(BAD_REQUEST);


        var userPosting = getUser(userId);
        if (JavaCommonMethods.nullValue(userPosting)){
            return error(NOT_FOUND);
        }

        if(JavaCommonMethods.wrongPwd(userPosting,pwd)) {
            return error(FORBIDDEN);
        }

        List<Long> msgList = userMessages.get(userId);
        synchronized (userMessages) {
            msg.setId(generateMid());
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


        return ok(msg.getId());
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        if(JavaCommonMethods.nullValue(user) || JavaCommonMethods.nullValue(mid) ||
        JavaCommonMethods.nullValue(pwd))
            return error(BAD_REQUEST);

        String[] usernameTokens = user.split("@");

        String userId = usernameTokens[0];

        var userOwner = getUser(userId);
        if (JavaCommonMethods.nullValue(userOwner)){
            return error(FORBIDDEN);
        }

        if(JavaCommonMethods.wrongPwd(userOwner,pwd)){
            return error(FORBIDDEN);
        }

        List<Long> ownerMessages =userMessages.get(userId);

        if(!allMessages.containsKey(mid) || !ownerMessages.contains(mid))
            return error(NOT_FOUND);


        synchronized (userMessages) {
            ownerMessages.remove(mid);
        }

        synchronized (allMessages) {
            allMessages.remove(mid);
        }

        return ok();
    }


    @Override
    public Result<Message> getMessage(String user, long mid) {
        String[] usernameTokens = user.split("@");

        String userId = usernameTokens[0];
        String userDomain = usernameTokens[1];

        if (JavaCommonMethods.nullValue(userId)  || JavaCommonMethods.nullValue(mid)) {
            return error(BAD_REQUEST);
        }

        List<Message> userMsgs;
        Message msg;
        if(userDomain.equals(Domain.get())) {
            var userVal = getUser(userId);
            if (JavaCommonMethods.nullValue(userVal)) {
                return error(NOT_FOUND);
            }
            if(!userMessages.containsKey(userId)) {
                userMessages.put(userId, new ArrayList<>());
            }
            userMsgs = getAllFeedMessages(userId, 0L);
            msg = getMessageByMid(userMsgs,mid);
            if (msg == null){
                return error(NOT_FOUND);
            }
        } else {
            return forwardGetMessage(user,userDomain,mid);
        }

        return ok(msg);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        String[] usernameTokens = user.split("@");

        String userId = usernameTokens[0];
        String userDomain = usernameTokens[1];

        if (JavaCommonMethods.nullValue(userId)) {
            return error(BAD_REQUEST);
        }


        List<Message> userMsgs;
        if(userDomain.equals(Domain.get())) {
            var userVal = getUser(userId);
            if (JavaCommonMethods.nullValue(userVal)) {
                return error(NOT_FOUND);
            }
            if(!userMessages.containsKey(userId)) {
                userMessages.put(userId, new ArrayList<>());
            }
            userMsgs = getAllFeedMessages(userId, time);
        } else {
            return forwardGetMessages(user,userDomain,time);
        }


        return ok(userMsgs);

    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        String[] usernameSubscriberTokens = user.split("@");
        String userSubcriberId = usernameSubscriberTokens[0];

        String[] usernameSubscribingTokens = userSub.split("@");
        String userSubcribingId = usernameSubscribingTokens[0];
        String userSubcribingDomain = usernameSubscribingTokens[1];

        var userSubscriber = getUser(userSubcriberId);
        if(JavaCommonMethods.nullValue(userSubscriber) || JavaCommonMethods.wrongPwd(userSubscriber,pwd))
            return error(FORBIDDEN);

        User userSubscribing;
        if(userSubcribingDomain.equals(Domain.get())) {
            userSubscribing = getUser(userSubcribingId);
        } else {
            userSubscribing = getUserInOtherDomain(userSubcribingId, userSubcribingDomain);
        }
        if (JavaCommonMethods.nullValue(userSubscribing))
            return error(NOT_FOUND);

        synchronized (usersSubscribedByDomain) {
            initSubsInOtherDomains(userSubcriberId);
            initSubSetForSpecificDomain(userSubcriberId, userSubcribingDomain);
            addUserToUsersSubsSet(userSubcriberId, userSub);
        }


        return ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        String[] usernameSubscriberTokens = user.split("@");
        String userSubcriberId = usernameSubscriberTokens[0];

        String[] usernameSubscribingTokens = userSub.split("@");
        String userUnsubcribingId = usernameSubscribingTokens[0];

        var userSubscriber = getUser(userSubcriberId);
        if(JavaCommonMethods.nullValue(userSubscriber) || JavaCommonMethods.wrongPwd(userSubscriber,pwd))
            return error(FORBIDDEN);

        if(JavaCommonMethods.nullValue(userUnsubcribingId)) {
            return error(NOT_FOUND);
        }

        if(!checkIfUserIsSubing(userSubcriberId,userSub)){
            return error(NOT_FOUND);
        }

        synchronized (usersSubscribedByDomain) {
            removeUserFromUsersSubsSet(userSubcriberId, userSub);
        }

        return ok();
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        String[] usernameTokens = user.split("@");
        String userId = usernameTokens[0];

        if(JavaCommonMethods.nullValue(userId) || getUser(userId) == null)
            return error(NOT_FOUND);


        initSubsInOtherDomains(userId);
        var subsList = getAllSubscriptions(userId);
        return ok(subsList);

    }

    @Override
    public Result<List<Message>> ownMessages(String user, long time) {
        if(JavaCommonMethods.nullValue(user) || JavaCommonMethods.nullValue(time))
            return error(BAD_REQUEST);

        String[] usernameTokens = user.split("@");
        String userId = usernameTokens[0];

        var userVal = getUser(userId);

        if(userVal == null)
            return error(NOT_FOUND);

        if (!userMessages.containsKey(userId)) {
            userMessages.put(userId, new ArrayList<>());
        }

        var messageIDsFromUser = messagesFromUserSinceTime(userMessages.get(userId), time);

        return ok(messageIDsFromUser);
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    private User getUser(String name){
        try{
            return users.get(name);
        } catch (Exception x) {
            return null;
        }
    }

    private List<Message> messagesFromUserSinceTimeWithListMessages(List<Message> msgs, long time){
        return msgs.stream()
                .filter(msg -> msg.getCreationTime() > time)
                .collect(Collectors.toList());
    }
    private List<Message> messagesFromUserSinceTime(List<Long> midS, long time){
        return midS.stream()
                .filter(msgId -> allMessages.containsKey(msgId) &&
                        allMessages.get(msgId).getCreationTime() > time)
                .map(allMessages::get)
                .collect(Collectors.toList());
    }

    private void initSubsInOtherDomains(String userId){
        if (usersSubscribedByDomain.get(userId) == null)
            usersSubscribedByDomain.put(userId,new ConcurrentHashMap<>());
    }

    private void initSubSetForSpecificDomain(String userId, String domain){
        usersSubscribedByDomain.get(userId).computeIfAbsent(domain, k -> new HashSet<>());
    }

    private Result<Message> forwardGetMessage(String user, String domain, long mid){
        return FeedsClients.get(domain + ":" + Feeds.SERVICE_NAME).getMessage(user,mid);
    }

    private Result<List<Message>> forwardGetMessages(String user, String domain, long time){
        return FeedsClients.get(domain + ":" + Feeds.SERVICE_NAME).getMessages(user,time);
    }
    private void addUserToUsersSubsSet(String userId, String userSub){
        String[] usernameTokens = userSub.split("@");
        String userDomain = usernameTokens[1];

        usersSubscribedByDomain.get(userId).get(userDomain).add(userSub);
    }

    private void removeUserFromUsersSubsSet(String userId, String userSub){
        String[] usernameTokens = userSub.split("@");
        String userDomain = usernameTokens[1];

        usersSubscribedByDomain.get(userId).get(userDomain).remove(userSub);
    }

    private boolean checkIfUserIsSubing(String userId, String userSub){
        return getAllSubscriptions(userId).contains(userSub);
    }

    private User getUserInOtherDomain(String userId,String domain){
        return UsersClients.get(domain + ":" + Users.SERVICE_NAME).findUser(userId).value();
    }

    private Message getMessageByMid(List<Message> messages, long mid){
        return messages.stream()
                .filter(m-> m.getId() == mid)
                .findFirst()
                .orElse(null);
    }

    private List<String> getAllSubscriptions(String userId){
        List<String> allSubscritpions = new ArrayList<>();
        List<Set<String>> subsByDomain = usersSubscribedByDomain.get(userId).values().stream().toList();
        if (subsByDomain == null)
            return allSubscritpions;
        for(var domain : subsByDomain) {
            if(domain != null)
                allSubscritpions.addAll(domain);
        }
        return allSubscritpions;
    }

    private List<Message> getMessagesFromAllSubscrpitons(String userId, Long time){
        Map<String, Set<String>> userSubcriptionsByDomain = usersSubscribedByDomain.get(userId);
        Set<Message> allMessages = new HashSet<>();
        if(userSubcriptionsByDomain == null)
            return allMessages.stream().toList();
        for(String domain  :  userSubcriptionsByDomain.keySet()){
            if (domain.equals(Domain.get())) {
                for (String userFromThisDomain : userSubcriptionsByDomain.get(domain)) {
                    String userIdWithoutDomain = userFromThisDomain.split("@")[0];
                    var allOtherUserMsg = userMessages.get(userIdWithoutDomain);
                    if(allOtherUserMsg!=null) {
                        allMessages.addAll(messagesFromUserSinceTime(allOtherUserMsg, time));
                    }
                }
            } else{
                Feeds feedsClient = FeedsClients.get(domain + ":" + Feeds.SERVICE_NAME);
                for (String userFromDomain : userSubcriptionsByDomain.get(domain)){
                    allMessages.addAll(messagesFromUserSinceTimeWithListMessages(feedsClient.ownMessages(userFromDomain,time).value(),time));
                }
            }

        }
        return allMessages.stream().toList();
    }

    private List<Message> getAllFeedMessages(String userId, Long time){
        var allUserMsgs = messagesFromUserSinceTime(userMessages.get(userId),time);
        allUserMsgs.addAll(getMessagesFromAllSubscrpitons(userId,time));
        return allUserMsgs;
    }

}
