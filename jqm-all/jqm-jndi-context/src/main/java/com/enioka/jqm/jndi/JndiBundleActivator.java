package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JndiBundleActivator implements BundleActivator
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JndiBundleActivator.class);

    @Override
    public void start(BundleContext context) throws Exception
    {
        try
        {
            if (!NamingManager.hasInitialContextFactoryBuilder())
            {
                JndiContext ctx = new JndiContext();
                NamingManager.setInitialContextFactoryBuilder(ctx);
            }
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not create JNDI context: " + e.getMessage());
            RuntimeException ex = new RuntimeException("Could not initialize JNDI Context", e);
            throw ex;
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
    }

}
