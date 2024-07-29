package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jndi.api.JqmJndiContextControlService;

@MetaInfServices(JqmJndiContextControlService.class)
public class JndiContextControlService implements JqmJndiContextControlService
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JndiContextControlService.class);

    private static JndiContext context = null;

    @Override
    public void registerIfNeeded()
    {
        try
        {
            if (!NamingManager.hasInitialContextFactoryBuilder())
            {
                context = new JndiContext();
                NamingManager.setInitialContextFactoryBuilder(context);
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
    public void reset()
    {
        if (context != null)
        {
            // Release as many resources as possible from the JRE singleton.
            context.resetSingletons();
        }
    }
}
