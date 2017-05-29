package com.enioka.jqm.jdbc;

import java.io.Closeable;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: better way to close statements and RS.

/**
 * Db querying utility.
 */
public class DbConn implements Closeable
{
    private static Logger jqmlogger = LoggerFactory.getLogger(DbConn.class);

    private Db parent;
    Connection _cnx;
    private boolean transac_open = false;
    private boolean rollbackOnly = false;
    private List<Statement> toClose = new ArrayList<Statement>();

    DbConn(Db parent, Connection cnx)
    {
        this.parent = parent;
        this._cnx = cnx;
    }

    public void commit()
    {
        if (rollbackOnly)
        {
            throw new IllegalStateException("cannot commit a rollback only session. Use rollback first.");
        }
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
            rollbackOnly = false;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }

    public void setRollbackOnly()
    {
        rollbackOnly = true;
    }

    private QueryPreparation adapterPreparation(String query_key, boolean forUpdate, Object... params)
    {
        QueryPreparation qp = new QueryPreparation();
        qp.parameters = new ArrayList<Object>(Arrays.asList(params));
        qp.queryKey = query_key;
        qp.sqlText = parent.getQuery(query_key);
        qp.forUpdate = forUpdate;

        this.parent.getAdapter().beforeUpdate(_cnx, qp);
        return qp;
    }

    public QueryResult runUpdate(String query_key, Object... params)
    {
        transac_open = true;
        PreparedStatement ps = null;
        QueryPreparation qp = adapterPreparation(query_key, false, params);
        try
        {
            ps = prepare(qp);
            QueryResult qr = new QueryResult();
            qr.nbUpdated = ps.executeUpdate();
            qr.generatedKey = qp.preGeneratedKey;
            if (query_key.contains("insert") && !query_key.equals("history_insert_with_end_date"))
            {
                ResultSet gen = ps.getGeneratedKeys();
                if (gen.next())
                    try
                    {
                        qr.generatedKey = gen.getInt(1);
                    }
                    catch (SQLException e)
                    {
                        // nothing to do.
                    }
            }

            jqmlogger.debug("Updated rows: {}", qr.nbUpdated);
            return qr;
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

    void runRawUpdate(String query_sql)
    {
        transac_open = true;
        Statement s = null;
        try
        {
            String sql = parent.getAdapter().adaptSql(query_sql);
            if (sql.trim().isEmpty())
            {
                return;
            }
            jqmlogger.debug(sql);
            s = _cnx.createStatement();
            s.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            closeQuietly(s);
        }
    }

    public ResultSet runRawSelect(String rawQuery, Object... params)
    {
        PreparedStatement ps = null;
        QueryPreparation q = new QueryPreparation();
        q.parameters = new ArrayList<Object>(Arrays.asList(params));
        q.sqlText = this.parent.getAdapter().adaptSql(rawQuery);
        this.parent.getAdapter().beforeUpdate(_cnx, q);

        try
        {
            jqmlogger.debug("Running raw SQL query: {}", q.sqlText);
            for (Object o : params)
            {
                if (o == null)
                {
                    jqmlogger.debug("     null");
                }
                else
                {
                    jqmlogger.debug("     {} - {}", o.toString(), o.getClass());
                }
            }

            ps = _cnx.prepareStatement(q.sqlText, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            toClose.add(ps);
            int i = 0;
            for (Object prm : q.parameters)
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
            // closeQuietly(ps); // Closed when cnx is closed.
        }
    }

    public ResultSet runSelect(String query_key, Object... params)
    {
        return runSelect(false, query_key, params);
    }

    public ResultSet runSelect(boolean for_update, String query_key, Object... params)
    {
        PreparedStatement ps = null;
        QueryPreparation qp = adapterPreparation(query_key, for_update, params);
        try
        {
            ps = prepare(qp);
            toClose.add(ps);
            return ps.executeQuery();
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

    public Map<String, Object> runSelectSingleRow(String query_key, Object... params)
    {
        HashMap<String, Object> res = new HashMap<String, Object>();
        ResultSet rs = null;
        try
        {
            rs = runSelect(query_key, params);

            if (!rs.next())
            {
                throw new NoResultException("The query returned zero rows when one was expected.");
            }
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++)
            {
                // We take the type as returned, with an exception for small numerics (we do not want long or BigInt which cannot be cast)
                Object or;
                if (meta.getColumnType(i) == java.sql.Types.NUMERIC && meta.getPrecision(i) <= 10)
                {
                    or = rs.getInt(i);
                }
                else
                {
                    or = rs.getObject(i);
                }
                res.put(meta.getColumnName(i).toUpperCase(), or);
            }

            if (rs.next())
            {
                throw new NonUniqueResultException("The query returned more than one row when one was expected");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        finally
        {
            closeQuietly(rs);
        }
        return res;
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
                else if (clazz.equals(Calendar.class))
                {
                    res = (T) getCal(rs, column);
                }
                else if (clazz.equals(Long.class))
                {
                    res = (T) (Long) rs.getLong(column);
                }
                else if (clazz.equals(Float.class))
                {
                    res = (T) (Float) rs.getFloat(column);
                }
                else
                {
                    throw new DatabaseException("unsupported single query return type " + clazz.getCanonicalName());
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
        finally
        {
            closeQuietly(rs);
        }
    }

    /**
     * Close all JDBC objects related to this connection.
     */
    public void close()
    {
        for (Statement s : toClose)
        {
            closeQuietly(s);
        }
        toClose.clear();

        if (transac_open)
        {
            try
            {
                this._cnx.rollback();
            }
            catch (Exception e)
            {
                // Ignore.
            }
        }
        closeQuietly(_cnx);
    }

    /**
     * Close utility method.
     * 
     * @param ps
     *            statement to close.
     */
    public void closeQuietly(Closeable ps)
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
    public void closeQuietly(ResultSet ps)
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
    public void closeQuietly(Connection ps)
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
    public void closeQuietly(Statement ps)
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

    private PreparedStatement prepare(QueryPreparation q)
    {
        PreparedStatement ps = null;
        if (_cnx == null)
        {
            throw new IllegalStateException("Connection does not exist");
        }

        if (q.sqlText == null || q.sqlText.trim().isEmpty())
        {
            throw new DatabaseException("unknown query key");
        }

        // Debug
        jqmlogger.debug("Running {} : {} with {} parameters.", q.queryKey, q.sqlText, q.parameters.size());
        for (Object o : q.parameters)
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
            if (q.forUpdate)
                ps = _cnx.prepareStatement(q.sqlText, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            else
                ps = _cnx.prepareStatement(q.sqlText, this.parent.getAdapter().keyRetrievalColumn());
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }

        // Add parameters
        int i = 0;
        for (Object prm : q.parameters)
        {
            addParameter(prm, ++i, ps);
        }

        return ps;
    }

    private void addParameter(Object value, int position, PreparedStatement s)
    {
        // TODO: cache meta call and use setObject.
        try
        {
            if (value == null)
            {
                parent.getAdapter().setNullParameter(position, s);
            }
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
            throw new DatabaseException("Could not set parameter at position " + position, e);
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

    public String paginateQuery(String sql, int start, int stopBefore, List<Object> prms)
    {
        return this.parent.getAdapter().paginateQuery(sql, start, stopBefore, prms);
    }
}
