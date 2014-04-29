package com.enioka.jqm.tools;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.spi.NamingManager;

import org.apache.log4j.Logger;


/**
 * The JMX Agent is (JVM-wide) RMI for serving remote JMX requests. It is compulsory because JQM uses fixed ports for the JMX server.
 */
final class JmxAgent
{
    private static Logger jqmlogger = Logger.getLogger(JmxAgent.class);
    private static boolean init = false;

    private JmxAgent()
    {
        // Utility class
    }

    static synchronized void registerAgent(int registryPort, int serverPort, String hostname) throws JqmInitError
    {
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

            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hostname + ":" + serverPort + "/jndi/rmi://" + hostname + ":"
                    + registryPort + "/jmxrmi");

            JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
            cs.start();
            init = true;
            jqmlogger.info("The JMX remote agent was registered. Connection string is " + url);
        }
        catch (Exception e)
        {
            throw new JqmInitError("Could not create remote JMX agent", e);
        }
    }
}
