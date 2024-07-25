package com.enioka.jqm.webserver.jetty;

import java.io.IOException;

import org.eclipse.jetty.webapp.WebAppClassLoader;

import com.enioka.jqm.cl.ExtClassLoader;

public class IsolatedClassLoader extends WebAppClassLoader
{
    public IsolatedClassLoader(Context context) throws IOException
    {
        super(ExtClassLoader.instance, context);
    }
}
