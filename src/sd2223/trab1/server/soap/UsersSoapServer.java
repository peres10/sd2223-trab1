package sd2223.trab1.server.soap;

import sd2223.trab1.api.java.Users;
import sd2223.trab1.server.util.Domain;

import java.util.logging.Logger;

public class UsersSoapServer extends AbstractSoapServer{

    public static final int PORT = 9500;
    private static Logger Log = Logger.getLogger(UsersSoapServer.class.getName());

    protected UsersSoapServer() {
        super(Log, Users.SERVICE_NAME, PORT, new SoapUsersWebService());
    }

    public static void main(String[] args) throws Exception {
        Domain.set( args.length > 0 ? args[0] : "");

        new UsersSoapServer().start();
    }
}
