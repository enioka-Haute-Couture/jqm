package com.enioka.jqm.test.helpers;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hsqldb.Server;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * An HSQLDB server listening in-memory only.
 */
@Component(immediate = false, service = DebugHsqlDbServer.class)
public class DebugHsqlDbServer
{
    private Server s;

    /**
     * Idempotent. Start or restart the server.
     */
    @Activate
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
                s.setDatabaseName(0, "testdbengine");
                s.setDatabasePath(0, "mem:testdbengine");
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
