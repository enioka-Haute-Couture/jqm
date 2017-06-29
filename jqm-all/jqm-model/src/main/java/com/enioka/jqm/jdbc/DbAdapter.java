package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * The interface to implement to create a new database adapter. Adapters contain all the database-specific stuff for running JQM on a
 * specific database brand.<br>
 * <br>
 * All implementations must have a no-argument constructor.
 *
 */
public interface DbAdapter
{
    /**
     * Tests if the adapter is compatible with a given database. When in doubt, answer no.
     * 
     * @param product
     *            the product code (from Connection.getMetaData().getDatabaseProductName()) in lower case.
     * @return true if compatible, false otherwise.
     */
    public boolean compatibleWith(String product);

    /**
     * Adapt the given query to enable it to run on the target database.This is only called on startup, as queries are cached by the engine.
     * 
     * @param sql
     *            the SQL query (full text, no terminator).
     * @return a ready to use query.
     */
    public String adaptSql(String sql);

    /**
     * The name of the columns to retrieve for getGeneratedKeys calls. (some dbs want uppercase, other lowercase).
     * 
     * @return the list of id columns.
     */
    public String[] keyRetrievalColumn();

    /**
     * A list of files to run (from the classpath) before running schema upgrades.
     */
    public List<String> preSchemaCreationScripts();

    /**
     * Hook run before creating a new update Statement.
     * 
     * @param cnx
     *            an open and ready to use connection to the database. Please return it without any open statement/result set.
     * @param q
     *            the query which will be run. Can be modified by this method.
     * @return a generated ID or null;
     */
    public void beforeUpdate(Connection cnx, QueryPreparation q);

    /**
     * Called after creating the first connection. The adapter should create its caches and do all initialization it requires. Most
     * importantly, the SQL query cache should be created.
     * 
     * @param cnx
     *            an open ready to use connection to the database.
     */
    public void prepare(Properties p, Connection cnx);

    /**
     * Returns a ready to run SQL text for the given key. (adaptSql does not need to be run on the returned string)
     * 
     * @param key
     *            key of the SQL order
     * @return full text to run on the database. Null if key not found.
     */
    public String getSqlText(String key);

    /**
     * Databases all have different issues when settings null parameters.
     * 
     * @param position
     *            in the statement (sql parameters).
     * @param s
     *            statement being built.
     */
    public void setNullParameter(int position, PreparedStatement s) throws SQLException;

    /**
     * Adds pagination elements to a query.
     * 
     * @param sql
     *            the full SQL text
     * @param start
     *            the first included row (start of page). First row is zero, not one.
     * @param stopBefore
     *            the first excluded row (end of page)
     * @param prms
     *            the bind variables values. Can be modified.
     * @return the ready to use SQL query for this database.
     */
    public String paginateQuery(String sql, int start, int stopBefore, List<Object> prms);
}
