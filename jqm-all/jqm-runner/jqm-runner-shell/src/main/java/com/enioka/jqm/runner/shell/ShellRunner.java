package com.enioka.jqm.runner.shell;

import org.kohsuke.MetaInfServices;

import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.runner.api.JobInstanceTracker;
import com.enioka.jqm.runner.api.JobRunner;
import com.enioka.jqm.runner.api.JobRunnerCallback;

/**
 * This runner provides the ability to launch CLI processes.
 */
@MetaInfServices(JobRunner.class)
public class ShellRunner implements JobRunner
{
    public ShellRunner()
    {

    }

    @Override
    public void close() throws Exception
    {
        // Nothing to do
    }

    @Override
    public boolean canRun(JobInstance toRun)
    {
        PathType type = toRun.getJD().getPathType();
        return type == PathType.DEFAULTSHELLCOMMAND || type == PathType.POWERSHELLCOMMAND || type == PathType.DIRECTEXECUTABLE;
    }

    @Override
    public JobInstanceTracker getTracker(JobInstance toRun, JobManager engineApi, JobRunnerCallback cb)
    {
        return new ShellJobInstanceTracker(toRun, cb, engineApi);
    }
}
