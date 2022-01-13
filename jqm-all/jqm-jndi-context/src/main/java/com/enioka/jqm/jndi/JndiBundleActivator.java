package com.enioka.jqm.jndi;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JNDI context can only be set once, so this bundle cannot really be unloaded (except when cheating with reflection).
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
            // Release as many resources as possible from the JRE singleton.
            ctx.resetSingletons();

            // And then try to release said singleton even if not officialy possible.
            tryResetInitialContextFactoryBuilder();
        }
    }

    /**
     * A very dirty hack allowing to reset the InitialContextFactoryBuilder while it is explicitely forbidden by the JRE.<br>
     * Useful in tests where we need a clean environment. Not needed in production where we do not support hot-reload anyway.
     */
    private void tryResetInitialContextFactoryBuilder()
    {
        try
        {
            for (Field field : NamingManager.class.getDeclaredFields())
            {
                if (InitialContextFactoryBuilder.class.equals(field.getType()))
                {
                    field.setAccessible(true);
                    field.set(null, null);
                    jqmlogger.debug("Reset attempt done on InitialContextFactoryBuilder");
                }
            }
        }
        catch (InaccessibleObjectException e)
        {
            jqmlogger.info(
                    "Cannot access singleton InitialContextFactoryBuilder on this JRE - JNDI context cannot be properly unloaded but it is only an issue during tests");
        }
        catch (Throwable t)
        {
            jqmlogger.warn(
                    "Unexpected error resetting InitialContextFactoryBuilder. Only an issue in tests but please report it toi maintainer",
                    t);
        }
    }
}
