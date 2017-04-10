package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Db
{
    private static Logger jqmlogger = LoggerFactory.getLogger(Db.class);

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

    @SuppressWarnings("unchecked")
    public Db(Properties props)
    {
        if (!props.containsKey("com.enioka.jqm.jdbc.url"))
        {
            throw new IllegalArgumentException("No database URL (com.enioka.jqm.jdbc.url) in the database properties");
        }
        String url = props.getProperty("com.enioka.jqm.jdbc.url");

        DataSource ds = null;
        if (url.contains("jdbc:hsqldb"))
        {
            Class<? extends DataSource> dsclass;
            try
            {
                dsclass = (Class<? extends DataSource>) Class.forName("org.hsqldb.jdbc.JDBCDataSource");
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalStateException("The driver for database HSQLDB was not found in the classpath");
            }

            try
            {
                ds = dsclass.newInstance();
                dsclass.getMethod("setDatabase", String.class).invoke(ds, "jdbc:hsqldb:mem:testdbengine");
            }
            catch (Exception e)
            {
                throw new DatabaseException("could not create datasource. See errors below.", e);
            }
        }
        else
        {
            throw new IllegalArgumentException("this constructor does not support this database - URL " + url);
        }

        this._ds = ds;
        initQueries();
        initDb();
    }

    private void initDb()
    {
        jqmlogger.warn("Database is being upgraded");
        ScriptRunner.run(getConn(), "/sql/create_db.sql");
        jqmlogger.warn("Database is now up to date");
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

        jqmlogger.info("Using database " + product);
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
