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

import java.io.File;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.pki.JpaCa;

/**
 * Every engine has an embedded Jetty engine that serves the different web service APIs.
 */
class JettyServer
{
    private static Logger jqmlogger = Logger.getLogger(JettyServer.class);

    private Server server = null;
    private HandlerCollection h = new HandlerCollection();
    private Node node;
    WebAppContext webAppContext = null;

    void start(Node node, EntityManager em)
    {
        // Start is also a restart method
        this.stop();

        // Only load Jetty if web APIs are allowed in the cluster
        boolean startJetty = !Boolean.parseBoolean(Helpers.getParameter("disableWsApi", "false", em));
        if (!startJetty)
        {
            jqmlogger.info("Jetty will not start - parameter disableWsApi is set to true");
            return;
        }

        // Only load Jetty if at least one war is present...
        File war = new File("./webapp/jqm-ws.war");
        if (!war.exists() || !war.isFile())
        {
            jqmlogger.info("Jetty will not start - there are no web applications to load inside the webapp directory");
            return;
        }

        // Only load Jetty if at least one application should start
        if (!node.getLoadApiAdmin() && !node.getLoadApiClient() && !node.getLoapApiSimple())
        {
            jqmlogger.info("Jetty will not start - all web APIs are disabled on this node");
            return;
        }

        this.node = node;
        boolean useSsl = Boolean.parseBoolean(Helpers.getParameter("enableWsApiSsl", "true", em));
        boolean useInternalPki = Boolean.parseBoolean(Helpers.getParameter("enableInternalPki", "true", em));
        String pfxPassword = Helpers.getParameter("pfxPassword", "SuperPassword", em);

        server = new Server();

        SslContextFactory scf = null;
        if (useSsl)
        {
            jqmlogger.info("JQM will use SSL for all HTTP communications as parameter enableWsApiSsl is 'true'");
            if (useInternalPki)
            {
                jqmlogger.info("JQM will use its internal PKI for all certificates as parameter enableInternalPki is 'true'");
                JpaCa.prepareWebServerStores(em, "CN=" + node.getDns(), "./conf/keystore.pfx", "./conf/trusted.jks", pfxPassword,
                        node.getDns(), "./conf/server.cer", "./conf/ca.cer");
            }
            scf = new SslContextFactory("./conf/keystore.pfx");
            scf.setKeyStorePassword(pfxPassword);
            scf.setKeyStoreType("PKCS12");

            scf.setTrustStore("./conf/trusted.jks");
            scf.setTrustStorePassword(pfxPassword);
            scf.setTrustStoreType("JKS");

            scf.setWantClientAuth(true);
        }
        else
        {
            jqmlogger.info("JQM will use plain HTTP for all communications (no SSL)");
        }

        // Connectors.
        List<Connector> ls = new ArrayList<Connector>();
        try
        {
            InetAddress[] adresses = InetAddress.getAllByName(node.getDns());
            for (InetAddress s : adresses)
            {
                if (s instanceof Inet4Address)
                {
                    Connector connector = null;
                    if (useSsl)
                    {
                        connector = new SslSocketConnector(scf);
                    }
                    else
                    {
                        connector = new SelectChannelConnector();
                    }

                    if (s.isLoopbackAddress() || "localhost".equals(node.getDns()))
                    {
                        connector.setHost("localhost");
                        connector.setPort(node.getPort());
                        ls.add(connector);
                        jqmlogger.debug("Jetty will bind on localhost:" + node.getPort());
                    }
                    else
                    {
                        connector.setHost(s.getHostAddress());
                        connector.setPort(node.getPort());
                        ls.add(connector);
                        jqmlogger.debug("Jetty will bind on " + s.getHostAddress() + ":" + node.getPort());
                    }
                }
            }
        }
        catch (UnknownHostException e1)
        {
            jqmlogger.warn("Could not resolve name " + node.getDns() + ". Will bind on all interfaces.");
            Connector connector = new SelectChannelConnector();
            connector.setHost(null);
            connector.setPort(node.getPort());
            ls.add(connector);
        }
        server.setConnectors(ls.toArray(new Connector[ls.size()]));

        // Collection handler
        server.setHandler(h);

        // Load the webapp context
        loadWar();

        // Start the server
        jqmlogger.trace("Starting Jetty (port " + node.getPort() + ")");
        try
        {
            server.start();
        }
        catch (BindException e)
        {
            // JETTY-839: threadpool not daemon nor close on exception. Explicit closing required as workaround.
            this.stop();
            throw new JqmInitError("Could not start web server - check there is no other process binding on this port & interface", e);
        }
        catch (Exception e)
        {
            throw new JqmInitError("Could not start web server - not a port issue, but a generic one", e);
        }

        // Save port if it was generated randomly
        if (node.getPort() == 0)
        {
            // New nodes are created with a non-assigned port.
            em.getTransaction().begin();
            node.setPort(getActualPort());
            em.getTransaction().commit();
        }

        // Done
        jqmlogger.info("Jetty has started on port " + getActualPort());
    }

    int getActualPort()
    {
        if (server == null)
        {
            return 0;
        }
        return server.getConnectors()[0].getLocalPort();
    }

    void stop()
    {
        if (server == null)
        {
            return;
        }
        jqmlogger.trace("Jetty will now stop");
        try
        {
            for (Handler ha : server.getHandlers())
            {
                ha.stop();
                ha.destroy();
                h.removeHandler(ha);
            }

            this.server.stop();
            this.server.join();
            this.server.destroy();
            this.server = null;
            jqmlogger.info("Jetty has stopped");
        }
        catch (Exception e)
        {
            jqmlogger
                    .error("An error occured during Jetty stop. It is not an issue if it happens during JQM node shutdown, but one during restart (memeory leak).",
                            e);
        }
    }

    private void loadWar()
    {
        File war = new File("./webapp/jqm-ws.war");
        if (!war.exists() || !war.isFile())
        {
            return;
        }
        jqmlogger.info("Jetty will now load the web service application war");

        // Load web application.
        webAppContext = new WebAppContext(war.getPath(), "/");
        webAppContext.setDisplayName("JqmWebServices");

        // Hide server classes from the web app
        final int nbEx = 5;
        String[] defExcl = webAppContext.getDefaultServerClasses();
        String[] exclusions = new String[defExcl.length + nbEx];
        for (int i = nbEx; i <= defExcl.length; i++)
        {
            exclusions[i] = defExcl[i - nbEx];
        }
        exclusions[0] = "com.enioka.jqm.tools.";
        exclusions[1] = "com.enioka.jqm.api.";
        // exclusions[2] = "org.slf4j.";
        // exclusions[3] = "org.apache.log4j.";
        exclusions[4] = "org.glassfish."; // Jersey
        webAppContext.setServerClasses(exclusions);

        // JQM configuration should be on the class path
        webAppContext.setExtraClasspath("conf/jqm.properties");
        webAppContext.setInitParameter("jqmnode", node.getName());
        webAppContext.setInitParameter("jqmnodeid", node.getId().toString());

        // Set configurations (order is important: need to unpack war before reading web.xml)
        webAppContext.setConfigurations(new Configuration[] { new WebInfConfiguration(), new WebXmlConfiguration(),
                new MetaInfConfiguration(), new FragmentConfiguration(), new AnnotationConfiguration() });

        h.addHandler(webAppContext);
    }
}
