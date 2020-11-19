package com.enioka.jqm.runner.java;

import com.enioka.jqm.api.JobInstanceTracker;
import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.api.JobRunner;
import com.enioka.jqm.api.JobRunnerCallback;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.JobInstance;

/**
 * Being in Java, JQM can have a special relationship with jobs coded in Java. This runner provides the capacity to run classes with
 * advanced CL handling inside the engine process. It itself has multiple plugins allowing it to load different types of classes.
 */
public class JavaRunner implements JobRunner
{
    private ClassloaderManager classloaderManager;

    public JavaRunner(DbConn cnx)
    {
        classloaderManager = new ClassloaderManager(cnx);
    }

    @Override
    public boolean canRun(JobInstance toRun)
    {
        PathType type = toRun.getJD().getPathType();
        return type == PathType.FS || type == PathType.MAVEN || type == PathType.MEMORY;
    }

    @Override
    public JobInstanceTracker getTracker(JobInstance toRun, JobManager engineApi, JobRunnerCallback cb)
    {
        return new JavaJobInstanceTracker(toRun, cb, classloaderManager, engineApi);
    }
}
