package com.enioka.jqm.jdbc;

import java.io.Closeable;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConn implements Closeable
{
    private static Logger jqmlogger = LoggerFactory.getLogger(DbConn.class);

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

    public void rollback()
    {
        try
        {
            _cnx.rollback();
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
            ps = prepare(query_key, false, params);
            QueryResult qr = new QueryResult();
            qr.nbUpdated = ps.executeUpdate();
            qr.generatedKeys = ps.getGeneratedKeys();
            jqmlogger.debug("Updated rows: {}", qr.nbUpdated);
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

    public ResultSet runRawSelect(String rawQuery, Object... params)
    {
        PreparedStatement ps = null;
        try
        {
            jqmlogger.debug("Running raw SQL query: {}", rawQuery);
            for (Object o : params)
            {
                if (o == null)
                {
                    jqmlogger.debug("     null");
                }
                else
                    jqmlogger.debug("     {} - {}", o.toString(), o.getClass());
            }

            ps = _cnx.prepareStatement(rawQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            int i = 0;
            for (Object prm : params)
            {
                addParameter(prm, ++i, ps);
            }

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

    public ResultSet runSelect(String query_key, Object... params)
    {
        return runSelect(false, query_key, params);
    }

    public ResultSet runSelect(boolean for_update, String query_key, Object... params)
    {
        PreparedStatement ps = null;
        try
        {
            ps = prepare(query_key, for_update, params);
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

    public <T> T runSelectSingle(String query_key, Class<T> clazz, Object... params)
    {
        return runSelectSingle(query_key, 1, clazz, params);
    }

    @SuppressWarnings("unchecked")
    public <T> T runSelectSingle(String query_key, int column, Class<T> clazz, Object... params)
    {
        ResultSet rs = runSelect(query_key, params);
        try
        {
            if (rs.getMetaData().getColumnCount() < 1)
            {
                throw new DatabaseException("query was supposed to return at least " + (column) + " columns - "
                        + rs.getMetaData().getColumnCount() + " were returned.");
            }

            T res;
            if (rs.next())
            {
                if (clazz.equals(Integer.class))
                {
                    res = (T) (Integer) rs.getInt(column);
                }
                else if (clazz.equals(String.class))
                {
                    res = (T) rs.getString(column);
                }
                else
                {
                    throw new DatabaseException("unsupported single query return type " + clazz.toGenericString());
                }
            }
            else
            {
                throw new NoResultException("query was supposed to return a single row - none returned");
            }
            if (rs.next())
            {
                throw new NonUniqueResultException("query was supposed to return a single row - multiple returned");
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
        if (transac_open)
        {
            try
            {
                this._cnx.rollback();
                transac_open = false;
            }
            catch (Exception e)
            {
                // Ignore.
            }
        }
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

    private PreparedStatement prepare(String query_key, boolean for_update, Object... params)
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

        // Debug
        jqmlogger.debug("Running {} : {} with {} parameters.", query_key, st, params.length);
        for (Object o : params)
        {
            if (o == null)
            {
                jqmlogger.debug("      null");
            }
            else
            {
                jqmlogger.debug("      {} - {}", o.toString(), o.getClass());
            }
        }

        try
        {
            if (for_update)
                ps = _cnx.prepareStatement(st, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            else
                ps = _cnx.prepareStatement(st, new String[] { "ID" });
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
            else if (value instanceof Calendar)
                s.setTimestamp(position, new Timestamp(((Calendar) value).getTimeInMillis()));
            else if (value instanceof List<?>)
            {
                Array a;
                List<?> vv = (List<?>) value;
                if (vv.size() == 0)
                {
                    throw new DatabaseException("Cannot do a query whith an empty list parameter");
                }
                if (vv.get(0) instanceof Integer)
                {
                    a = _cnx.createArrayOf("INTEGER", ((List<?>) value).toArray(new Integer[0]));
                }
                else if (vv.get(0) instanceof String)
                {
                    a = _cnx.createArrayOf("VARCHAR", ((List<?>) value).toArray(new String[0]));
                }
                else
                {
                    String[] vvv = new String[vv.size()];
                    int i = 0;
                    for (Object o : vv)
                    {
                        vvv[i++] = o.toString();
                    }
                    a = _cnx.createArrayOf("VARCHAR", vvv);
                }
                s.setArray(position, a);

            }
            else
            {
                s.setString(position, value.toString());
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }

    public Calendar getCal(ResultSet rs, int colIdx) throws SQLException
    {
        Calendar c = null;
        if (rs.getTimestamp(colIdx) != null)
        {
            c = Calendar.getInstance();
            c.setTimeInMillis(rs.getTimestamp(colIdx).getTime());
        }
        return c;
    }
}
