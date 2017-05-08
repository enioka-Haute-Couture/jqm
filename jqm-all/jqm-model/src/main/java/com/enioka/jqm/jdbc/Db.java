package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for all database-related operations, from initialization to schema upgrade, as well as creating sessions for querying the
 * database.
 */
public class Db
{
    private static Logger jqmlogger = LoggerFactory.getLogger(Db.class);

    /**
     * The version of the schema as it described in the current Maven artifact
     */
    private static final int SCHEMA_VERSION = 1;

    /**
     * The SCHEMA_VERSION version is backward compatible until this version
     */
    private static final int SCHEMA_COMPATIBLE_VERSION = 1;

    /**
     * The list of different database adapters. We are using reflection for loading them for future extensibility.
     */
    private static String[] ADAPTERS = new String[] { "com.enioka.jqm.jdbc.DbImplPg", "com.enioka.jqm.jdbc.DbImplHsql",
            "com.enioka.jqm.jdbc.DbImplOracle" };

    private DataSource _ds;
    private DbAdapter adapter = null;
    private Map<String, String> _queries;
    private String product;

    public Db(String dsName)
    {
        try
        {
            this._ds = (DataSource) InitialContext.doLookup(dsName);
        }
        catch (NamingException e)
        {
            throw new DatabaseException("Could not retrieve datasource resource named " + dsName, e);
        }
        if (this._ds == null)
        {
            throw new DatabaseException("no data source found");
        }
        init();
    }

    public Db()
    {
        this("jdbc/jqm");
    }

    public Db(DataSource ds)
    {
        this._ds = ds;
        init();
    }

    /**
     * Debug constructor only. Works only with HSQLDB.
     * 
     * @param props
     */
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
        init();
    }

    /**
     * Main database initialization. To be called only when _ds is a valid DataSource.
     */
    private void init()
    {
        initAdapter();
        initQueries();
        dbUpgrade();
        checkSchemaVersion();
    }

    private void checkSchemaVersion()
    {
        DbConn cnx = this.getConn();
        int db_schema_version = 0;
        int db_schema_compat_version = 0;
        Map<String, Object> rs = null;
        try
        {
            rs = cnx.runSelectSingleRow("version_select_latest");
            db_schema_version = (Integer) rs.get("VERSION_D1");

            db_schema_compat_version = (Integer) rs.get("COMPAT_D1");
        }
        catch (NoResultException e)
        {
            // Database is to be created, so version 0.
        }
        catch (Exception z)
        {
            throw new DatabaseException("could not retrieve version information from database", z);
        }
        finally
        {
            cnx.close();
        }

        if (SCHEMA_VERSION > db_schema_version)
        {
            if (SCHEMA_COMPATIBLE_VERSION <= db_schema_version)
            {
                // OK
                return;
            }
        }
        if (SCHEMA_VERSION == db_schema_version)
        {
            // OK
            return;
        }
        if (SCHEMA_VERSION < db_schema_version)
        {
            if (SCHEMA_VERSION >= db_schema_compat_version)
            {
                // OK
                return;
            }
        }

        // If here, not OK at all.
        throw new DatabaseException("Database schema version mismatch. This library can work with schema versions from "
                + SCHEMA_COMPATIBLE_VERSION + " to at least " + SCHEMA_VERSION + " but database is in version " + db_schema_version);
    }

    /**
     * Updates the database. Never call this during normal operations, upgrade is a user-controlled operation.
     */
    private void dbUpgrade()
    {

        DbConn cnx = this.getConn();
        Map<String, Object> rs = null;
        int db_schema_version = 0;
        try
        {
            rs = cnx.runSelectSingleRow("version_select_latest");
            db_schema_version = (Integer) rs.get("VERSION_D1");
        }
        catch (Exception e)
        {
            // Database is to be created, so version 0 is OK.
        }
        cnx.rollback();

        if (SCHEMA_VERSION > db_schema_version)
        {
            jqmlogger.warn("Database is being upgraded from version {} to version {}", db_schema_version, SCHEMA_VERSION);

            // Upgrade scripts are named from_to.sql with 5 padding (e.g. 00000_00003.sql)
            // We try to find the fastest path (e.g. a direct 00000_00005.sql for creating a version 5 schema from nothing)
            // This is a simplistic and non-optimal algorithm as we try only a single path (no going back)

            int loop_from = db_schema_version;
            int to = db_schema_version;
            List<String> toApply = new ArrayList<String>();

            while (to != SCHEMA_VERSION)
            {
                boolean progressed = false;
                for (int loop_to = SCHEMA_VERSION; loop_to > db_schema_version; loop_to--)
                {
                    String migrationFileName = String.format("/sql/%05d_%05d.sql", loop_from, loop_to);
                    jqmlogger.debug("Trying migration script {}", migrationFileName);
                    if (Db.class.getResource(migrationFileName) != null)
                    {
                        toApply.add(migrationFileName);
                        to = loop_to;
                        loop_from = loop_to;
                        progressed = true;
                        break;
                    }
                }

                if (!progressed)
                {
                    break;
                }
            }
            if (to != SCHEMA_VERSION)
            {
                throw new DatabaseException(
                        "There is no migration path from version " + db_schema_version + " to version " + SCHEMA_VERSION);
            }

            for (String s : toApply)
            {
                jqmlogger.info("Running migration script {}", s);
                ScriptRunner.run(cnx, s);
            }
            cnx.commit(); // Yes, really. For advanced DB!

            cnx.close(); // HSQLDB does not refresh its schema without this.
            cnx = getConn();

            cnx.runUpdate("version_insert", SCHEMA_VERSION, SCHEMA_COMPATIBLE_VERSION);
            cnx.commit();
            jqmlogger.info("Database is now up to date");
        }
        else
        {
            jqmlogger.info("Database is already up to date");
        }
        cnx.close();
    }

    /**
     * Creates the adapter for the target database.
     */
    private void initAdapter()
    {
        Connection tmp = null;
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

        jqmlogger.info("Database reports it is " + product);

        DbAdapter newAdpt = null;
        for (String s : ADAPTERS)
        {
            try
            {
                Class<? extends DbAdapter> clazz = Db.class.getClassLoader().loadClass(s).asSubclass(DbAdapter.class);
                newAdpt = clazz.newInstance();
                if (newAdpt.compatibleWith(product))
                {
                    adapter = newAdpt;
                    break;
                }
            }
            catch (Exception e)
            {
                throw new DatabaseException("Issue when loading database adapter named: " + s, e);
            }
        }

        if (adapter == null)
        {
            throw new DatabaseException("Unsupported database! There is no JQM database adapter compatible with product name " + product);
        }

        // TODO: go to DS metadata and check supported versions of databases.
    }

    /**
     * Create the query cache (with db-specific queries)
     */
    private void initQueries()
    {
        for (String key : DbImplBase.queries.keySet())
        {
            DbImplBase.queries.put(key, this.adapter.adaptSql(DbImplBase.queries.get(key)));
        }
        _queries = DbImplBase.queries;

        // Replace parameters
        for (Map.Entry<String, String> p : _queries.entrySet())
        {
            p.setValue(String.format(p.getValue(), ""));
        }
    }

    /**
     * A connection to the database. Should be short-lived. No transaction active by default.
     * 
     * @return a new open connection.
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
}
