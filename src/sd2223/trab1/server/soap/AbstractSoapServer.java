package sd2223.trab1.server.soap;

import jakarta.xml.ws.Endpoint;
import sd2223.trab1.server.common.AbstractServer;
import sd2223.trab1.server.resources.Discovery;
import sd2223.trab1.server.util.Token;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class AbstractSoapServer extends AbstractServer {

    protected static String SERVER_URI_FMT = "http://%s:%s/soap";

    final Object impl;

    protected AbstractSoapServer(Logger log, String service, int port, Object impl) {
        super(log, service, port);
        this.impl = impl;
    }

    @Override
    protected void start() throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(SERVER_URI_FMT, ip, port);
        String serviceNameWDomain = String.format("%s:%s", Token.get(), service);

        Endpoint.publish(serverURI.replace(ip,INETADDR_ANY),impl);

        Log.info(String.format("%s Server ready @ %s\n", service, serverURI));

        Discovery.getInstance().announce(serviceNameWDomain, serverURI);
    }
}
