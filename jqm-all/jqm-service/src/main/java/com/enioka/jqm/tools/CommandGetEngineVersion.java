package com.enioka.jqm.tools;

import com.beust.jcommander.Parameters;

@Parameters(commandNames = "Get-Version", commandDescription = "Fetch the version of the command line tool.")
class CommandGetEngineVersion extends CommandBase
{
    @Override
    int doWork()
    {
        jqmlogger.info("Engine version: " + Helpers.getMavenVersion());
        return 0;
    }
}
