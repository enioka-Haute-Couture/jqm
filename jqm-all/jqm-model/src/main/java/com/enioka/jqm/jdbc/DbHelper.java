package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbHelper
{
    private static Logger jqmlogger = LoggerFactory.getLogger(DbHelper.class);

    /**
     * Close utility method. ACtually for the Statement having created the ResultSet.
     *
     * @param rs
     *            statement to close (through a RS).
     */
    static void closeQuietly(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
                rs.getStatement().close();
            }
            catch (Exception e)
            {
                // Should go with the connection anyway.
                // jqmlogger.warn("Could not close a DB result set - possible pool leak", e);
            }
        }
    }

    /**
     * Close utility method.
     *
     * @param connection
     *            statement to close.
     */
    static void closeQuietly(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (Exception e)
            {
                jqmlogger.warn("Could not close a DB connection - possible pool leak", e);
            }
        }
    }

    /**
     * Close utility method.
     *
     * @param st
     *            statement to close.
     */
    static void closeQuietly(Statement st)
    {
        if (st != null)
        {
            try
            {
                st.close();
            }
            catch (Exception e)
            {
                // Should go with the connection anyway.
                // jqmlogger.warn("Could not close a DB statement - possible pool leak", e);
            }
        }
    }
}
