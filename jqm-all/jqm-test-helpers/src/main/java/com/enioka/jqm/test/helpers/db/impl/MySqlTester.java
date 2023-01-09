package com.enioka.jqm.test.helpers.db.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.test.helpers.db.DbTester;

public class MySqlTester implements DbTester
{
    public static Logger jqmlogger = LoggerFactory.getLogger(MySqlTester.class);

    @Override
    public void simulateCrash(DbConn cnx)
    {
        ResultSet rs = cnx.runRawSelect("SELECT ID FROM INFORMATION_SCHEMA.PROCESSLIST WHERE USER = 'jqm' AND ID != CONNECTION_ID()");
        try
        {
            while (rs.next())
            {
                cnx.runRawUpdate("KILL CONNECTION " + rs.getInt(1));
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
