package sd2223.trab1.server.rest;


import jakarta.annotation.Resource;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.server.common.AbstractServer;
import sd2223.trab1.server.resources.Discovery;
import sd2223.trab1.server.util.Token;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public abstract class AbstractRestServer extends AbstractServer{

    protected static String SERVER_URI_FMT = "http://%s:%s/rest";

    protected AbstractRestServer(Logger log, String service, int port){
        super(log, service, port);
    }

    protected void start() throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(SERVER_URI_FMT, ip, port);
        String serviceNameWDomain = String.format("%s:%s", Token.get(), service);

        ResourceConfig config = new ResourceConfig();

        registerResources ( config );

        JdkHttpServerFactory.createHttpServer(URI.create(serverURI.replace(ip, INETADDR_ANY)), config);

        Log.info(String.format("%s Server ready @ %s\n", service, serverURI));

        Discovery.getInstance().announce(serviceNameWDomain, serverURI);
    }

    abstract void registerResources( ResourceConfig config );
}
