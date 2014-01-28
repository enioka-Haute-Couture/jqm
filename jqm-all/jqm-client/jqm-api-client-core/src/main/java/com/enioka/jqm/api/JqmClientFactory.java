package com.enioka.jqm.api;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * More than heavily inspired by slf4j.
 * 
 */
public class JqmClientFactory
{
    private static String STATIC_CLIENT_BINDER_PATH = "com/enioka/jqm/api/StaticClientBinder.class";
    private static String STATIC_CLIENT_BINDER_NAME = "com.enioka.jqm.api.StaticClientBinder";
    private static IClientFactoryBinder binder;

    private final static Set<URL> findClientBinders()
    {
        Set<URL> res = new LinkedHashSet<URL>();
        try
        {
            ClassLoader loggerFactoryClassLoader = JqmClientFactory.class.getClassLoader();
            Enumeration<URL> paths;
            if (loggerFactoryClassLoader == null)
            {
                paths = ClassLoader.getSystemResources(STATIC_CLIENT_BINDER_PATH);
            }
            else
            {
                paths = loggerFactoryClassLoader.getResources(STATIC_CLIENT_BINDER_PATH);
            }
            while (paths.hasMoreElements())
            {
                URL path = paths.nextElement();
                res.add(path);
            }
        }
        catch (IOException ioe)
        {
            throw new JqmClientException("Error getting resources from path", ioe);
        }
        return res;
    }

    private final static void bind()
    {
        Set<URL> staticClientBinderPathSet = findClientBinders();

        if (staticClientBinderPathSet.size() == 0)
        {
            throw new JqmClientException("there was no client implementation on the classpath");
        }
        if (staticClientBinderPathSet.size() > 1)
        {
            throw new JqmClientException("there were multiple client implementations on the classpath. Only keep the one you want to use");
        }

        try
        {
            @SuppressWarnings("unchecked")
            Class<IClientFactoryBinder> binderClass = (Class<IClientFactoryBinder>) JqmClientFactory.class.getClassLoader().loadClass(
                    STATIC_CLIENT_BINDER_NAME);

            binder = (IClientFactoryBinder) binderClass.getMethod("getSingleton").invoke(null);
        }
        catch (ClassNotFoundException e)
        {
            // Should never happen...
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not load the client provider", e);
        }

    }

    /**
     * Return the default client. Note this client is shared in the static context. (said otherwise: the same client is always returned
     * inside a same class loading context). The initialization cost is only paid at first call.
     * 
     * @return the default client
     */
    public static JqmClient getClient()
    {
        if (binder == null)
        {
            bind();
        }
        return binder.getClientFactory().getClient();
    }

    /**
     * Remove the client of the given name from the static cache. Next time {@link #getClient(String)} is called, initialization cost will
     * have to be paid once again.<br>
     * Use <code>null</code> to reset the default client.<br>
     * This method is mostly useful for tests when databases are reset and therefore clients become invalid as they hold connections to
     * them.<br>
     * If the name does not exist, no exception is thrown.
     * 
     * @param name
     *            the client to reset, or <code>null</code> for the default client
     */
    public static void resetClient(String name)
    {
        if (binder == null)
        {
            bind();
        }
        binder.getClientFactory().resetClient(name);
    }

    /**
     * @see {@link #resetClient(String)} with name = null.
     */
    public static void resetClient()
    {
        resetClient(null);
    }

}
