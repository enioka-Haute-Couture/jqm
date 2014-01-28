package com.enioka.jqm.api;

class StaticClientBinder implements IClientFactoryBinder
{
    private static final StaticClientBinder SINGLETON = new StaticClientBinder();

    public static StaticClientBinder getSingleton()
    {
        return SINGLETON;
    }

    @Override
    public IClientFactory getClientFactory()
    {
        return new ClientFactory();
    }

    @Override
    public String getClientFactoryName()
    {
        return "hibernate";
    }

}
