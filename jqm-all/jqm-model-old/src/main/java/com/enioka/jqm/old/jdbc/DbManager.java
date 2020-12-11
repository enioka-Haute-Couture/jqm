package com.enioka.jqm.old.jdbc;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DbManager
{
    /**
     * Note initialization HAS to be lazy. We do not know if the database driver is up when this code is run!
     */

    private static Logger jqmlogger = LoggerFactory.getLogger(DbManager.class);

    private static Db db;

    public static Db getDb()
    {
        initDbIfNeeded();
        return db;
    }

    public static void setDb(Db db)
    {
        DbManager.db = db;
    }

    private static void initDbIfNeeded()
    {
        if (db == null)
        {
            synchronized (DbManager.class)
            {
                if (db == null)
                {
                    try
                    {
                        // Load optional properties file
                        Properties p = Db.loadProperties();

                        // Connect to DB.
                        db = new Db(p);
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
}
