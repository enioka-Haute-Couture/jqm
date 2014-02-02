package com.enioka.jqm.api;

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
