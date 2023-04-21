package sd2223.trab1.server.soap;

import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.server.util.Domain;
import sd2223.trab1.server.util.ServerMIDToken;

import java.util.logging.Logger;

public class FeedsSoapServer extends AbstractSoapServer{

    public static final int PORT = 9675;
    private static Logger Log = Logger.getLogger(FeedsSoapServer.class.getName());

    protected FeedsSoapServer() {
        super(Log, Feeds.SERVICE_NAME, PORT, new SoapFeedsWebService());
    }

    public static void main(String[] args) throws Exception {
        Domain.set( args.length > 0 ? args[0] : "");
        ServerMIDToken.set( args.length > 1 ? args[1] : "");

        new FeedsSoapServer().start();
    }
}
