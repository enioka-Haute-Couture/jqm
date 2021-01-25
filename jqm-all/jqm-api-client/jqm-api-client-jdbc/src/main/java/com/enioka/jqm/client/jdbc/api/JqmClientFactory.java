package com.enioka.jqm.client.jdbc.api;

import java.util.Properties;

import com.enioka.jqm.client.api.JqmClient;
import com.enioka.jqm.client.shared.BaseJqmClientFactory;

/**
 * The entry point to create a {@link JqmClient} that will be able to interact with JQM.<br/>
 * {@link JqmClient}s should never be created outside of this factory.<br>
 * The factory also holds the client cache - clients are cached to avoid creating useless objects and connections. (it is possible to create
 * a non-cached client but this is not the default)
 */
public class JqmClientFactory
{
    private static BaseJqmClientFactory<JdbcClient> baseFactory = new BaseJqmClientFactory<JdbcClient>((Properties p) -> {
        return new JdbcClient(p);
    });

    /**
     * Return the default client. Note this client is shared in the static context. (said otherwise: the same client is always returned
     * inside a same class loading context). The initialization cost is only paid at first call.
     *
     * @return the default client
     */
    public static JqmClient getClient()
    {
        return baseFactory.getClient(null, null, true);
    }

    /**
     * Return a new client that may be cached or not. Given properties are always used when not cached, and only used at creation time for
     * cached clients. For advanced use - usually {@link #getClient()} is used instead for retrieving the default cached client.
     *
     * @param name
     *            if null, default client. Otherwise, helpful to retrieve cached clients later. Ignored if <code>cached</code> is false.
     * @param p
     *            a set of properties. Implementation specific. Unknown properties are silently ignored.
     * @param cached
     *            if false, the client will not be cached and subsequent calls with the same name will return different objects.
     */
    public static JqmClient getClient(String name, Properties p, boolean cached)
    {
        return baseFactory.getClient(name, p, cached);
    }

    /**
     * Most client providers use a specific configuration file. However, it may be desired to overload these values with runtime values.
     * This method enables a client to specify these values.<br>
     * <strong>Note that the parameter names depend on the client provider!</strong><br>
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
        baseFactory.setProperties(properties);
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
        baseFactory.setProperty(key, value);
    }

    /**
     * Remove the client of the given name from the cache. Next time {@link #getClient(String, Properties, boolean)} is called,
     * initialization cost will have to be paid once again.<br>
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
        baseFactory.resetClient(name);
    }

    /**
     * This is {@link #resetClient(String)} with name = null (i.e. reset the default client).
     */
    public static void resetClient()
    {
        resetClient(null);
    }
}
