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

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.security.KeyStore;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.naming.spi.NamingManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.pki.JdbcCa;

/**
 * The JMX Agent is (JVM-wide) RMI for serving remote JMX requests. It is
 * compulsory because JQM uses fixed ports for the JMX server.
 */
final class JmxAgent
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JmxAgent.class);
    private static JMXConnectorServer connectorServer = null;

    private JmxAgent()
    {
        // Utility class
    }

    static synchronized void registerAgent(int registryPort, int serverPort, String hostname, DbConn cnx)
            throws JqmInitError
    {
        if (connectorServer != null)
        {
            // The agent is JVM-global, not engine specific, so prevent double start.
            jqmlogger.info("The JMX remote agent has already been registered. Please unregister it before if you want to register it again with other settings.");
            return;
        }

        jqmlogger.trace("registering remote agent");
        try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            JndiContext ctx = (JndiContext) NamingManager.getInitialContext(null);
            ctx.registerRmiContext(LocateRegistry.createRegistry(registryPort));

            boolean useSsl = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableJmxSsl", "false"));
            boolean sslNeedClientAuth = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableJmxSslAuth", "false"));
            boolean useInternalPki = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableInternalPki", "true"));
            String pfxPassword = GlobalParameter.getParameter(cnx, "pfxPassword", "SuperPassword");

            HashMap<String, Object> env = null;

            if (useSsl)
            {
                jqmlogger.info("JQM will use SSL for all communications with the JMX remote agent as parameter enableJmxSsl is 'true'");
                env = new HashMap<String, Object>();

                // From JettyServer class:
                if (useInternalPki)
                {
                    jqmlogger.info("JQM will use its internal PKI for all certificates as parameter enableInternalPki is 'true'");
                    JdbcCa.prepareWebServerStores(cnx, "CN="
                            + hostname, "./conf/keystore.pfx", "./conf/trusted.jks", pfxPassword, hostname, "./conf/server.cer", "./conf/ca.cer");
                }

                // Following instructions of
                // https://docs.oracle.com/javadb/10.10.1.2/adminguide/radminjmxenablepwdssl.html
                // and
                // https://docs.oracle.com/javase/8/docs/technotes/guides/management/agent.html:
                System.setProperty("com.sun.management.jmxremote.registry.ssl", Boolean.toString(useSsl));
                System.setProperty("com.sun.management.jmxremote.ssl.need.client.auth", Boolean.toString(sslNeedClientAuth));

                SslRMIServerSocketFactory ssf = null;
                // System.setProperty("javax.net.debug", "all");

                String[] enabledCipherSuites = null;
                String[] enabledProtocols = null;

                try
                {
                    // Load the SSL keystore properties
                    char[] pfxPasswordChars = pfxPassword != null ? pfxPassword.toCharArray() : null;

                    String keyStorePath = "./conf/keystore.pfx";
                    KeyStore ks = null;
                    ks = KeyStore.getInstance("PKCS12");
                    FileInputStream ksfis = new FileInputStream(keyStorePath);
                    try
                    {
                        ks.load(ksfis, pfxPasswordChars);
                    }
                    finally
                    {
                        ksfis.close();
                    }
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(ks, pfxPasswordChars);

                    String trustStorePath = "./conf/trusted.jks";
                    KeyStore ts = null;
                    ts = KeyStore.getInstance("JKS");
                    FileInputStream tsfis = new FileInputStream(trustStorePath);
                    try
                    {
                        ts.load(tsfis, pfxPasswordChars);
                    }
                    finally
                    {
                        tsfis.close();
                    }
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init((KeyStore) ts);

                    SSLContext sslctx = SSLContext.getInstance("TLSv1");
                    sslctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                    if (sslNeedClientAuth)
                    {
                        jqmlogger.info("JQM will use client authentication through SSL for all communications with the JMX remote agent as parameter enableJmxSslAuth is 'true'");
                    }
                    ssf = new SslRMIServerSocketFactory(sslctx, enabledCipherSuites, enabledProtocols, sslNeedClientAuth);
                }
                catch (Exception e)
                {
                    jqmlogger.error("JQM could not setup correctly the SSL context for the JMX remote agent", e);
                }

                env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new SslRMIClientSocketFactory());
                env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
            }

            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hostname + ":" + serverPort + "/jndi/rmi://"
                    + hostname + ":" + registryPort + "/jmxrmi");
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);

            connectorServer.start();
            jqmlogger.info("The JMX remote agent was registered. Connection string is " + url);
        }
        catch (Exception e)
        {
            throw new JqmInitError("Could not create remote JMX agent", e);
        }
    }

    static synchronized void unregisterAgent()
    {
        if (connectorServer != null)
        {
            try
            {
                connectorServer.stop();
                connectorServer = null;
                jqmlogger.info("The JMX remote agent has been unregistered.");
            }
            catch (IOException e)
            {
                jqmlogger.error("An error occurred during JMX remote agent unregistration.", e);
            }
        }
        else
        {
            jqmlogger.info("The JMX remote agent has not been registered, ignoring unregistration order.");
        }
    }

}
