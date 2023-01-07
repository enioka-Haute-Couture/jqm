package com.enioka.jqm.test.helpers.db;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for test DB management.
 */
public class DbTesterManager
{
    public static Logger jqmlogger = LoggerFactory.getLogger(DbTesterManager.class);

    private static Map<String, String> testAdapters = new HashMap<String, String>();

    static
    {
        testAdapters.put("hsqldb", "com.enioka.jqm.test.helpers.db.impl.HsqlDbTester");
        testAdapters.put("postgresql", "com.enioka.jqm.test.helpers.db.impl.PgTester");
        testAdapters.put("oracle", "com.enioka.jqm.test.helpers.db.impl.OracleTester");
        testAdapters.put("mysql", "com.enioka.jqm.test.helpers.db.impl.MySqlTester");
        testAdapters.put("mariadb", "com.enioka.jqm.test.helpers.db.impl.MySqlTester");
        // testAdapters.put("db2", "com.enioka.jqm.test.helpers.db.impl.Db2Tester");
    }

    /**
     * Normalizes the test DB type name.
     *
     * @return
     */
    public static String getTestDbName()
    {
        String dbName = System.getenv("DB");
        if (dbName == null || dbName.isEmpty())
        {
            dbName = "hsqldb";
        }
        return dbName.toLowerCase();
    }

    /**
     * Get the tester for the current database, or null if no suitable tester exists.
     *
     * @return
     */
    public static DbTester getTestDbAdapter()
    {
        String dbName = getTestDbName();

        String testAdapterClassName = testAdapters.get(dbName);
        if (testAdapterClassName == null)
        {
            return null;
        }

        try
        {
            return DbTesterManager.class.getClassLoader().loadClass(testAdapterClassName).asSubclass(DbTester.class).newInstance();
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * JUnit helper - only do test if the current database is one of those given as a parameter.
     *
     * @param dbNames
     */
    public static void assumeSpecificDb(String... dbNames)
    {
        String testDbName = getTestDbName();
        for (String dbName : dbNames)
        {
            dbName = dbName.toLowerCase();
            if (testDbName.equals(dbName))
            {
                return;
            }
        }
        Assume.assumeTrue(false);
    }

    /**
     * JUnit helper - do not do test if the current database is one of those given as a parameter.
     *
     * @param dbNames
     */
    public static void assumeNotSpecificDb(String... dbNames)
    {
        String testDbName = getTestDbName();
        for (String dbName : dbNames)
        {
            dbName = dbName.toLowerCase();
            if (testDbName.equals(dbName))
            {
                Assume.assumeTrue(false);
            }
        }
        // If here - not one of the searched database, it's OK.
    }

    /**
     * JUnit helper - do not do test if the current database has no test adapter.
     */
    public static void assumeCurrentTestDbHasTestAdapter()
    {
        String testDbName = getTestDbName();
        Assume.assumeTrue(testAdapters.get(testDbName) != null);
    }
}
