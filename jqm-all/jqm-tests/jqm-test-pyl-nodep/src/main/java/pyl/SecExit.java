package pyl;

public class SecExit
{
    public static void main(String[] args)
    {
        System.out.println("Trying to kill the JVM from inside a payload now!");
        System.exit(1);
        System.out.println("Ooops... no exception triggered and JVM still alive.");
    }
}
