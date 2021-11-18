package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;

import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JNDI context can only be set once, so this bundle cannot really be unloaded. During tests, this means the first one loaded always
 * wins.
 */
@Header(name = org.osgi.framework.Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class JndiBundleActivator implements BundleActivator
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JndiBundleActivator.class);
    private static JndiContext ctx;

    @Override
    public void start(BundleContext context) throws Exception
    {
        try
        {
            if (!NamingManager.hasInitialContextFactoryBuilder())
            {
                ctx = new JndiContext();
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
        if (ctx != null)
        {
            ctx.resetSingletons();
        }
    }
}
