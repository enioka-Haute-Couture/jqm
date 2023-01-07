package com.enioka.jqm.test.helpers.db.impl;

import org.hsqldb.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.test.helpers.db.DbTester;

public class HsqlDbTester implements DbTester
{
    public static Logger jqmlogger = LoggerFactory.getLogger(HsqlDbTester.class);

    private static Server s;

    @Override
    public void simulateCrash(DbConn cnx)
    {
        jqmlogger.info("DB is going down");
        s.stop();
        this.waitDbStop();
        jqmlogger.info("DB is now fully down");
    }

    @Override
    public void simulateResumeAfterCrash(DbConn cnx)
    {
        jqmlogger.info("Restarting DB");
        s.start();
    }

    @Override
    public void init()
    {
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();
    }

    @Override
    public void stop()
    {
        s.stop();
        this.waitDbStop();
    }

    private void waitDbStop()
    {
        while (s.getState() != 16)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                if (Thread.interrupted())
                {
                    return;
                }
            }
        }
    }

}
