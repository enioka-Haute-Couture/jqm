package com.enioka.jqm.jndi.api;

public interface JqmJndiContextControlService
{
    /**
     * Register the JNDI context if it is not already registered. Once registered, the JNDI context cannot be unregistered.
     */
    void registerIfNeeded();

    /**
     * Clean all singleton resources from the JNDI context.
     */
    void reset();
}
