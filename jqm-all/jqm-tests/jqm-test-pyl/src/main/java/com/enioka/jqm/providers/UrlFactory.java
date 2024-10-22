package com.enioka.jqm.providers;

import java.net.URL;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

public class UrlFactory implements ObjectFactory
{
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
    {
        return new URL("http://houba.hop");
    }
}
