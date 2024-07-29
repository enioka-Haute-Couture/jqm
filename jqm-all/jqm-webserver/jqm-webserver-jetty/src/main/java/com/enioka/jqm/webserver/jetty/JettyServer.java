package com.enioka.jqm.webserver.jetty;

import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import com.enioka.jqm.webserver.api.WebServer;
import com.enioka.jqm.webserver.api.WebServerConfiguration;

/**
 * Main implementation of the HTTP(S) server.<br>
 * Some parts inspired by https://northcoder.com/post/jetty-11-secure-connections/
 */
@MetaInfServices(WebServer.class)
public class JettyServer implements WebServer
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JettyServer.class);

    private Server server = null;

    @Override
    public void start(WebServerConfiguration configuration)
    {
        // Start is also a restart method
        this.stop();

        // Only load Jetty if web APIs are allowed in the cluster
        if (!configuration.isWsEnabled()
                || (!configuration.isStartAdmin() && !configuration.isStartClient() && !configuration.isStartSimple()))
        {
            jqmlogger.info("Jetty will not start - parameter disableWsApi is set to true");
            return;
        }

        // Only load Jetty if at least one application should start
        if (!configuration.isStartAdmin() && !configuration.isStartClient() && !configuration.isStartSimple())
        {
            jqmlogger.info("Jetty will not start - all web APIs are disabled on this node");
            return;
        }

        // Only load Jetty if at least one war is present...
        File war = new File(configuration.getWarPath());
        if (!war.exists() || !war.isFile())
        {
            jqmlogger.info("Jetty will not start - there are no web applications to load inside the webapp directory");
            return;
        }

        // Create server
        server = new Server();
        server.setDumpAfterStart(jqmlogger.isTraceEnabled());
        server.setConnectors(null);

        HttpConfiguration http = new HttpConfiguration();
        if (configuration.isUseTls())
        {
            jqmlogger.info("JQM will use HTTPS for all communications (with TLS)");
            http.setSecureScheme("https");
            http.setSecurePort(configuration.getPort());
            http.addCustomizer(getSecureRequestCustomizer());

            // HTTP/1.1
            HttpConnectionFactory httpCF = new HttpConnectionFactory(http);

            // HTTP/2
            var http2CF = new HTTP2ServerConnectionFactory(http);

            // ALPN is used to negotiate HTTP2 over a previously established connection
            var alpnCF = new ALPNServerConnectionFactory();
            alpnCF.setDefaultProtocol(httpCF.getProtocol()); // fallback protocol

            // TLS CF
            var tlsHttp2CF = new SslConnectionFactory(getSSLContextFactory(configuration), alpnCF.getProtocol());

            // Add the server connector
            var connector = new ServerConnector(server, tlsHttp2CF, alpnCF, http2CF, httpCF); // Order is important - HTTP2 over HTTP1.
            connector.setPort(configuration.getPort());
            connector.setHost(getHost(configuration));
            server.addConnector(connector);
        }
        else
        {
            jqmlogger.info("JQM will use plain HTTP for all communications (no TLS)");
            http.setSendServerVersion(false);

            HttpConnectionFactory httpCF = new HttpConnectionFactory(http);
            var connector = new ServerConnector(server, httpCF);
            connector.setPort(configuration.getPort());
            connector.setHost(getHost(configuration));
            server.addConnector(connector);
        }

        // Add the webapp
        var webAppContext = loadWar(configuration);
        if (webAppContext != null)
        {
            server.setHandler(webAppContext);
        }

        // Start
        try
        {
            server.start();
        }
        catch (Exception e)
        {
            // JETTY-839: threadpool not daemon nor close on exception. Explicit closing required as workaround.
            this.stop();
            throw new JqmInitError("Could not start web server - check there is no other process binding on this port & interface", e);
        }

        jqmlogger.info("Jetty has started on port " + getActualPort());
    }

    @Override
    public void stop()
    {
        if (server == null)
        {
            return;
        }
        jqmlogger.trace("Jetty will now stop");
        try
        {
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

    @Override
    public int getActualPort()
    {
        if (server == null || server.getConnectors() == null || server.getConnectors().length == 0)
        {
            return 0;
        }
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    private SecureRequestCustomizer getSecureRequestCustomizer()
    {
        var src = new SecureRequestCustomizer();
        src.setSniRequired(false);
        src.setStsMaxAge(3600);
        src.setStsIncludeSubDomains(true);
        return src;
    }

    private SslContextFactory.Server getSSLContextFactory(WebServerConfiguration configuration)
    {
        jqmlogger.info("Jetty web server uses keystore {} and truststore {}", configuration.getKeyStorePath(),
                configuration.getTrustStorePath());

        var sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(configuration.getKeyStorePath());
        sslContextFactory.setKeyStorePassword(configuration.getKeyStorePassword());
        sslContextFactory.setKeyStoreType("PKCS12");

        sslContextFactory.setTrustStorePath(configuration.getTrustStorePath());
        sslContextFactory.setTrustStorePassword(configuration.getTrustStorePassword());
        sslContextFactory.setTrustStoreType("JKS");

        sslContextFactory.setWantClientAuth(true);

        sslContextFactory.addExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_RSA_WITH_AES_128_CBC_SHA256", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_RSA_WITH_AES_256_CBC_SHA");

        sslContextFactory.addExcludeProtocols("SSLv3", "TLSv1", "TLSv1.1");

        return sslContextFactory;
    }

    private String getHost(WebServerConfiguration configuration)
    {
        // Host either comes from node configuirain or from a system property that overloads it (shortcut for container deployments)
        // TODO: put this in other project.
        var hostName = configuration.getHost();
        String interfaceFromProperty = System.getProperty("com.enioka.jqm.interface");
        if (interfaceFromProperty != null && !interfaceFromProperty.isEmpty())
        {
            hostName = interfaceFromProperty;
        }

        if (hostName == null || hostName.isEmpty())
        {
            jqmlogger.info("No hostname specified - binding Jetty on all interfaces");
            return null;
        }

        // Special cases
        if ("localhost".equals(hostName))
        {
            jqmlogger.debug("Jetty will bind on loopback as required");
            return "localhost";
        }
        else if ("0.0.0.0".equals(hostName))
        {
            jqmlogger.debug("Jetty will bind on all interfaces as required");
            return null;
        }

        // Normal case
        return hostName;
    }

    private WebAppContext loadWar(WebServerConfiguration configuration)
    {
        File war = new File(configuration.getWarPath());
        if (!war.exists() || !war.isFile())
        {
            return null;
        }
        jqmlogger.info("Jetty will now load the web service application war");

        // Load web application.
        var webAppContext = new WebAppContext(war.getPath(), "/");
        webAppContext.setDisplayName("JqmWebServices");

        // Allow access to the extensions directory, but not engine classes.
        /*
         * try { webAppContext.setClassLoader(new IsolatedClassLoader(webAppContext)); } catch (IOException e) { throw new
         * JqmInitError("Could not create isolated class loader", e); }
         */

        // Avoid annoying default Jetty configuration that loads classes outside the classpath...
        webAppContext.setDefaultsDescriptor(null);
        WebAppContext.addServerClasses(server, "-org.eclipse.jetty.servlet.DefaultServlet");

        // JQM configuration should be on the classpath
        try
        {
            webAppContext.setExtraClasspath("conf/jqm.properties");
        }
        catch (IOException e)
        {
            throw new JqmInitError("Could not add JQM configuration to the webapp classpath", e);
        }
        // webAppContext.setInitParameter("jqmnode", node.getName());
        webAppContext.setInitParameter("jqmnodeid", configuration.getLocalNodeId() + "");
        webAppContext.setInitParameter("startSimple", configuration.isStartSimple() + "");
        webAppContext.setInitParameter("startClient", configuration.isStartClient() + "");
        webAppContext.setInitParameter("startAdmin", configuration.isStartAdmin() + "");

        // Set configurations (order is important: need to unpack war before reading web.xml)
        webAppContext.setConfigurations(new Configuration[] { new WebInfConfiguration(), new WebXmlConfiguration(),
                new MetaInfConfiguration(), new FragmentConfiguration(), new AnnotationConfiguration() });

        return webAppContext;
    }
}
