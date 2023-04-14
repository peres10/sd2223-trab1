package sd2223.trab1.server.rest;

import jakarta.inject.Singleton;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.server.common.JavaUsers;

import java.util.List;
import java.util.logging.Logger;

@Singleton
public class UsersResources extends RestResource implements UsersService {
   private static Logger Log = Logger.getLogger(UsersResources.class.getName());

   final Users impl;
   public UsersResources(){
       this.impl = new JavaUsers();
   }

   @Override
    public String createUser(User user) {
       Log.info("REST: createUser : " + user);
       return super.fromJavaResult( impl.createUser( user) );
    }

    @Override
    public User getUser(String name, String pwd) {
        Log.info(String.format("REST: getUser : name = %s \n",name));
        return super.fromJavaResult( impl.getUser(name, pwd) );
    }

    @Override
    public User updateUser(String name, String pwd, User user) {
        Log.info(String.format("REST: updateUser : name = %s new user data = ",name)+user);
        return super.fromJavaResult( impl.updateUser( name, pwd , user) );
    }

    @Override
    public User deleteUser(String name, String pwd) {
       Log.info(String.format("REST: deleteUser : name = %s \n",name));
        return super.fromJavaResult( impl.deleteUser( name, pwd) );
    }

    @Override
    public List<User> searchUsers(String pattern) {
        Log.info(String.format("REST: searchUsers : pattern = \n",pattern));
        return super.fromJavaResult( impl.searchUsers( pattern ) );
    }

    @Override
    public User findUser(String name) {
       Log.info(String.format("REST: findUser : %s \n",name));
       return super.fromJavaResult( impl.findUser(name));
    }

}
