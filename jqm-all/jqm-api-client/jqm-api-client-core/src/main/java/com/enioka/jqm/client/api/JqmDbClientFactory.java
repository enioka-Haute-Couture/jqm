package com.enioka.jqm.client.api;

import java.util.Properties;

import com.enioka.jqm.client.shared.IDbClientFactory;

/**
 * A specialized version of JqmClientFactory only using clients that connect to the database. Only useful if both clients are present on the
 * class path, as in tests - otherwise use JqmClientFactory directly.
 */
public class JqmDbClientFactory
{
    /**
     * Default constructor.
     */
    public JqmDbClientFactory(){}

    /**
     * Set the properties for the database client factory.
     * @param properties the properties to set
     */
    public static void setProperties(Properties properties)
    {
        JqmClientFactory.setProperties(properties);
    }

    /**
     * Set a single property for the database client factory.
     * @param key the key of the property to set
     * @param value the value of the property to set
     */
    public static void setProperty(String key, Object value)
    {
        JqmClientFactory.setProperty(key, value);
    }

    /**
     * Get a database client.
     * @return a database client
     */
    public static JqmClient getClient()
    {
        return JqmClientFactory.getClient(null, null, true, IDbClientFactory.class);
    }

    /**
     * Get a database client.
     * @param name name of the client to get
     * @param p properties to use for the client
     * @param cached whether the client should be cached or not
     * @return a database client
     */
    public static JqmClient getClient(String name, Properties p, boolean cached)
    {
        return JqmClientFactory.getClient(name, p, cached, IDbClientFactory.class);
    }

    /**
     * Reset the database client factory.
     */
    public static void reset()
    {
        JqmClientFactory.reset();
    }
}
