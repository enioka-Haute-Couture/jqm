package com.enioka.jqm.jdbc;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbHelper
{
    /**
     * Close utility method.
     * 
     * @param ps
     *            statement to close.
     */
    static void closeQuietly(Closeable ps)
    {
        if (ps != null)
        {
            try
            {
                ps.close();
            }
            catch (Exception e)
            {
                // Do nothing.
            }
        }
    }

    /**
     * Close utility method.
     * 
     * @param ps
     *            statement to close.
     */
    static void closeQuietly(ResultSet ps)
    {
        if (ps != null)
        {
            try
            {
                ps.close();
            }
            catch (Exception e)
            {
                // Do nothing.
            }
        }
    }

    /**
     * Close utility method.
     * 
     * @param ps
     *            statement to close.
     */
    static void closeQuietly(Connection ps)
    {
        if (ps != null)
        {
            try
            {
                ps.close();
            }
            catch (Exception e)
            {
                // Do nothing.
            }
        }
    }

    /**
     * Close utility method.
     * 
     * @param ps
     *            statement to close.
     */
    static void closeQuietly(Statement ps)
    {
        if (ps != null)
        {
            try
            {
                ps.close();
            }
            catch (Exception e)
            {
                // Do nothing.
            }
        }
    }
}
