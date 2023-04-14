package sd2223.trab1.clients;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import sd2223.trab1.server.resources.Discovery;

import java.net.URI;
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

    public T get ( String fullServiceName){
        URI[] uris = Discovery.getInstance().knownUrisOf(fullServiceName,1);
        return getByUri(uris[0].toString());
    }

    public T getByUri(String uriString){
        try{
            return clients.get(URI.create(uriString));
        } catch (Exception x){
            x.printStackTrace();
        }
        return null;
    }
}
