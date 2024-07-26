package com.enioka.jqm.client.api;

import java.util.Properties;

import com.enioka.jqm.client.shared.IWsClientFactory;

/**
 * A specialized version of JqmClientFactory only using clients that connect to the JQM web services. Only useful if both clients are
 * present on the class path, as in tests - otherwise use JqmClientFactory directly.
 */
public class JqmWsClientFactory
{
    public static void setProperties(Properties properties)
    {
        JqmClientFactory.setProperties(properties);
    }

    public static void setProperty(String key, Object value)
    {
        JqmClientFactory.setProperty(key, value);
    }

    public static JqmClient getClient()
    {
        return JqmClientFactory.getClient(null, null, true, IWsClientFactory.class);
    }

    public static JqmClient getClient(String name, Properties p, boolean cached)
    {
        return JqmClientFactory.getClient(name, p, cached, IWsClientFactory.class);
    }

    public static void reset()
    {
        JqmClientFactory.reset();
    }
}
