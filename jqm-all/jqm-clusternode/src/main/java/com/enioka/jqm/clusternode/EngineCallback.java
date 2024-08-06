package com.enioka.jqm.clusternode;

import java.util.Calendar;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.engine.api.lifecycle.JqmEngineHandler;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.pki.JdbcCa;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;
import com.enioka.jqm.webserver.api.WebServer;
import com.enioka.jqm.webserver.api.WebServerConfiguration;

public class EngineCallback implements JqmEngineHandler
{
    private static Logger jqmlogger = LoggerFactory.getLogger(EngineCallback.class);

    private WebServer webServer = null;
    private WebServerConfiguration webServerConfig = null;
    private DirectoryScanner scanner = null;
    private String logLevel = "INFO";
    private String nodePrms = null;
    private Calendar latestJettyRestart = Calendar.getInstance();

    @Override
    public void onConfigurationChanged(Node node)
    {
        // Log level changes.
        if (!this.logLevel.equals(node.getRootLogLevel()))
        {
            this.logLevel = node.getRootLogLevel();
            CommonService.setLogLevel(this.logLevel);
        }

        // Jetty restart. Conditions are:
        // * some parameters (such as security parameters) have changed
        // * node parameter change such as start or stop an API.
        Calendar bflkpm = Calendar.getInstance();
        String np = node.getDns() + node.getPort() + node.getLoadApiAdmin() + node.getLoadApiClient() + node.getLoapApiSimple();
        if (nodePrms == null)
        {
            nodePrms = np;
        }
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            int i = cnx.runSelectSingle("globalprm_select_count_modified_jetty", Integer.class, latestJettyRestart);
            if (i > 0 || !np.equals(nodePrms))
            {
                this.webServer.start(webServerConfig);
                latestJettyRestart = bflkpm;
                nodePrms = np;
            }
        }
    }

    @Override
    public void onNodeConfigurationRead(Node node)
    {
        try (DbConn cnx = DbManager.getDb().getConn()) {

            // Main log levels comes from configuration
            CommonService.setLogLevel(node.getRootLogLevel());
            this.logLevel = node.getRootLogLevel();

            // Jetty
            this.webServer = ServiceLoaderHelper.getService(ServiceLoader.load(WebServer.class), false);
            webServerConfig = new WebServerConfiguration();
            webServerConfig.setHost(node.getDns());
            webServerConfig.setPort(node.getPort());

            var rootPath = System.getProperty("com.enioka.jqm.alternateJqmRoot", null);
            if (rootPath == null || rootPath.isEmpty())
            {
                rootPath = ".";
            }

            webServerConfig.setWsEnabled(!Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "disableWsApi", "false")));
            webServerConfig.setStartSimple(
                    node.getLoapApiSimple() && !Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "disableWsApiSimple", "false")));
            webServerConfig.setStartClient(
                    node.getLoadApiClient() && !Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "disableWsApiClient", "false")));
            webServerConfig.setStartAdmin(
                    node.getLoadApiAdmin() && !Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "disableWsApiAdmin", "false")));
            webServerConfig.setUseTls(Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableWsApiSsl", "true")));

            var useInternalPki = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableInternalPki", "true"));
            var pfxPassword = GlobalParameter.getParameter(cnx, "pfxPassword", "SuperPassword");
            if (webServerConfig.isUseTls() && useInternalPki)
            {
                jqmlogger.info("JQM will use its internal PKI for all certificates as parameter enableInternalPki is 'true'");
                JdbcCa.prepareWebServerStores(cnx, "CN=" + node.getDns(), rootPath + "/conf/keystore.pfx", rootPath + "/conf/trusted.jks",
                        pfxPassword, node.getDns(), rootPath + "/conf/server.cer", rootPath + "/conf/ca.cer");
            }

            webServerConfig.setKeyStorePassword(pfxPassword);
            webServerConfig.setTrustStorePassword(pfxPassword);
            webServerConfig.setKeyStorePath(rootPath + "/conf/keystore.pfx");
            webServerConfig.setTrustStorePath(rootPath + "/conf/trusted.jks");

            webServerConfig.setWarPath(rootPath + "/webapp/jqm-ws.war");
            webServerConfig.setLocalNodeId(node.getId());

            this.webServer.start(webServerConfig);

            // Update node if port was changed
            var newPort = this.webServer.getActualPort();
            if (newPort != node.getPort())
            {
                node.setPort(newPort);
                cnx.runUpdate("node_update_port_by_id", newPort, node.getId());
                cnx.commit();
            }

            // Deployment scanner
            String gp2 = GlobalParameter.getParameter(cnx, "directoryScannerRoot", "");
            if (!gp2.isEmpty())
            {
                scanner = new DirectoryScanner(gp2, node);
                (new Thread(scanner)).start();
            }
        }
    }

    @Override
    public void onNodeStopped()
    {
        this.webServer.stop();
        if (this.scanner != null)
        {
            this.scanner.stop();
        }
    }

    @Override
    public void onNodeStarted()
    {
        // Nothing done by default.
    }

    @Override
    public void onJobInstancePreparing(JobInstance job)
    {}

    @Override
    public void onJobInstanceDone(JobInstance ji)
    {}

    @Override
    public void onNodeStarting(String nodeName)
    {
        CommonService.setLogFileName(nodeName);
    }
}
