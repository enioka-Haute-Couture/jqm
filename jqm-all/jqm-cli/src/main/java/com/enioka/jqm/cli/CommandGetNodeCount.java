package com.enioka.jqm.cli;

import com.beust.jcommander.Parameters;
import com.enioka.admin.MetaService;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;

@Parameters(commandNames = "Get-NodeCount", commandDescription = "Get how many nodes are declared inside the central configuration.")
class CommandGetNodeCount extends CommandBase
{
    @Override
    int doWork()
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            jqmlogger.info("Existing nodes: " + MetaService.getNodes(cnx).size());
            return 0;
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not fetch node count", e);
            return 111;
        }
    }
}
