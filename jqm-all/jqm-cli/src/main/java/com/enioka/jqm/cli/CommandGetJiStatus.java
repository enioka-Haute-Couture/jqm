package com.enioka.jqm.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.jqm.client.api.JqmClientFactory;
import com.enioka.jqm.client.shared.IDbClientFactory;

@Parameters(commandNames = "Get-JiStatus", commandDescription = "Fetch the status of a running or ended job instance.")
class CommandGetJiStatus extends CommandBase
{
    @Parameter(names = { "-i", "--id" }, description = "ID of the job instance to query.", required = true)
    private Integer id;

    @Override
    int doWork()
    {
        jqmlogger.info("Status is: " + JqmClientFactory.getClient(IDbClientFactory.class).getJob(id).getState());
        return 0;
    }
}
