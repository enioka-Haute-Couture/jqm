package com.enioka.jqm.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the one and only database connection pool of the application. Handles default properties loading (may be overloaded at
 * runtime).<br>
 * <br>
 * Note: initialization HAS to be lazy. We do not know if the database driver is up when this code is run!
 */
public final class DbManager
{
    private static Logger jqmlogger = LoggerFactory.getLogger(DbManager.class);

    private static Db db;

    /**
     * Returns the main database object. It is cached, and there is only one Db per Java process.<br>
     * Lazy init - first call is costly as all database init takes place.<br>
     *
     * @return the database entry point
     */
    public static Db getDb()
    {
        return getDb(null);
    }

    /**
     * Returns the main database object. It is cached, and there is only one Db per Java process.<br>
     * Lazy init - first call is costly as all database init takes place.<br>
     * The properties are only used on first call and ignore after that even if it changes or becomes null.
     *
     * @param overloadProps
     *            optional properties to initialize the database connection with
     * @return the database entry point
     */
    public static Db getDb(Properties overloadProps)
    {
        // Init this.db if needed.
        initDbIfNeeded(overloadProps);

        // Done
        return db;
    }

    private static void initDbIfNeeded(Properties overloadProps)
    {
        if (db == null)
        {
            synchronized (DbManager.class)
            {
                if (db == null)
                {
                    try
                    {
                        // Load optional properties file(s)
                        Properties props = loadProperties();

                        // Add overloads if any
                        if (overloadProps != null)
                        {
                            props.putAll(overloadProps);
                        }

                        // Connect to DB.
                        db = new Db(props);
                    }
                    catch (Exception e)
                    {
                        jqmlogger.error("Unable to connect with the database. Maybe your configuration file is wrong. "
                                + "Please check the password or the url in the $JQM_DIR/conf/resources.xml", e);
                        throw new RuntimeException("Database connection issue", e);
                    }
                }
            }
        }
    }

    /**
     * Helper method to load the standard JQM property files from class path.
     *
     * @return a Properties object, which may be empty but not null.
     */
    private static Properties loadProperties()
    {
        return loadProperties(new String[] { "META-INF/jqm.properties", "jqm.properties" });
    }

    /**
     * Helper method to load a property file from class path.
     *
     * @param filesToLoad
     *            an array of paths (class path paths) designating where the files may be. All files are loaded, in the order given. Missing
     *            files are silently ignored.
     *
     * @return a Properties object, which may be empty but not null.
     */
    private static Properties loadProperties(String[] filesToLoad)
    {
        Properties p = new Properties();

        for (String path : filesToLoad)
        {
            try (InputStream fis = ClassLoader.getSystemResourceAsStream(path))
            {
                if (fis != null)
                {
                    p.load(fis);
                    jqmlogger.info("A jqm.properties file was found at {}", path);
                }
            }
            catch (IOException e)
            {
                // We allow no configuration files, but not an unreadable configuration file.
                throw new DatabaseException("META-INF/jqm.properties file is invalid", e);
            }
        }

        // Overload the datasource name from environment variable if any (tests only).
        String dbName = System.getenv("DB");
        if (dbName != null)
        {
            p.put("com.enioka.jqm.jdbc.datasource", "jdbc/" + dbName);
        }

        // Done
        return p;
    }
}
