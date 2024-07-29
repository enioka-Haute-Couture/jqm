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
     * @return
     */
    int updateSchema(Connection connection);

    /**
     * Returns the SQL that would be applied by the updateSchema method.
     *
     * @param connection
     * @return
     */
    String getUpdateSchemaSql(Connection connection);

    /**
     * Returns true if the database schema is up to date.
     *
     * @param connection
     * @return
     */
    boolean isUpToDate(Connection connection);
}
