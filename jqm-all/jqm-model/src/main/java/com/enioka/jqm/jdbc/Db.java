package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Db
{
    private DataSource _ds;
    private Map<String, String> _queries;

    public Db(String dsName)
    {
        try
        {
            this._ds = (DataSource) InitialContext.doLookup(dsName);
        }
        catch (NamingException e)
        {
            throw new DatabaseException(e);
        }
        if (this._ds == null)
        {
            throw new DatabaseException("no data source found");
        }
        initQueries();
    }

    public Db()
    {
        this("jdbc/jqm");
    }

    public Db(DataSource ds)
    {
        this._ds = ds;
        initQueries();
        initDb();
    }

    private void initDb()
    {
        ScriptRunner.run(getConn(), "/sql/create_db.sql");
    }

    private void initQueries()
    {
        Connection tmp = null;
        String product;
        try
        {
            tmp = _ds.getConnection();
            product = tmp.getMetaData().getDatabaseProductName().toLowerCase();
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Cannot connect to the database", e);
        }
        finally
        {
            try
            {
                tmp.close();
            }
            catch (SQLException e)
            {
                // Nothing to do.
            }
        }

        System.out.println(product);
        if (product.contains("oracle"))
        {
            _queries = DbImplOracle.getQueries();
        }
        else if (product.contains("hsql"))
        {
            _queries = DbImplHsql.getQueries();
        }
        else
        {
            throw new DatabaseException("Unsupported database");
        }

        // TODO: go to DS metadata and check supported versions of databases.

        // Replace parameters
        for (Map.Entry<String, String> p : _queries.entrySet())
        {
            p.setValue(String.format(p.getValue(), ""));
        }
    }

    /**
     * A connection to the database. Should be short-lived. No transaction active by default.
     * 
     * @return
     */
    public DbConn getConn()
    {
        try
        {
            Connection cnx = _ds.getConnection();
            cnx.setAutoCommit(false);
            return new DbConn(this, cnx);
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }

    /**
     * Gets the interpolated text of a query from cache. If key does not exist, an exception is thrown.
     * 
     * @param key
     *            name of the query
     * @return the query text
     */
    String getQuery(String key)
    {
        String res = this._queries.get(key);
        if (res == null)
        {
            throw new DatabaseException("Query does not exist");
        }
        return res;

    }

}
