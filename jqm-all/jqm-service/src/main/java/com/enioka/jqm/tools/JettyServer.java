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

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.pki.JdbcCa;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Every engine has an embedded Jetty engine that serves the different web service APIs.
 */
class JettyServer
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JettyServer.class);

    private Server server = null;
    private HandlerCollection handlers = new HandlerCollection();
    private Node node;
    WebAppContext webAppContext = null;

    void start(Node node, DbConn cnx)
    {
        // Start is also a restart method
        this.stop();

        ///////////////////////////////////////////////////////////////////////
        // Configuration checks
        ///////////////////////////////////////////////////////////////////////

        // Only load Jetty if web APIs are allowed in the cluster
        boolean startJetty = !Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "disableWsApi", "false"));
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
        boolean useSsl = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableWsApiSsl", "true"));
        boolean useInternalPki = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableInternalPki", "true"));
        String pfxPassword = GlobalParameter.getParameter(cnx, "pfxPassword", "SuperPassword");
        String bindTo = node.getDns().trim().toLowerCase();

        ///////////////////////////////////////////////////////////////////////
        // Jetty configuration
        ///////////////////////////////////////////////////////////////////////

        // Setup thread pool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(10);

        // Create server
        server = new Server(threadPool);

        server.setDumpAfterStart(false);
        server.setDumpBeforeStop(false);
        server.setStopAtShutdown(true);

        // HTTP configuration
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setOutputBufferSize(32768);
        httpConfig.setRequestHeaderSize(8192);
        httpConfig.setResponseHeaderSize(8192);
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendDateHeader(false);
        if (useSsl)
        {
            httpConfig.setSecurePort(node.getPort());
        }

        // TLS configuration
        SslContextFactory scf = null;
        if (useSsl)
        {
            jqmlogger.info("JQM will use TLS for all HTTP communications as parameter enableWsApiSsl is 'true'");

            // Certificates
            if (useInternalPki)
            {
                jqmlogger.info("JQM will use its internal PKI for all certificates as parameter enableInternalPki is 'true'");
                JdbcCa.prepareWebServerStores(cnx, "CN=" + node.getDns(), "./conf/keystore.pfx", "./conf/trusted.jks", pfxPassword,
                        node.getDns(), "./conf/server.cer", "./conf/ca.cer");
            }
            scf = new SslContextFactory("./conf/keystore.pfx");

            scf.setKeyStorePassword(pfxPassword);
            scf.setKeyStoreType("PKCS12");

            scf.setTrustStorePath("./conf/trusted.jks");
            scf.setTrustStorePassword(pfxPassword);
            scf.setTrustStoreType("JKS");

            // Ciphers
            scf.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                    "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                    "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
                    "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_RSA_WITH_AES_128_CBC_SHA256", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
                    "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                    "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA");

            // We allow client certificate authentication.
            scf.setWantClientAuth(true);
            scf.setEndpointIdentificationAlgorithm(null); // Means no hostname check, as client certificates do not sign a hostname but an
                                                          // identity.

            // Servlet TLS attributes
            httpConfig = new HttpConfiguration(httpConfig);
            httpConfig.addCustomizer(new SecureRequestCustomizer());

            // Connectors.
            ServerConnector https = new ServerConnector(server, scf, new HttpConnectionFactory(httpConfig));
            https.setPort(node.getPort());
            https.setIdleTimeout(30000);
            https.setHost(bindTo);
            server.addConnector(https);

            jqmlogger.debug("Jetty will bind on interface {} on port {} with HTTPS", bindTo, node.getPort());
        }
        else
        {
            jqmlogger.info("JQM will use plain HTTP for all communications (no TLS)");

            // Connectors.
            ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
            http.setPort(node.getPort());
            http.setIdleTimeout(30000);
            http.setHost(bindTo);
            server.addConnector(http);

            jqmlogger.debug("Jetty will bind on interface {} on port {} with HTTP", bindTo, node.getPort());
        }

        // Collection handler
        server.setHandler(handlers);

        // Load the webapp context
        loadWar(cnx);

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
            cnx.runUpdate("node_update_port_by_id", getActualPort(), node.getId());
            node.setPort(getActualPort()); // refresh in-memory object too.
            cnx.commit();
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
        return ((NetworkConnector) server.getConnectors()[0]).getLocalPort();
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
                handlers.removeHandler(ha);
            }

            this.server.stop();
            this.server.join();
            this.server.destroy();
            this.server = null;
            jqmlogger.info("Jetty has stopped");
        }
        catch (Exception e)
        {
            jqmlogger.error(
                    "An error occured during Jetty stop. It is not an issue if it happens during JQM node shutdown, but one during restart (memeory leak).",
                    e);
        }
    }

    private void loadWar(DbConn cnx)
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
        webAppContext.getServerClasspathPattern().add("com.enioka.jqm.api."); // engine and webapp can have different API implementations
                                                                              // (during tests mostly)
        webAppContext.getServerClasspathPattern().add("com.enioka.jqm.tools.");
        webAppContext.getServerClasspathPattern().add("-com.enioka.jqm.tools.JqmXmlException"); // inside XML bundle, not engine.
        webAppContext.getServerClasspathPattern().add("-com.enioka.jqm.tools.XmlJobDefExporter");

        // JQM configuration should be on the class path
        webAppContext.setExtraClasspath("conf/jqm.properties");
        webAppContext.setInitParameter("jqmnode", node.getName());
        webAppContext.setInitParameter("jqmnodeid", node.getId().toString());
        webAppContext.setInitParameter("enableWsApiAuth", GlobalParameter.getParameter(cnx, "enableWsApiAuth", "true"));

        // Set configurations (order is important: need to unpack war before reading web.xml)
        webAppContext.setConfigurations(new Configuration[] { new WebInfConfiguration(), new WebXmlConfiguration(),
                new MetaInfConfiguration(), new FragmentConfiguration(), new AnnotationConfiguration() });

        handlers.addHandler(webAppContext);
    }
}
