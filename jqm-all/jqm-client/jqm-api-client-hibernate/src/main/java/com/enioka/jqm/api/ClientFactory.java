/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enioka.jqm.api;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ClientFactory implements IClientFactory
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ClientFactory.class);
    private static JqmClient defaultClient;
    private static ConcurrentMap<String, JqmClient> clients = new ConcurrentHashMap<String, JqmClient>();

    @Override
    public JqmClient getClient()
    {
        return getClient(null, null, true);
    }

    @Override
    public JqmClient getClient(String name, Properties props, boolean cached)
    {
        if (props == null)
        {
            props = new Properties();
        }

        if (!cached)
        {
            return new HibernateClient(props);
        }

        synchronized (clients)
        {
            if (name == null)
            {
                if (defaultClient == null)
                {
                    jqmlogger.trace("creating default client");
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
                    jqmlogger.trace("resetting client " + name);
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
                    jqmlogger.trace("resetting default client");
                    defaultClient.dispose();
                    defaultClient = null;
                }
            }
        }
    }

}
