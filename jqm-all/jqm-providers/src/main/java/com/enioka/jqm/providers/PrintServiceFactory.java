package com.enioka.jqm.providers;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

public class PrintServiceFactory implements ObjectFactory
{
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
    {
        return new PrintServiceImpl();
    }

    /**
     * Helper method to avoid instantiating a factory to create a service. Should be used in non-JNDI environments.
     */
    public static PrintService getService()
    {
        return new PrintServiceImpl();
    }
}
