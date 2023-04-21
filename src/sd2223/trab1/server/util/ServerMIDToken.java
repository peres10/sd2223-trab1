package sd2223.trab1.server.util;

public class ServerMIDToken {

    private static String token;

    public static void set(String t){
        token = t;
    }

    public static String get(){
        return token ==null ? "" : token;
    }

    public static long tokenValue() {
        return Long.parseLong(token) ;
    }
}
