package com.enioka.jqm.test.helpers;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hsqldb.Server;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HSQLDB server listening in-memory only.
 */
@Component(immediate = false, service = DebugHsqlDbServer.class, scope = ServiceScope.SINGLETON, configurationPolicy = ConfigurationPolicy.OPTIONAL, configurationPid = "com.enioka.jqm.test.helpers.DebugHsqlDbServer", property = {
        "dbName=testdbengine", "dbPath=mem:testdbengine" })
public class DebugHsqlDbServer
{
    private Logger jqmlogger = LoggerFactory.getLogger(DebugHsqlDbServer.class);

    private Server s;
    private String dbName, dbPath;

    @Activate
    public void activate(Map<String, Object> properties)
    {
        jqmlogger.debug("HSQLDB configuration initialized to {}", properties);
        this.dbName = properties.get("dbName").toString();
        this.dbPath = properties.get("dbPath").toString();
        this.start();
    }

    @Modified
    public void modifiedConfiguration(Map<String, Object> properties)
    {
        jqmlogger.debug("HSQLDB configuration modified to {}", properties);
        stop();
        start();
    }

    /**
     * Idempotent. Start or restart the server.
     */
    public void start()
    {
        if (s == null)
        {
            // Database only starts if the correct env var is set.
            if (isHsqldb())
            {
                s = new Server();
                s.setLogWriter(null);
                s.setSilent(true);
                s.setDatabaseName(0, this.dbName);
                s.setDatabasePath(0, this.dbPath);
            }

            // Reset the caches - the database has changed.
            try
            {
                InitialContext.doLookup("internal://reset");
            }
            catch (NamingException e)
            {
                // jqmlogger.warn("Could not purge test JNDI context", e);
            }
        }

        if (s != null)
        {
            s.start();
        }
    }

    /**
     * Idempotent. Stops the server.
     */
    @Deactivate
    public void stop()
    {
        if (s != null)
        {
            s.stop();
            waitDbStop();
            s = null;
        }
    }

    public boolean isHsqldb()
    {
        return System.getenv("DB") == null || "hsqldb".equals(System.getenv("DB"));
    }

    private void waitDbStop()
    {
        while (s.getState() != 16)
        {
            sleepms(1);
        }
    }

    private void sleepms(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            // not an issue in tests
        }
    }
}
