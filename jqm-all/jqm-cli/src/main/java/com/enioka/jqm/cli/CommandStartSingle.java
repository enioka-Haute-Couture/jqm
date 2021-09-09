package com.enioka.jqm.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import com.enioka.jqm.engine.api.lifecycle.JqmSingleRunnerOperations;
import com.enioka.jqm.model.JobInstance;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

// TODO: remove OSGi boilerplate, put it in cluster-node.
@Parameters(commandNames = "Start-Single", commandDescription = "Internal JQM use only. Starts an already ATTRIBUTED job instance synchronously.", hidden = true)
class CommandStartSingle extends CommandBase
{
    @Parameter(names = { "-i", "--id" }, description = "ID of the job instance to launch.", required = true)
    private int id;

    @Override
    int doWork()
    {
        BundleContext bundleContext = FrameworkUtil.getBundle(CommandStartSingle.class).getBundleContext();
        if (bundleContext == null)
        {
            throw new JqmInitError("Not in an OSGi context, cannot start a single runner");
        }
        ServiceReference<JqmSingleRunnerOperations> jqmEngineSR = bundleContext.getServiceReference(JqmSingleRunnerOperations.class);
        if (jqmEngineSR == null)
        {
            throw new JqmInitError("No jqm engine service instance available - check jqm-engine bundle is started");
        }
        JqmSingleRunnerOperations engine = bundleContext.getServiceObjects(jqmEngineSR).getService();

        JobInstance res = engine.runAtOnce(id);
        jqmlogger.info("{}", res.getState());
        return 0;
    }
}
