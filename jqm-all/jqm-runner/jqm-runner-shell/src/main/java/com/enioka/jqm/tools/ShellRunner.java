package com.enioka.jqm.tools;

import com.enioka.jqm.api.JobInstanceTracker;
import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.api.JobRunner;
import com.enioka.jqm.api.JobRunnerCallback;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.JobInstance;

/**
 * This runner provides the ability to launch CLI processes.
 */
class ShellRunner implements JobRunner
{
    ShellRunner(DbConn cnx)
    {

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
        return new ShellJobInstanceTracker(toRun);
    }
}
