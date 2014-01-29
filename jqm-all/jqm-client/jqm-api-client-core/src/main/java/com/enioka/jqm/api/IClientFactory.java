package com.enioka.jqm.api;

import java.util.Properties;

interface IClientFactory
{
    /**
     * Return the default client. Note this client is shared in the static context. (said otherwise: the same client is always returned
     * inside a same class loading context). The initialization cost is only paid at first call.
     * 
     * @return the default client
     */
    public JqmClient getClient();

    /**
     * Return a named client. Note this client is shared in the static context. (said otherwise: for a given name, the same client is always
     * returned inside a same class loading context)<br>
     * This is helpful when multiple clients are needed, such as in some multithreading scenarios. Otherwise, just use the default client
     * return by {@link #getClient()}
     * 
     * @return the required client
     */
    public JqmClient getClient(String name, Properties props);

    /**
     * Will remove the client of the given name from the static cache. Next time {@link #getClient(String)} is called, initialization cost
     * will have to be paid once again.<br>
     * Use <code>null</code> to reset the default client.<br>
     * This method is mostly useful for tests when databases are reset and therefore clients become invalid as they hold connections to
     * them.<br>
     * If the name does not exist, no exception is thrown.
     * 
     * @param name
     *            the client to reset, or <code>null</code> for the default client
     */
    public void resetClient(String name);
}
