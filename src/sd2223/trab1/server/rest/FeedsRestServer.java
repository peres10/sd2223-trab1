package sd2223.trab1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.api.service.java.Feeds;
import sd2223.trab1.server.util.CustomLoggingFilter;
import sd2223.trab1.server.util.Token;

import java.util.logging.Logger;

public class FeedsRestServer extends AbstractRestServer{

    public static final int PORT = 8085;

    private static Logger Log = Logger.getLogger(FeedsRestServer.class.getName());

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
        Log.info("Antes do start do server\n");
        Token.set( args.length > 0 ? args[0] : "");
        new FeedsRestServer().start();
        Log.info("Deu start do server\n");
    }
}
