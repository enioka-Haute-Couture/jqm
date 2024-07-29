package com.enioka.jqm.client.api;

import java.util.Properties;

/**
 * This interface must be implemented by all client providers. It simply defines the "create client" interface.
 *
 */
public interface IClientFactory
{
    /**
     * Return the default client. Note this client is shared in the static context. (said otherwise: the same client is always returned
     * inside a same class loading context). The initialization cost is only paid at first call.
     *
     * @return the default client
     */
    JqmClient getClient(Properties properties);
}
