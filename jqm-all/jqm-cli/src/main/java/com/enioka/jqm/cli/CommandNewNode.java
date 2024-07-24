package com.enioka.jqm.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.configservices.DefaultConfigurationService;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;

@Parameters(commandNames = "New-Node", commandDescription = "Create a new node inside central configuration.")
class CommandNewNode extends CommandBase
{
    @Parameter(names = { "-n", "--node" }, description = "Name of the node to create.", required = true)
    private String nodeName;

    @Parameter(names = { "-p",
            "--port-web" }, description = "Port for web services of the new node. Default is a random free port.", required = false)
    private int port = 0;

    @Override
    public int doWork()
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            jqmlogger.info("Creating engine node " + nodeName);
            DefaultConfigurationService.updateConfiguration(cnx);
            DefaultConfigurationService.updateNodeConfiguration(nodeName, cnx, port);
            return 0;
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not create the engine", e);
            return 102;
        }
    }
}
