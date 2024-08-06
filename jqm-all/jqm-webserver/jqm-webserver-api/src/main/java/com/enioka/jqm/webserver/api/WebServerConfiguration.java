package com.enioka.jqm.webserver.api;

public class WebServerConfiguration
{
    // Basics
    private int port = 0;
    private String host = "0.0.0.0";
    private String warPath = "./jqm-ws.war";

    // Which services should be started?
    private boolean wsEnabled = false, startSimple = false, startClient = false, startAdmin = false;

    // Some services need to access the local node configuration
    private long localNodeId;

    // TLS
    private boolean useTls = false;
    private String keyStorePath = "./conf/keystore.pfx", keyStorePassword = null;
    private String trustStorePath = "./conf/truststore.pfx", trustStorePassword = null;

    public String getTrustStorePath()
    {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath)
    {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword()
    {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword)
    {
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyStorePath()
    {
        return keyStorePath;
    }

    public void setKeyStorePath(String keystorePath)
    {
        this.keyStorePath = keystorePath;
    }

    public String getKeyStorePassword()
    {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keystorePassword)
    {
        this.keyStorePassword = keystorePassword;
    }

    public boolean isUseTls()
    {
        return useTls;
    }

    public void setUseTls(boolean useTls)
    {
        this.useTls = useTls;
    }

    public boolean isWsEnabled()
    {
        return wsEnabled;
    }

    public void setWsEnabled(boolean wsEnabled)
    {
        this.wsEnabled = wsEnabled;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public boolean isStartSimple()
    {
        return startSimple;
    }

    public void setStartSimple(boolean startSimple)
    {
        this.startSimple = startSimple;
    }

    public boolean isStartClient()
    {
        return startClient;
    }

    public void setStartClient(boolean startClient)
    {
        this.startClient = startClient;
    }

    public boolean isStartAdmin()
    {
        return startAdmin;
    }

    public void setStartAdmin(boolean startAdmin)
    {
        this.startAdmin = startAdmin;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getWarPath()
    {
        return warPath;
    }

    public void setWarPath(String warPath)
    {
        this.warPath = warPath;
    }

    public long getLocalNodeId()
    {
        return localNodeId;
    }

    public void setLocalNodeId(long localNodeId)
    {
        this.localNodeId = localNodeId;
    }
}
