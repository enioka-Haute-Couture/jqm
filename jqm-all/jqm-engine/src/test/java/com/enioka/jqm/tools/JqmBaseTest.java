package com.enioka.jqm.tools;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jndi.JndiContext;
import com.enioka.jqm.jndi.JndiContextFactory;

public class JqmBaseTest
{
    public static Logger jqmlogger = Logger.getLogger(JqmBaseTest.class);
    public static Server s;

    @BeforeClass
    public static void testInit() throws Exception
    {
        JndiContextFactory.createJndiContext();
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();
    }

    @AfterClass
    public static void stop() throws NamingException
    {
        JqmClientFactory.resetClient(null);
        Helpers.resetEmf();
        ((JndiContext) NamingManager.getInitialContext(null)).resetSingletons();
        s.shutdown();
        s.stop();
    }
}
