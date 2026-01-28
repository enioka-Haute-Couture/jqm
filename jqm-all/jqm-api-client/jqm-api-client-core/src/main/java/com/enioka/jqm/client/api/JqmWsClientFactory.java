package com.enioka.jqm.client.api;

import java.util.Properties;

import com.enioka.jqm.client.shared.IWsClientFactory;

/**
 * A specialized version of JqmClientFactory only using clients that connect to the JQM web services. Only useful if both clients are
 * present on the class path, as in tests - otherwise use JqmClientFactory directly.
 */
public class JqmWsClientFactory
{
    /**
     * Default Constructor.
     */
    public JqmWsClientFactory(){}

    /**
     * Set the properties to use for the web service client factory.
     * @param properties the properties to use
     */
    public static void setProperties(Properties properties)
    {
        JqmClientFactory.setProperties(properties);
    }

    /**
     * Set a single property for the web service client factory.
     * @param key the property key
     * @param value the property value
     */
    public static void setProperty(String key, Object value)
    {
        JqmClientFactory.setProperty(key, value);
    }

    /**
     * Get a web service client.
     * @return a web service client
     */
    public static JqmClient getClient()
    {
        return JqmClientFactory.getClient(null, null, true, IWsClientFactory.class);
    }

    /**
     * Get a web service client.
     * @param name name of the client to get
     * @param p properties to use for the client
     * @param cached whether to cache the client
     * @return a web service client
     */
    public static JqmClient getClient(String name, Properties p, boolean cached)
    {
        return JqmClientFactory.getClient(name, p, cached, IWsClientFactory.class);
    }

    /**
     * Reset the web service client factory.
     */
    public static void reset()
    {
        JqmClientFactory.reset();
    }
}
