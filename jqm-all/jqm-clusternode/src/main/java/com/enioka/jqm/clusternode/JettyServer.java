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
package com.enioka.jqm.clusternode;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.pki.JdbcCa;
import com.enioka.jqm.shared.misc.Closer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Every engine has an embedded Jetty engine that serves the different web service APIs.
 */
class JettyServer
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JettyServer.class);

    private Node node;
    private List<String> startedServicesPid = new ArrayList<>(3);

    void start(Node node, DbConn cnx)
    {
        ConfigurationAdmin adminService = getConfigurationAdmin();

        this.node = node;
        Configuration httpServiceConfiguration;
        try
        {
            httpServiceConfiguration = adminService.getConfiguration("org.apache.felix.http", null);
        }
        catch (IOException e)
        {
            throw new JqmInitError("Could not access OSGi registry storage", e);
        }
        Dictionary<String, Object> httpServiceProperties = httpServiceConfiguration.getProperties();
        if (httpServiceProperties == null)
        {
            httpServiceProperties = new Hashtable<String, Object>();
        }

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

        // Only load Jetty if at least one application should start
        if (!node.getLoadApiAdmin() && !node.getLoadApiClient() && !node.getLoapApiSimple())
        {
            jqmlogger.info("Jetty will not start - all web APIs are disabled on this node");
            return;
        }

        // Get which APIs should start
        boolean loadApiSimple = node.getLoapApiSimple();
        boolean loadApiClient = node.getLoadApiClient();
        boolean loadApiAdmin = node.getLoadApiAdmin();

        // Port - also update node if no port specified.
        if (node.getPort() == 0)
        {
            node.setPort(getRandomFreePort());

            cnx.runUpdate("node_update_port_by_id", node.getPort(), node.getId());
            cnx.commit();
        }
        boolean useSsl = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableWsApiSsl", "true"));

        // Certificates
        boolean useInternalPki = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableInternalPki", "true"));
        String pfxPassword = GlobalParameter.getParameter(cnx, "pfxPassword", "SuperPassword");

        ///////////////////////////////////////////////////////////////////////
        // Certificates preparation
        ///////////////////////////////////////////////////////////////////////

        // Update stores from database? (or create them completely)
        if (useSsl && useInternalPki)
        {
            jqmlogger.info("JQM will use its internal PKI for all certificates as parameter enableInternalPki is 'true'");
            JdbcCa.prepareWebServerStores(cnx, "CN=" + node.getDns(), "./conf/keystore.pfx", "./conf/trusted.jks", pfxPassword,
                    node.getDns(), "./conf/server.cer", "./conf/ca.cer");
        }

        if (useSsl)
        {
            // Keystore for HTTPS connector
            httpServiceProperties.put("org.apache.felix.https.keystore", "./conf/keystore.pfx");
            httpServiceProperties.put("org.apache.felix.https.keystore.password", pfxPassword);

            // Trust store
            httpServiceProperties.put("org.apache.felix.https.truststore", "./conf/trusted.jks");
            httpServiceProperties.put("org.apache.felix.https.truststore.password", pfxPassword);
            httpServiceProperties.put("org.apache.felix.https.truststore.type", "JKS");

            // Client certificate authentication
            httpServiceProperties.put("org.apache.felix.https.clientcertificate", "wants");
        }

        ///////////////////////////////////////////////////////////////////////
        // Create a configuration for each JAXRS API to start
        ///////////////////////////////////////////////////////////////////////

        if (loadApiSimple)
        {
            activateApiJaxRsService(adminService, "com.enioka.jqm.ws.api.ServiceSimple");
        }
        if (loadApiClient)
        {
            activateApiJaxRsService(adminService, "com.enioka.jqm.ws.api.ServiceClient");
        }
        if (loadApiAdmin)
        {
            activateApiJaxRsService(adminService, "com.enioka.jqm.ws.api.ServiceAdmin");
        }

        ///////////////////////////////////////////////////////////////////////
        // Jetty configuration
        ///////////////////////////////////////////////////////////////////////

        // HTTP configuration
        httpServiceProperties.put("org.apache.felix.http.jetty.responseBufferSize", 32768);
        httpServiceProperties.put("org.apache.felix.http.jetty.headerBufferSize", 8192);
        httpServiceProperties.put("org.apache.felix.http.jetty.sendServerHeader", false);

        // This is a JQM node
        httpServiceProperties.put("servlet.init.jqmnodeid", node.getId().toString());

        // Connectors configuration - only start http or https, but not both
        httpServiceProperties.put("org.apache.felix.https.enable", useSsl);
        httpServiceProperties.put("org.apache.felix.http.enable", !useSsl);
        if (!useSsl)
        {
            httpServiceProperties.put("org.osgi.service.http.port", node.getPort());

            jqmlogger.info("JQM will use plain HTTP for all communications (no TLS)");
        }
        else
        {
            httpServiceProperties.put("org.osgi.service.http.port.secure", node.getPort());
            httpServiceProperties.put("org.apache.felix.https.jetty.ciphersuites.excluded",
                    "SSL_RSA_WITH_DES_CBC_SHA,SSL_DHE_RSA_WITH_DES_CBC_SHA,SSL_DHE_DSS_WITH_DES_CBC_SHA,SSL_RSA_EXPORT_WITH_RC4_40_MD5,"
                            + "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA,SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA,SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA,"
                            + "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA,TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA,"
                            + "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,TLS_RSA_WITH_AES_128_GCM_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,"
                            + "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,"
                            + "TLS_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,"
                            + "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,"
                            + "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,"
                            + "TLS_DHE_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_DSS_WITH_AES_256_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_RSA_WITH_AES_256_CBC_SHA");
            httpServiceProperties.put("org.apache.felix.https.jetty.protocols.included", "TLSv1.2,TLSv1.3");

            jqmlogger.info("JQM will use TLS for all HTTP communications as parameter enableWsApiSsl is 'true'");
        }
        jqmlogger.debug("Jetty will bind on port {}", node.getPort());

        ///////////////////////////////////////////////////////////////////////
        // JAX-RS whiteboard configuration
        ///////////////////////////////////////////////////////////////////////

        Configuration jaxRsServiceConfiguration;
        try
        {
            jaxRsServiceConfiguration = adminService.getConfiguration("org.apache.aries.jax.rs.whiteboard.default", null);
        }
        catch (IOException e)
        {
            throw new JqmInitError("Could not access OSGi registry storage", e);
        }
        Dictionary<String, Object> jaxRsServiceProperties = jaxRsServiceConfiguration.getProperties();
        if (jaxRsServiceProperties == null)
        {
            jaxRsServiceProperties = new Hashtable<String, Object>();
        }

        jaxRsServiceProperties.put("enabled", true);
        jaxRsServiceProperties.put("default.application.base", "/ws");
        jaxRsServiceProperties.put("osgi.http.whiteboard.context.select", "(osgi.http.whiteboard.context.name=MAIN_HTTP_CTX)");

        ///////////////////////////////////////////////////////////////////////
        // Done with Jetty itself - give configuration to the admin service.
        ///////////////////////////////////////////////////////////////////////
        try
        {
            jqmlogger.debug("Starting HTTP service configuration and reset");
            jaxRsServiceConfiguration.update(jaxRsServiceProperties);
            httpServiceConfiguration.update(httpServiceProperties);
            jqmlogger.debug("HTTP service configuration and reset are done");
        }
        catch (IOException e)
        {
            throw new JqmInitError("Could not modify OSGi registry storage", e);
        }
    }

    void stop()
    {
        jqmlogger.trace("Jetty will now stop");
        ConfigurationAdmin adminService = getConfigurationAdmin();
        Configuration configuration;
        try
        {
            configuration = adminService.getConfiguration("org.apache.felix.http", null);
        }
        catch (IOException e)
        {
            throw new JqmInitError("Could not access OSGi registry storage", e);
        }
        if (configuration == null || configuration.getProperties() == null)
        {
            // Was not actually started or bundles absent.
            return;
        }
        Dictionary<String, Object> properties = configuration.getProperties();

        if ("false".equals(properties.get("org.apache.aries.jax.rs.whiteboard.default.enabled"))
                || ("false".equals(properties.get("org.apache.felix.http.enable"))
                        && "false".equals(properties.get("org.apache.felix.https.enable"))))
        {
            // Not started
            return;
        }

        properties.put("org.apache.aries.jax.rs.whiteboard.default.enabled", false);
        properties.put("org.apache.felix.http.enable", false);
        properties.put("org.apache.felix.https.enable", false);

        try
        {
            configuration.update(properties);
        }
        catch (IOException e)
        {
            throw new JqmInitError("Could not modify OSGi registry storage", e);
        }

        cleanApiJaxRsServices(adminService);
        jqmlogger.debug("Jetty has stopped");
    }

    private void activateApiJaxRsService(ConfigurationAdmin adminService, String configPid)
    {
        Configuration jaxRsServiceConfig;
        try
        {
            jaxRsServiceConfig = adminService.getFactoryConfiguration(configPid, node.getId() + "", null);
            // Final null is important - it means the configuration is attached to the first
            // bundle with a compatible service
        }
        catch (IOException e)
        {
            throw new JqmInitError("Could not fetch OSGi " + configPid + " configuration factory", e);
        }
        Dictionary<String, Object> jaxRsServiceProperties = jaxRsServiceConfig.getProperties();
        if (jaxRsServiceProperties == null)
        {
            jaxRsServiceProperties = new Hashtable<>();
        }
        jaxRsServiceProperties.put("jqmnodeid", node.getId());

        try
        {
            jaxRsServiceConfig.update(jaxRsServiceProperties);
        }
        catch (IOException e)
        {
            throw new JqmInitError("Could not modify OSGi registry storage", e);
        }

        startedServicesPid.add(jaxRsServiceConfig.getPid());
        jqmlogger.info("Created configuration for service " + configPid);
    }

    private void cleanApiJaxRsServices(ConfigurationAdmin adminService)
    {
        Configuration config = null;
        for (String pid : startedServicesPid)
        {
            try
            {
                config = adminService.getConfiguration(pid);
            }
            catch (IOException e)
            {
                // Ignore - service already dead on shutdown.
                continue;
            }

            try
            {
                config.delete();
            }
            catch (IOException e)
            {
                jqmlogger.warn("Could not remove JAXRS service from configuration", e);
            }
        }
    }

    private int getRandomFreePort()
    {
        int port = -1;
        ServerSocket ss = null;
        try
        {
            ss = new ServerSocket(0);
            port = ss.getLocalPort();
        }
        catch (IOException e)
        {
            throw new JqmInitError("Could not determine a free TCP port", e);
        }
        finally
        {
            Closer.closeQuietly(ss);
        }
        return port;
    }

    /**
     * Retrieve the configuration service. It is retrieved manually. JQM does not use OSGi everywhere, only on extension points and HTTP
     * server, so this class is a bridge between the OSGi world (HTTP/JAXRS whiteboard) and normal Java code, and it is not a component
     * itself so we cannot use injection.
     **/
    private ConfigurationAdmin getConfigurationAdmin()
    {
        BundleContext bundleContext = FrameworkUtil.getBundle(JettyServer.class).getBundleContext();
        ServiceReference<ConfigurationAdmin> serviceRef = bundleContext.getServiceReference(ConfigurationAdmin.class);
        return bundleContext.getService(serviceRef);
    }
}
