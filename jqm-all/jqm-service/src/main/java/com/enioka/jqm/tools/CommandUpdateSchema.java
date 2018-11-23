package com.enioka.jqm.tools;

import java.util.Properties;

import com.beust.jcommander.Parameters;
import com.enioka.admin.MetaService;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;

@Parameters(commandNames = "Update-Schema", commandDescription = "Updates the database schema.")
class CommandUpdateSchema extends CommandBase
{
    @Override
    int doWork()
    {
        if (!Helpers.isDbInitialized())
        {
            jqmlogger.info("Database connector initialization");
            Properties p = Db.loadProperties();
            p.setProperty("com.enioka.jqm.jdbc.allowSchemaUpdate", "true");
            Db db = new Db(p);
            Helpers.setDb(db);
        }

        try (DbConn cnx = Helpers.getNewDbSession())
        {
            Helpers.updateConfiguration(cnx);
            cnx.commit();
            jqmlogger.info("Upgrade done");
            jqmlogger.info("Existing nodes: " + MetaService.getNodes(cnx).size());
            return 0;
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not upgrade", e);
            return 101;
        }
    }
}
