package sd2223.trab1.clients;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import sd2223.trab1.api.service.java.Result;
import sd2223.trab1.api.service.java.Result.ErrorCode;
import sd2223.trab1.server.common.JavaFeeds;
import sd2223.trab1.server.resources.Discovery;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Logger;

public class ClientFactory<T>{

    final static Logger Log = Logger.getLogger(ClientFactory.class.getName());
    private static final String REST = "/rest";
    private static final String SOAP = "/soap";

    private final String serviceName;
    private final Function<URI,T> restClient;
    private final Function<URI,T> soapClient;

    ClientFactory(String serviceName, Function<URI, T> restClient,Function<URI, T> soapClient ){
        this.serviceName = serviceName;
        this.restClient = restClient;
        this.soapClient = soapClient;
    }

    LoadingCache<URI, T> clients = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public T load(URI uri) throws Exception {
                    T client;
                    if (uri.toString().endsWith(REST))
                        client = restClient.apply(uri);
                    else if (uri.toString().endsWith(SOAP))
                        client = soapClient.apply(uri);
                    else
                        throw new RuntimeException("Unknown service type..." + uri);
                    return client;
                }
            });

    public T get(){
        URI[] uris = Discovery.getInstance().knownUrisOf(serviceName, 1);
        return get(uris[0]);
    }

    public T get(URI uri){
        try{
            return clients.get(uri);
        } catch (ExecutionException e) {
            throw new RuntimeException(ErrorCode.INTERNAL_ERROR.toString());
        }
    }

    public T get(String urlString){
        var i = urlString.indexOf(serviceName);
        return this.get( URI.create(urlString.substring(0, i-1)));
    }
}
