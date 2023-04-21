package sd2223.trab1.server.soap;

import jakarta.jws.WebService;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.soap.FeedsException;
import sd2223.trab1.api.soap.FeedsService;
import sd2223.trab1.server.common.JavaFeeds;
import sd2223.trab1.server.util.ServerMIDToken;

import java.util.List;
import java.util.logging.Logger;

@WebService(serviceName = FeedsService.NAME, targetNamespace = FeedsService.NAMESPACE, endpointInterface = FeedsService.INTERFACE)
public class SoapFeedsWebService extends SoapWebService<FeedsException> implements FeedsService {
    private static Logger Log = Logger.getLogger(SoapFeedsWebService.class.getName());

    final Feeds impl;

    public SoapFeedsWebService() {
        super( (result)-> new FeedsException( result.error().toString()));
        this.impl = new JavaFeeds();
    }


    @Override
    public long postMessage(String user, String pwd, Message msg) throws FeedsException {
        Log.info(String.format("SOAP: postMessage: user = %s, password = %s + MSG: ",user,pwd)+msg);

        return super.fromJavaResult(impl.postMessage(user,pwd,msg));
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) throws FeedsException {
        Log.info(String.format("SOAP: removeFromPersonalFeed: user = %s mid = ",user)+mid);

        super.fromJavaResult(impl.removeFromPersonalFeed(user,mid,pwd));
    }

    @Override
    public Message getMessage(String user, long mid) throws FeedsException {
        Log.info(String.format("SOAP: getMessage: user = %s mid = ",user)+mid+ " \n");

        return super.fromJavaResult(impl.getMessage(user,mid));
    }

    @Override
    public List<Message> getMessages(String user, long time) throws FeedsException {
        Log.info(String.format("SOAP: getMessages: user = %s newer than = ", user) + time + "\n");

        return super.fromJavaResult(impl.getMessages(user,time));
    }

    @Override
    public void subUser(String user, String userSub, String pwd) throws FeedsException {
        Log.info(String.format("SOAP: subUser: subscriber = %s subscribing = %s \n", user,userSub));

        super.fromJavaResult(impl.subUser(user,userSub,pwd));
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) throws FeedsException {
        Log.info(String.format("SOAP: unsubscribeUser: subscriber = %s subscribing = %s \n", user,userSub));

        super.fromJavaResult(impl.unsubscribeUser(user,userSub,pwd));
    }

    @Override
    public List<String> listSubs(String user) throws FeedsException {
        Log.info(String.format("SOAP: listSub: user = %s \n", user));

        return super.fromJavaResult(impl.listSubs(user)) ;
    }

    @Override
    public List<Message> ownMessages(String user, long time) throws FeedsException {
        Log.info(String.format("SOAP: ownMessages: user = %s newer than = ", user) + time + "\n");

        return super.fromJavaResult(impl.ownMessages(user,time));
    }
}
