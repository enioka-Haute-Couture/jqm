package com.enioka.jqm.test.helpers.db.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.test.helpers.db.DbTester;

public class OracleTester implements DbTester
{
    public static Logger jqmlogger = LoggerFactory.getLogger(PgTester.class);

    @Override
    public void simulateCrash(DbConn cnx)
    {
        ResultSet rs = cnx
                .runRawSelect("SELECT SID,SERIAL# FROM V$SESSION WHERE USERNAME = 'JQM' AND AUDSID != SYS_CONTEXT('USERENV','sessionid')");
        try
        {
            while (rs.next())
            {
                cnx.runRawUpdate("ALTER SYSTEM DISCONNECT SESSION '" + rs.getInt(1) + "," + rs.getInt(2) + "' IMMEDIATE");
            }
            rs.close();
        }
        catch (SQLException e)
        {
            jqmlogger.error("Could not simulate crash", e);
            throw new DatabaseException(e);
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
