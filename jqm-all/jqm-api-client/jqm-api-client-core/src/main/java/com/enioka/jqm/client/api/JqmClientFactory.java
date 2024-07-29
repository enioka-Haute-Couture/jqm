package com.enioka.jqm.client.api;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.shared.exceptions.JqmMissingPluginException;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;

public final class JqmClientFactory
{
    protected static Logger jqmlogger = LoggerFactory.getLogger(JqmClientFactory.class);

    private static Properties props = new Properties();
    private static Map<Class<? extends IClientFactory>, IClientFactory> factories = new HashMap<>();

    private static ConcurrentMap<String, JqmClient> clientCache = new ConcurrentHashMap<String, JqmClient>();
    private static Map<Class<? extends IClientFactory>, JqmClient> defaultClientCache = new HashMap<>();

    private static IClientFactory getFactoryFromServiceLoader(Class<? extends IClientFactory> clazz)
    {
        if (factories.containsKey(clazz))
        {
            return factories.get(clazz);
        }

        IClientFactory factory;
        try
        {
            factory = ServiceLoaderHelper.getService(ServiceLoader.load(clazz));
            factories.put(clazz, factory);
        }
        catch (JqmMissingPluginException e)
        {
            throw new JqmClientException("Could not find any JQM client provider", e);
        }
        return factory;
    }

    private JqmClientFactory()
    {
        // Utility class
    }

    /**
     * Most client providers use a specific configuration file. However, it may be desired to overload these values with runtime values.
     * This method enables a client to specify these values.<br>
     * <strong>Note that the parameter names depend on the provider!</strong><br>
     * Moreover, changing the properties only impact <code>JqmClient</code>s created after the modification. It is important to keep in mind
     * that created <code>JqmClient</code>s are cached - therefore it is customary to use this function <strong>before creating any
     * clients</strong>.
     *
     * @param properties
     *            a non null property bag
     * @throws InvalidParameterException
     *             if props is null
     */
    public static void setProperties(Properties properties)
    {
        if (props == null)
        {
            throw new InvalidParameterException("props cannot be null");
        }
        JqmClientFactory.props = properties;
    }

    /**
     * See {@link #setProperties(Properties)}
     *
     * @param key
     *            a non null non empty parameter key.
     * @param value
     *            value of the parameter.
     */
    public static void setProperty(String key, Object value)
    {
        if (props == null)
        {
            throw new IllegalStateException("properties are null");
        }
        if (key == null || key.isEmpty())
        {
            throw new InvalidParameterException("key cannot be empty or null");
        }
        props.put(key, value);
    }

    public static void removeProperty(String key)
    {
        if (props == null)
        {
            return;
        }
        props.remove(key);
    }

    /**
     * Return the default client. Note this client is shared in the static context. (said otherwise: the same client is always returned
     * inside a same class loading context). The initialization cost is only paid at first call.
     *
     * @return the default client
     */
    public static JqmClient getClient()
    {
        return getClient(null, null, true, IClientFactory.class);
    }

    /**
     * Return the default client for a specific subtype of JQM client. Note this client is shared in the static context. (said otherwise:
     * the same client is always returned inside a same class loading context). The initialization cost is only paid at first call.
     *
     * @return the default client for this client subtype.
     */
    public static JqmClient getClient(Class<? extends IClientFactory> clazz)
    {
        return getClient(null, null, true, clazz);
    }

    /**
     * Return a new client that may be cached or not. Given properties are always use when not cached, and only used at creation time for
     * cached clients.
     *
     * @param name
     *            if null, default client. Otherwise, helpful to retrieve cached clients later.
     * @param p
     *            a set of properties. Implementation specific. Unknown properties are silently ignored.
     * @param cached
     *            if false, the client will not be cached and subsequent calls with the same name will return different objects.
     */
    public static JqmClient getClient(String name, Properties p, boolean cached, Class<? extends IClientFactory> clazz)
    {
        Properties p2 = null;
        if (p == null)
        {
            p2 = props;
        }
        else
        {
            p2 = new Properties(props);
            p2.putAll(p);
        }

        var factory = getFactoryFromServiceLoader(clazz);

        if (!cached)
        {
            return factory.getClient(p2);
        }

        synchronized (clientCache)
        {
            if (name == null)
            {
                if (defaultClientCache.get(clazz) == null)
                {
                    jqmlogger.trace("creating default client");
                    defaultClientCache.put(clazz, factory.getClient(p2));
                }
                return defaultClientCache.get(clazz);
            }
            else
            {
                clientCache.putIfAbsent(name, factory.getClient(p2));
                return clientCache.get(name);
            }
        }
    }

    /**
     * Close and remove all cached clients.
     */
    public static void reset()
    {
        synchronized (clientCache)
        {
            for (var c : clientCache.values())
            {
                c.dispose();
            }
            clientCache.clear();

            for (var c : defaultClientCache.values())
            {
                c.dispose();
            }
            defaultClientCache.clear();
        }
    }
}
