package com.enioka.jqm.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.model.updater.api.DbSchemaManager;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;

/**
 * Entry point for all database-related operations, from initialization to schema upgrade, as well as creating sessions for querying the
 * database.
 */
public class Db
{
    private static Logger jqmlogger = LoggerFactory.getLogger(Db.class);

    private DataSource _ds = null;
    private DbAdapter adapter = null;
    private String product;
    private Properties p = null;

    /**
     * Connects to the database by retrieving a DataDource from JNDI (with every parameter set to default, including the JNDI alias for the
     * DataSource being jdbc/jqm).
     */
    public Db()
    {
        this(null);
    }

    /**
     * Main constructor. Properties may be null. Properties are not documented on purpose, as this is a private JQM API.
     *
     * @param properties
     */
    public Db(Properties properties)
    {
        this(getNewDataSource(false, properties), properties);
    }

    /**
     * Constructor for cases when a DataSource is readily available (and not retrieved through JNDI).
     *
     * @param ds
     *            the existing DataSource.
     * @param properties
     *            properties ue at initialization time.
     */
    public Db(DataSource ds, Properties properties)
    {
        this._ds = ds;
        this.p = new Properties();
        if (properties != null)
        {
            this.p.putAll(properties);
        }
        initAdapter(this.p);
    }

    private static DataSource getNewDataSource(boolean useCompEnv, Properties properties)
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        String dsName = properties.getProperty("com.enioka.jqm.jdbc.datasource", "jdbc/jqm");
        int retryCount = Integer.parseInt(properties.getProperty("com.enioka.jqm.jdbc.initialRetries", "10"));
        int waitMs = Integer.parseInt(properties.getProperty("com.enioka.jqm.jdbc.initialRetryWaitMs", "10000"));

        if (useCompEnv)
        {
            // This is a hack. Some containers will use root context as default for JNDI (WebSphere, Glassfish...), other will use
            // java:/comp/env/ (Tomcat...). So if we actually know the required alias, we try both, and the user only has to provide a
            // root JNDI alias that will work in both cases.
            dsName = "java:/comp/env/" + dsName;
        }

        int retries = 0;
        DataSource res = null;

        // Get DataSource from JNDI (either JQM self-hosted JNDI directory or the directory of the app server, depending on the use case)
        while (res == null)
        {
            try
            {
                res = (DataSource) InitialContext.doLookup(dsName);
            }
            catch (NameNotFoundException e2)
            {
                res = null;
            }
            catch (Exception e)
            {
                if (++retries > retryCount)
                {
                    break;
                }
                // TODO: naming exception name does not exist.

                String msg = e.getLocalizedMessage();
                if (e.getCause() != null)
                {
                    msg += " - " + e.getCause().getLocalizedMessage();
                    jqmlogger.error(
                            "Database not available: " + msg + ". Retry " + retries + "/" + retryCount + ". Waiting for database...");

                    try
                    {
                        Thread.sleep(waitMs);
                    }
                    catch (InterruptedException e1)
                    {
                        // Nothing to do.
                    }
                }
            }
        }

        if (res == null && !useCompEnv)
        {
            return getNewDataSource(true, properties);
        }
        else if (res == null)
        {
            throw new DatabaseException("no data source found");
        }

