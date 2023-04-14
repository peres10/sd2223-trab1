package sd2223.trab1.clients;

import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.common.RetryUsersClient;
import sd2223.trab1.clients.rest.RestUsersClient;
import sd2223.trab1.clients.soap.SoapUsersClient;

public class Clients {
    public static final ClientFactory<Users> UsersClients = new ClientFactory<>(
            Users.SERVICE_NAME,
            (u) -> new RetryUsersClient(new RestUsersClient(u)),
            (u) -> new RetryUsersClient(new SoapUsersClient(u))
    );
}
