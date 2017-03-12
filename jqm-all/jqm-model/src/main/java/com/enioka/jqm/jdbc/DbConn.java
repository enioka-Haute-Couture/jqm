package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

public class DbConn
{

    private Db parent;
    private Connection _cnx;
    private boolean transac_open = false;

    DbConn(Db parent, Connection cnx)
    {
        this.parent = parent;
        this._cnx = cnx;
    }

    public QueryResult runUpdate(boolean commit, String query_key, Object... params)
    {
        QueryResult res = runUpdate(query_key, params);
        commit();
        return res;
    }

    public void commit()
    {
        try
        {
            _cnx.commit();
            transac_open = false;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }

    public QueryResult runUpdate(String query_key, Object... params)
    {
        transac_open = true;
        PreparedStatement ps = null;
        try
        {
            ps = prepare(query_key, params);
            QueryResult qr = new QueryResult();
            qr.nbUpdated = ps.executeUpdate();
            qr.generatedKeys = ps.getGeneratedKeys();
            return qr;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        finally
        {
            // closeQuietly(ps);
        }
    }

    void runRawUpdate(String query_sql)
    {
        Statement s = null;
        try
        {
            s = _cnx.createStatement();
            s.executeQuery(query_sql);
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            try
            {
                s.close();
            }
            catch (Exception e)
            {
                // Ignore.
            }
        }

    }

    public ResultSet runSelect(String query_key, Object... params)
    {
        PreparedStatement ps = null;
        try
        {
            ps = prepare(query_key, params);
            return ps.executeQuery();
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        finally
        {
            closeQuietly(ps);
        }
    }

    public int runSelectSingleInt(String query_key, Object... params)
    {
        ResultSet rs = runSelect(query_key, params);
        try
        {
            if (rs.getMetaData().getColumnCount() != 1)
            {
                throw new DatabaseException("query was supposed to return a single column - 0 or more than 2 returned.");
            }
            if (!(rs.getMetaData().getColumnType(0) == java.sql.Types.INTEGER
                    || rs.getMetaData().getColumnType(0) != java.sql.Types.SMALLINT))
            {
                throw new DatabaseException("query was supposed to return an integer - wrong datatype");
            }

            int res;
            if (rs.next())
            {
                res = rs.getInt(0);
            }
            else
            {
                throw new DatabaseException("query was supposed to return a single row - none returned");
            }
            if (rs.next())
            {
                throw new DatabaseException("query was supposed to return a single row - multiple returned");
            }

            return res;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }

    /**
     * Close all JDBC objects related to this connection.
     */
    public void close()
    {
        try
        {
            this._cnx.close();
        }
        catch (Exception s)
        {
            // Ignore.
        }
    }

    /**
     * Close a statement. Connection itself is left open.
     * 
     * @param ps
     *            statement to close.
     */
    private void closeQuietly(PreparedStatement ps)
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

    private PreparedStatement prepare(String query_key, Object... params)
    {
        PreparedStatement ps = null;
        String st = parent.getQuery(query_key);
        if (_cnx == null)
        {
            throw new IllegalStateException("Connection does not exist");
        }

        if (st == null)
        {
            throw new DatabaseException("unknown query key");
        }
        System.out.println("Running " + st + " with " + params.length + " parameters.");
        for (Object o : params)
            if (o == null)
            {
                System.out.println("     null");
            }
            else
                System.out.println("     " + o.toString() + "         " + o.getClass());

        try
        {
            ps = _cnx.prepareStatement(st, Statement.RETURN_GENERATED_KEYS);
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }

        // Add parameters
        int i = 0;
        for (Object prm : params)
        {
            addParameter(prm, ++i, ps);
        }

        return ps;
    }

    private void addParameter(Object value, int position, PreparedStatement s)
    {
        try
        {
            if (value == null)
                s.setNull(position, Types.VARCHAR);
            else if (Integer.class == value.getClass())
                s.setInt(position, (Integer) value);
            else if (Long.class == value.getClass())
                s.setLong(position, (Long) value);
            else if (String.class == value.getClass())
                s.setString(position, (String) value);
            else if (Timestamp.class == value.getClass())
                s.setTimestamp(position, (Timestamp) value);
            else if (Time.class == value.getClass())
                s.setTime(position, (Time) value);
            else if (Boolean.class == value.getClass())
                s.setBoolean(position, (Boolean) value);
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }
}
