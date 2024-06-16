package com.enioka.jqm.cli;

import com.beust.jcommander.Parameters;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.repository.VersionRepository;

@Parameters(commandNames = "Get-Version", commandDescription = "Fetch the version of the command line tool.")
class CommandGetEngineVersion extends CommandBase
{
    @Override
    public int doWork()
    {
        jqmlogger.info("Engine version: " + VersionRepository.getMavenVersion());
        return 0;
    }
}
