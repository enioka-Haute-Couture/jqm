package com.enioka.jqm.jdbc;

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
}
