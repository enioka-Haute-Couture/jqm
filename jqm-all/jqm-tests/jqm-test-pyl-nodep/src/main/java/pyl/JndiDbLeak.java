package pyl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class JndiDbLeak
{
    public static void main(String[] args)
    {
        DataSource ds = null;
        Connection c = null;
        Statement s = null;
        ResultSet rs = null;
        try
        {
            ds = (DataSource) InitialContext.doLookup("jdbc/test");
            c = ds.getConnection();
            s = c.createStatement();
            rs = s.executeQuery("SELECT count(1) FROM INFORMATION_SCHEMA.COLUMNS");
            rs.next();
            long k = rs.getLong(1);
            System.out.println("number of columns in database : " + k);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not find JNDI resource or other issue", e);
        }

        // Do NOT close anything, this is a leak simulation.
    }
}
