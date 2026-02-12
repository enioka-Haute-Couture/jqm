package com.enioka.jqm.test.helpers;

import java.io.Closeable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hsqldb.Server;

/**
 * An HSQLDB server listening in-memory only.
 */
public class DebugHsqlDbServer implements Closeable
{
    private Server s;
    private String dbName = "testdbengine", dbPath = "mem:testdbengine";

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
    @Override
    public void close()
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
