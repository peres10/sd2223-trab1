package sd2223.trab1.clients;

import sd2223.trab1.api.service.java.Users;
import sd2223.trab1.clients.common.RetryUsersClient;
import sd2223.trab1.clients.rest.RestUsersClient;
import sd2223.trab1.clients.soap.SoapUsersClient;
import sd2223.trab1.server.common.JavaFeeds;

import java.util.logging.Logger;

public class Clients {
    public static final ClientFactory<Users> UsersClients = new ClientFactory<>(
            Users.SERVICE_NAME,
            (u) -> new RetryUsersClient(new RestUsersClient(u)),
            (u) -> new RetryUsersClient(new SoapUsersClient(u))
    );
}
