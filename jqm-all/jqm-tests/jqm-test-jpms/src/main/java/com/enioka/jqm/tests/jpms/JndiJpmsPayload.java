package com.enioka.jqm.tests.jpms;

import java.io.File;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

public class JndiJpmsPayload implements Runnable
{
    public void run()
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
