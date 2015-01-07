package pyl;

import java.io.File;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

public class JndiFile
{
    public static void main(String[] args)
    {
        File o;
        try
        {
            o = (File) NamingManager.getInitialContext(null).lookup("fs/test");
        }
        catch (NamingException e)
        {
            throw new RuntimeException("could not get the test directory", e);
        }
        System.out.println(o.getAbsolutePath());
    }
}
