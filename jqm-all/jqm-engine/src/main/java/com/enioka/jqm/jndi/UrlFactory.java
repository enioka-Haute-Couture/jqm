package com.enioka.jqm.jndi;

import java.net.URL;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

class UrlFactory implements ObjectFactory
{
    UrlFactory()
    {

    }

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
    {
        JndiResourceDescriptor resource = (JndiResourceDescriptor) obj;
        if (resource.get("URL") != null)
        {
            String url = (String) resource.get("URL").getContent();
            return new URL(url);
        }
        else
        {
            throw new NamingException("Resource does not have a valid URL parameter");
        }
    }
}
