package com.enioka.jqm.providers;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class StringFactory implements ObjectFactory
{
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
    {
        Reference resource = (Reference) obj;
        if (resource.get("STRING") != null)
        {
            String res = (String) resource.get("STRING").getContent();
            return res;
        }
        else
        {
            throw new NamingException("Resource does not have a valid STRING parameter");
        }
    }
}
