package com.enioka.jqm.api;

/**
 * A Binder is a class inside a client implementation that gives the name of a class implementing {@link IClientFactory}. Every client
 * implementation should have a class named <code>com.enioka.jqm.api.StaticClientBinder</code> implementing this interface for the static
 * binding system to work. This system was copied from slf4j.
 */
interface IClientFactoryBinder
{
    /**
     * Return the {@link IClientFactory} that the {@link JqmClientFactory} should bind to.
     * 
     * @return
     */
    public IClientFactory getClientFactory();

    /**
     * @return the class name of the intended {@link IClientFactory} instance
     */
    public String getClientFactoryName();
}
