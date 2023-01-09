package com.enioka.jqm.test.helpers.db.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.test.helpers.db.DbTester;

public class PgTester implements DbTester
{
    public static Logger jqmlogger = LoggerFactory.getLogger(PgTester.class);

    @Override
    public void simulateCrash(DbConn cnx)
    {
        // pg_terminate_backend(integer, integer) only exists in psql 14+, so we need to manually wait.
        ResultSet rs = cnx
                .runRawSelect("select pg_terminate_backend(pid) from pg_stat_activity where datname='jqm' AND pid <> pg_backend_pid()");
        try
        {
            rs.close();
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        try
        {
            Thread.sleep(1000); // time for process to die.
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
    }

    @Override
    public void simulateResumeAfterCrash(DbConn cnx)
    {
        // Nothing to do.
    }

    @Override
    public void init()
    {
        // Nothing to do.
    }

    @Override
    public void stop()
    {
        // Nothing to do.
    }
}
