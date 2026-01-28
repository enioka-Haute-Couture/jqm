package com.enioka.jqm.model.updater;

import java.sql.Connection;

/**
 * Database schema version management service.
 */
public interface DbSchemaManager
{
    /**
     * Applies all pending migrations and returns the applied changeset count.
     *
     * @param connection
     *            the database connection to use
     * @return the number of changesets applied
     */
    int updateSchema(Connection connection);

    /**
     * Returns the SQL that would be applied by the updateSchema method.
     *
     * @param connection
     *            the database connection to use
     * @return the SQL statements that would be executed
     */
    String getUpdateSchemaSql(Connection connection);

    /**
     * Returns true if the database schema is up to date.
     *
     * @param connection
     *            the database connection to use
     * @return true if schema is current, false if migrations are pending
     */
    boolean isUpToDate(Connection connection);
}
