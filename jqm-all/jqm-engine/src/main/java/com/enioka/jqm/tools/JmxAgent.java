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
package com.enioka.jqm.tools;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.Semaphore;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.spi.NamingManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JMX Agent is (JVM-wide) RMI for serving remote JMX requests. It is compulsory because JQM uses fixed ports for the JMX server.
 */
final class JmxAgent
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JmxAgent.class);
    private static boolean init = false;

    private static JMXConnectorServer connectorServer;
    private static Semaphore startRequests = new Semaphore(0);

    private JmxAgent()
    {
        // Utility class
    }

    static synchronized void registerAgent(int registryPort, int serverPort, String hostname) throws JqmInitError
    {
        startRequests.release(1);
        if (init)
        {
            // The agent is JVM-global, not engine specific, so prevent double start.
            return;
        }

        jqmlogger.trace("registering remote agent");
        try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            JndiContext ctx = (JndiContext) NamingManager.getInitialContext(null);
            ctx.registerRmiContext(LocateRegistry.createRegistry(registryPort));

            JMXServiceURL url = new JMXServiceURL(
                    "service:jmx:rmi://" + hostname + ":" + serverPort + "/jndi/rmi://" + hostname + ":" + registryPort + "/jmxrmi");

            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
            connectorServer.start();
            init = true;
            jqmlogger.info("The JMX remote agent was registered. Connection string is " + url);
        }
        catch (Exception e)
        {
            throw new JqmInitError("Could not create remote JMX agent", e);
        }
    }

    /**
     * JMX agent runs as a non-daemon thread, so it prevents JVM from stoppings. It must be stopped manually when engines die. In most
     * cases, this is not needed as the agent itself has a shutodwn hook, but this hook does not trigger when JVM stopping comes from inside
     * the engine and not CTRL+C or equivalent.
     */
    static synchronized void unregisterAgentIfNoMoreNodes()
    {
        if (!init || startRequests.availablePermits() == 0)
        {
            return;
        }

        // Only stop the agent when there are no more engines started.
        try
        {
            startRequests.acquire();
        }
        catch (InterruptedException e1)
        {
            Thread.currentThread().interrupt();
            return; // Actually ignore.
        }
        if (startRequests.availablePermits() > 0)
        {
            // Still at least one other node running, do not stop the JMX agent.
            return;
        }

        if (connectorServer.isActive())
        {
            try
            {
                jqmlogger.info("Stopping JMX agent");
                connectorServer.stop();
            }
            catch (IOException e)
            {
                throw new JqmRuntimeException("Could not stop remote JMX agent", e);
            }
        }
        init = false;
    }
}
