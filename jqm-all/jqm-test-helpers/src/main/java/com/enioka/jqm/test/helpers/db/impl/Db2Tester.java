package com.enioka.jqm.test.helpers.db.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.test.helpers.db.DbTester;

public class Db2Tester implements DbTester
{
    public static Logger jqmlogger = LoggerFactory.getLogger(PgTester.class);

    @Override
    public void simulateCrash(DbConn cnx)
    {
        ResultSet rs = cnx.runRawSelect("SELECT APPLICATION_HANDLE, CLIENT_IPADDR, CLIENT_PORT_NUMBER, SESSION_AUTH_ID,"
                + "CURRENT_SERVER, APPLICATION_NAME, CLIENT_PROTOCOL, CLIENT_PLATFORM, CLIENT_HOSTNAME, CONNECTION_START_TIME, APPLICATION_ID, EXECUTION_ID"
                + " FROM TABLE(MON_GET_CONNECTION(cast(NULL as bigint), -2)) WHERE APPLICATION_HANDLE != MON_GET_APPLICATION_HANDLE()");
        try
        {
            while (rs.next())
            {
                cnx.runRawUpdate("CALL SYSPROC.ADMIN_CMD('FORCE APPLICATION (" + rs.getInt(1) + ")')");
            }
            rs.close();
        }
        catch (SQLException e)
        {
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
