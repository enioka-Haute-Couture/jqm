package com.enioka.jqm.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class ClientFactory implements IClientFactory
{
    private static JqmClient defaultClient;
    private static ConcurrentMap<String, JqmClient> clients = new ConcurrentHashMap<String, JqmClient>();

    @Override
    public JqmClient getClient()
    {
        synchronized (clients)
        {
            if (defaultClient == null)
            {
                defaultClient = new HibernateClient();
            }
            return defaultClient;
        }
    }

    @Override
    public JqmClient getClient(String name)
    {
        synchronized (clients)
        {
            clients.putIfAbsent(name, new HibernateClient());
            return clients.get(name);
        }
    }

    @Override
    public void resetClient(String name)
    {
        if (name != null)
        {
            synchronized (clients)
            {
                if (clients.containsKey(name))
                {
                    clients.get(name).dispose();
                    clients.remove(name);
                }
            }
        }
        else
        {
            synchronized (clients)
            {
                if (defaultClient != null)
                {
                    defaultClient.dispose();
                    defaultClient = null;
                }
            }
        }
    }

}
