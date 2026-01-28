package com.enioka.jqm.client.jdbc.api;

import java.util.Properties;

import org.kohsuke.MetaInfServices;

import com.enioka.jqm.client.api.IClientFactory;
import com.enioka.jqm.client.api.JqmClient;
import com.enioka.jqm.client.shared.IDbClientFactory;

/**
 * The entry point to create a {@link JqmClient} that will be able to interact with JQM.<br>
 * {@link JqmClient}s should never be created outside of this factory.<br>
 * The factory also holds the client cache - clients are cached to avoid creating useless objects and connections. (it is possible to create
 * a non-cached client but this is not the default)
 */
@MetaInfServices({ IDbClientFactory.class, IClientFactory.class })
public class JqmClientFactory implements IDbClientFactory
{
    /**
     * Return the default client. Note this client is shared in the static context. (said otherwise: the same client is always returned
     * inside a same class loading context). The initialization cost is only paid at first call.
     *
     * @param properties the properties to configure the client.
     *
     * @return the default client
     */
    public JqmClient getClient(Properties properties)
    {
        return new JdbcClient(properties);
    }
}
