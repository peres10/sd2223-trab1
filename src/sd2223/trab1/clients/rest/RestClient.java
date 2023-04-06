package sd2223.trab1.clients.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.checkerframework.checker.units.qual.C;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import sd2223.trab1.api.service.java.Result;
import sd2223.trab1.clients.ClientFactory;
import sd2223.trab1.clients.common.RetryClient;

import java.net.URI;

import static sd2223.trab1.api.service.java.Result.error;
import static sd2223.trab1.api.service.java.Result.ok;

public class RestClient extends RetryClient {
    protected final URI uri;
    protected final Client client;
    protected final ClientConfig config;
    protected final WebTarget target;

    public RestClient(URI uri, String path){
        this.uri = uri;
        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);
        this.target = this.client.target(uri).path(path);
    }

    protected <T> Result<T> toJavaResult(Response r, GenericType<T> genericType) {
        try {
            var status = r.getStatusInfo().toEnum();
            if (status == Response.Status.OK && r.hasEntity())
                return ok(r.readEntity(genericType));
            else if (status == Response.Status.NO_CONTENT) return ok();

            return error(getErrorCodeFrom(status.getStatusCode()));
        } finally{
            r.close();
        }
    }

    private static Result.ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> Result.ErrorCode.OK;
            case 409 -> Result.ErrorCode.CONFLICT;
            case 403 -> Result.ErrorCode.FORBIDDEN;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 400 -> Result.ErrorCode.BAD_REQUEST;
            case 500 -> Result.ErrorCode.INTERNAL_ERROR;
            case 501 -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
