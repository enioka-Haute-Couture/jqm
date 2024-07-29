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

@Parameters(commandNames = "Update-Schema", commandDescription = "Updates the database schema.")
public class DbUpdateVerb extends CommandBase
{

    @Override
    public int doWork()
    {
        var dbSchemaManager = ServiceLoaderHelper.getService(ServiceLoader.load(DbSchemaManager.class));
        Properties properties = new Properties();
        properties.put("com.enioka.jqm.jdbc.waitForConnectionValid", "true");
        properties.put("com.enioka.jqm.jdbc.waitForSchemaValid", "false");

        Db db = DbManager.getDb(properties);
        try (var cnx = db.getDataSource().getConnection())
        {
            dbSchemaManager.updateSchema(cnx);
            return 0;
        }
        catch (SQLException e)
        {
            jqmlogger.error("Could not upgrade database schema", e);
            return 1;
        }
    }
}
