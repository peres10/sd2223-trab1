package sd2223.trab1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.server.util.CustomLoggingFilter;
import sd2223.trab1.server.util.Token;

import java.util.logging.Logger;

public class FeedsRestServer extends AbstractRestServer{

    public static final int PORT = 8085;

    private static Logger Log = Logger.getLogger(FeedsRestServer.class.getName());

    private static String domain;
    protected FeedsRestServer() {
        super(Log, Feeds.SERVICE_NAME, PORT);
        Log.info("server\n");
    }

    @Override
    void registerResources(ResourceConfig config) {
        config.register(FeedsResources.class);
        config.register(CustomLoggingFilter.class);
    }

    public static void main(String[] args) throws Exception{
        Token.set( args.length > 0 ? args[0] : "");
        new FeedsRestServer().start();
    }
}
