package sd2223.trab1.server.rest;

import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.server.util.CustomLoggingFilter;
import sd2223.trab1.server.util.Domain;

import java.util.logging.Logger;

public class UsersRestServer extends AbstractRestServer{
    public static final int PORT = 8080;

    private static Logger Log = Logger.getLogger(UsersRestServer.class.getName());

    protected UsersRestServer() {
        super(Log, Users.SERVICE_NAME, PORT);
    }

    @Override
    void registerResources(ResourceConfig config) {
        config.register(UsersResources.class);
        config.register(CustomLoggingFilter.class);
    }

    public static void main(String[] args) throws Exception {
        Domain.set( args.length > 0 ? args[0] : "");
        new UsersRestServer().start();
    }
}
