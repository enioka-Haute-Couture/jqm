package com.enioka.jqm.providers;

import java.net.URL;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class UrlFactory implements ObjectFactory
{
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
    {
        Reference resource = (Reference) obj;
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
