package sd2223.trab1.server.soap;

import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.server.util.Token;

import java.util.logging.Logger;

public class FeedsSoapServer extends AbstractSoapServer{

    public static final int PORT = 9675;
    private static Logger Log = Logger.getLogger(FeedsSoapServer.class.getName());

    protected FeedsSoapServer() {
        super(Log, Feeds.SERVICE_NAME, PORT, new SoapFeedsWebService());
    }

    public static void main(String[] args) throws Exception {
        Token.set( args.length > 0 ? args[0] : "");

        new FeedsSoapServer().start();
    }
}
