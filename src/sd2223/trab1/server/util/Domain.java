package sd2223.trab1.server.util;

public class Domain {

    private static String domain;

    public static void set(String t){
        domain = t;
    }

    public static String get(){
        return domain ==null ? "" : domain;
    }

}
