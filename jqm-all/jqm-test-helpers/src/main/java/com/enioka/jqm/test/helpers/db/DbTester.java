package com.enioka.jqm.test.helpers.db;

import com.enioka.jqm.jdbc.DbConn;

/**
 * A test entry point to simulate DB failures.
 */
public interface DbTester
{
    /**
     * Called before all tests.
     */
    void init();

    /**
     * Called after all tests.
     */
    void stop();

    /**
     * Should try to simulate what happens when the database is violently disconnected from JQM. Usually simulated with a session kill.
     *
     * @param cnx
     */
    void simulateCrash(DbConn cnx);

    void simulateResumeAfterCrash(DbConn cnx);
}
