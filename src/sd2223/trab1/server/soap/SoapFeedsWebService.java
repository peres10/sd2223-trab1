package sd2223.trab1.server.soap;

import jakarta.jws.WebService;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.soap.FeedsException;
import sd2223.trab1.api.soap.FeedsService;
import sd2223.trab1.server.common.JavaFeeds;

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
        return 0;
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) throws FeedsException {

    }

    @Override
    public Message getMessage(String user, long mid) throws FeedsException {
        return null;
    }

    @Override
    public List<Message> getMessages(String user, long time) throws FeedsException {
        return null;
    }

    @Override
    public void subUser(String user, String userSub, String pwd) throws FeedsException {

    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) throws FeedsException {

    }

    @Override
    public List<String> listSubs(String user) throws FeedsException {
        return null;
    }
}