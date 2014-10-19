package com.enioka.jqm.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Payload3 implements Runnable
{
    @Override
    public void run()
    {
        try
        {
            DataSource ds = (DataSource) InitialContext.doLookup("jdbc/test");

            Connection c = ds.getConnection();
            Statement s = c.createStatement();
            s.execute("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
            s.close();
            c.close();
        }
        catch (NamingException e)
        {
            throw new RuntimeException("could not retrieve the connection", e);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("could not run query", e);
        }
    }
}
