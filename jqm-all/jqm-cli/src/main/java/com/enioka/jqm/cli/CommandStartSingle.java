package com.enioka.jqm.cli;

import java.util.ServiceLoader;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import com.enioka.jqm.engine.api.lifecycle.JqmSingleRunnerOperations;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;

@Parameters(commandNames = "Start-Single", commandDescription = "Internal JQM use only. Starts an already ATTRIBUTED job instance synchronously.", hidden = true)
class CommandStartSingle extends CommandBase
{
    @Parameter(names = { "-i", "--id" }, description = "ID of the job instance to launch.", required = true)
    private Long id;

    @Override
    public int doWork()
    {
        var engine = ServiceLoaderHelper.getService(ServiceLoader.load(JqmSingleRunnerOperations.class));
        if (engine == null)
        {
            throw new JqmInitError("No jqm engine service instance available - check jqm-engine plugin is present");
        }

        JobInstance res = engine.runAtOnce(id);
        jqmlogger.info("{}", res.getState());
        return 0;
    }
}
