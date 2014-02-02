package com.enioka.jqm.api;

import java.util.Properties;
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
    public JqmClient getClient(String name, Properties props)
    {
        if (props == null)
        {
            props = new Properties();
        }

        synchronized (clients)
        {
            if (name == null)
            {
                if (defaultClient == null)
                {
                    defaultClient = new HibernateClient(props);
                }
                return defaultClient;
            }
            else
            {
                clients.putIfAbsent(name, new HibernateClient(props));
                return clients.get(name);
            }
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
