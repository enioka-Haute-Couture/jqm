package com.enioka.jqm.model.updater.cli;

import java.sql.SQLException;
import java.util.Properties;
import java.util.ServiceLoader;


import com.beust.jcommander.Parameters;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.updater.DbSchemaManager;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;

@Parameters(commandNames = "Test-Schema", commandDescription = "Test if the database schema is up to date.")
public class TestDbUpToDateVerb extends CommandBase
{

    @Override
    public int doWork()
    {
        var dbSchemaManager = ServiceLoaderHelper.getService(ServiceLoader.load(DbSchemaManager.class));
        var properties = new Properties();
        properties.put("com.enioka.jqm.jdbc.waitForConnectionValid", "true");
        properties.put("com.enioka.jqm.jdbc.waitForSchemaValid", "false");
        Db db = DbManager.getDb(properties);
        try (var cnx = db.getDataSource().getConnection())
        {
            if (dbSchemaManager.isUpToDate(db.getDataSource().getConnection()))
            {
                jqmlogger.info("Database schema is up to date");
                return 0;
            }
            else
            {
                jqmlogger.error("Database schema is not up to date");
                return 1;
            }
        }
        catch (SQLException e)
        {
            jqmlogger.error("Error while checking database schema", e);
            return 2;
        }
    }
}
