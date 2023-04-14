package sd2223.trab1.server.soap;

import jakarta.jws.WebService;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.soap.UsersException;
import sd2223.trab1.api.soap.UsersService;
import sd2223.trab1.server.common.JavaUsers;

import java.util.List;
import java.util.logging.Logger;

@WebService(serviceName = UsersService.NAME, targetNamespace = UsersService.NAMESPACE, endpointInterface = UsersService.INTERFACE)
public class SoapUsersWebService extends SoapWebService<UsersException> implements UsersService {
    private static Logger Log = Logger.getLogger(SoapUsersWebService.class.getName());

    final Users impl;
    public SoapUsersWebService() {
        super( (result)-> new UsersException( result.error().toString()));
        this.impl = new JavaUsers();
    }

    @Override
    public String createUser(User user) throws UsersException {
        Log.info("SOAP: createUser : " + user);
        return super.fromJavaResult(impl.createUser(user));
    }

    @Override
    public User getUser(String name, String pwd) throws UsersException {
        Log.info(String.format("SOAP: getUser : name = %s \n",name));
        return super.fromJavaResult(impl.getUser(name,pwd));
    }

    @Override
    public User updateUser(String name, String pwd, User user) throws UsersException {
        Log.info(String.format("SOAP: updateUser : name = %s new user data = "+user,name));
        return super.fromJavaResult(impl.updateUser(name,pwd,user));
    }

    @Override
    public User deleteUser(String name, String pwd) throws UsersException {
        Log.info(String.format("SOAP: deleteUser : name = %s \n",name));
        return super.fromJavaResult( impl.deleteUser( name, pwd) );
    }

    @Override
    public List<User> searchUsers(String pattern) throws UsersException {
        Log.info(String.format("SOAP: searchUsers : pattern = \n",pattern));
        return super.fromJavaResult( impl.searchUsers( pattern ) );
    }

    @Override
    public User findUser(String name) throws UsersException {
        Log.info(String.format("SOAP: findUser : %s \n",name));
        return super.fromJavaResult( impl.findUser(name));
    }
}