        return res;
    }

    /**
     * The raw Datasource object. This should not be used except when needing to interact with the database before it is fully initialized.
     */
    public DataSource getDataSource()
    {
        return _ds;
    }

    /**
     * Main database initialization. To be called only when _ds is a valid DataSource.
     */
    private void initAdapter(Properties p)
    {
        // Select a DB adapter
        selectAdapter(p);

        // Adapt and cache all JQM SQL queries with the selected DB adapter.
        initQueries();

        // If DB is in wrong version or not available, just wait for it to be ready.
        // First wait for the low-level JDBC connection to be valid, meaning connected to DB.
        if ("true".equals(this.p.getProperty("com.enioka.jqm.jdbc.waitForConnectionValid", "true")))
        {
            waitForDatabaseConnected();
        }

        // Then wait for the schema to be created and in correct version.
        if ("true".equals(this.p.getProperty("com.enioka.jqm.jdbc.waitForSchemaValid", "true")))
        {
            waitForSchemaReady();
        }
    }

    private void waitForDatabaseConnected()
    {
        while (true)
        {
            try (var cnx = this._ds.getConnection())
            {
                if (cnx.isValid(1000))
                {
                    break;
                }
                jqmlogger.error("Database not yet available. Waiting for database...");
                wait(10000);
            }
            catch (SQLException e)
            {
                var msg = e.getLocalizedMessage();
                if (e.getCause() != null)
                {
                    msg += " - " + e.getCause().getLocalizedMessage();
                }
                jqmlogger.error("Database not yet available: {}. Waiting for database...", msg);
                wait(10000);
            }
        }
    }

    private void waitForSchemaReady()
    {
        // Then wait for the schema to be created and in correct version.
        while (true)
        {
            if (checkDbIsReady())
            {
                break;
            }
            jqmlogger.error("Database connected but schema not yet created. Waiting for database...");
            wait(10000);
        }
    }

    private void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            // Nothing to do.
        }
    }

    private boolean checkDbIsReady()
    {
        String dbAdaptersProp = p.getProperty("com.enioka.jqm.jdbc.adapters");
        if ((dbAdaptersProp == null || dbAdaptersProp.isEmpty()) && FrameworkUtil.getBundle(getClass()) != null)
        {
            // OSGi case
            try (var cnx = _ds.getConnection())
            {
                var context = FrameworkUtil.getBundle(getClass()).getBundleContext();
                var serviceRef = context.getServiceReference(DbSchemaManager.class);
                if (serviceRef != null)
                {
                    var dbSchemaManager = context.getService(serviceRef);
                    if (dbSchemaManager != null)
                    {
                        jqmlogger
                                .debug("Checking if database schema is up to date using full Liquibase service inside an OSGi environment");
                        return dbSchemaManager.isUpToDate(cnx);
                    }
                }
            }
            catch (Exception e)
            {
                throw new DatabaseException("Issue when loading database adapter", e);
            }
        }

        // If here: not OSGi or no Liquibase service available.
        jqmlogger.debug("Checking if database schema is up to date using direct JDBC connection");
        try (DbConn cnx = this.getConn())
        {
            cnx.runSelect("jndi_select_all");
            return true;
        }
        catch (NoResultException e)
        {
            // Empty table means OK
            return true;
        }
        catch (Exception z)
        {
            return false;
        }
    }

    /**
     * Creates the adapter for the target database. The list of available adapters comes either from ServiceLoader or from a property.
     */
    private void selectAdapter(Properties p)
    {
        try (Connection tmp = _ds.getConnection())
        {
            DatabaseMetaData meta = tmp.getMetaData();
            product = meta.getDatabaseProductName().toLowerCase();
            jqmlogger.info("Database reports it is " + meta.getDatabaseProductName() + " " + meta.getDatabaseMajorVersion() + "."
                    + meta.getDatabaseMinorVersion());

            // Find the correct db apdater
            jqmlogger.info("Database adapter search: using METAINF services");
            try
            {
                for (var service : ServiceLoaderHelper.getServices(ServiceLoader.load(DbAdapter.class)))
                {
                    jqmlogger.info("\t\tFound DB adapter plugin {}", service.getClass().getCanonicalName());
                    if (service.compatibleWith(meta))
                    {
                        adapter = service;
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                throw new DatabaseException("Issue when loading database adapter", e);
            }
        }
        catch (SQLException e1)
        {
            throw new DatabaseException("Cannot connect to the database", e1);
        }

        if (adapter == null)
        {
            throw new DatabaseException("Unsupported database! There is no JQM database adapter compatible with product name " + product);
        }
        else
        {
            jqmlogger.info("Using database adapter {}", adapter.getClass().getCanonicalName());
        }
    }

    /**
     * Create the query cache (with db-specific queries)
     */
    private void initQueries()
    {
        DbConn cnx = getConn();
        adapter.prepare(p, cnx._cnx);
        cnx.close();
    }

    /**
     * A connection to the database. Should be short-lived. No transaction active by default.
     *
     * @return a new open connection.
     */
    public DbConn getConn()
    {
        Connection cnx = null;
        try
        {
            Thread.interrupted(); // this is VERY sad. Needed for Oracle driver which otherwise fails spectacularly.
            cnx = _ds.getConnection();
            if (cnx.getAutoCommit())
            {
                cnx.setAutoCommit(false);
                cnx.rollback(); // To ensure no open transaction created by the pool before changing TX mode
            }

            if (cnx.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED)
            {
                cnx.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }

            return new DbConn(this, cnx);
        }
        catch (SQLException e)
        {
            DbHelper.closeQuietly(cnx); // May have been left open when the pool has given us a failed connection.
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
        String res = this.adapter.getSqlText(key);
        if (res == null)
        {
            throw new DatabaseException("Query " + key + " does not exist");
        }
        return res;
    }

    DbAdapter getAdapter()
    {
        return this.adapter;
    }

    public String getProduct()
    {
        return this.product;
    }

    /**
     * For tests we allow trying to close the datasource. As it is often a pooled connection, we may need to free the pool between tests.
     */
    void close()
    {
        if (_ds == null)
        {
            return;
        }

        try
        {
            Method m = _ds.getClass().getMethod("close", boolean.class);
            m.invoke(_ds, true);
            jqmlogger.info("Connection pool was closed with purge");
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                Method m = _ds.getClass().getMethod("close");
                m.invoke(_ds);
                jqmlogger.info("Connection pool was closed without purge");
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                   InvocationTargetException e1)
            {
                // nothing to do - this DS cannot be closed.
            }
        }
        catch (Exception e)
        {
            // nothing to do - this DS cannot be closed.
        }
        _ds = null;
    }
}
