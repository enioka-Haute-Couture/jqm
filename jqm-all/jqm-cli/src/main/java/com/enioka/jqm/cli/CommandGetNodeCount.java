package com.enioka.jqm.cli;

import java.util.List;

import com.beust.jcommander.Parameters;
import com.enioka.admin.MetaService;
import com.enioka.api.admin.NodeDto;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;

@Parameters(commandNames = "Get-NodeCount", commandDescription = "Get how many nodes are declared inside the central configuration.")
class CommandGetNodeCount extends CommandBase
{
    @Override
    public int doWork()
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            List<NodeDto> nodes = MetaService.getNodes(cnx);
            for (NodeDto node : nodes)
            {
                jqmlogger.info("Already existing: " + node.getName());
            }
            jqmlogger.info("Existing nodes: " + nodes.size());
            return 0;
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not fetch node count", e);
            return 111;
        }
    }
}
