package sd2223.trab1.server.common;

import sd2223.trab1.api.User;

public class JavaCommonMethods {
    protected static <T> boolean nullValue(T val ){
        return val == null;
    }

    protected static boolean badUser( User user ){
        return user==null || nullValue(user.getName()) || nullValue(user.getPwd())
                || nullValue(user.getDisplayName()) || nullValue(user.getDomain()) ;
    }

    protected static boolean wrongPwd( User user, String pwd){
        return !user.getPwd().equals(pwd);
    }
}
