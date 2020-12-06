package com.enioka.jqm.runner.java;

import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.runner.api.JobInstanceTracker;
import com.enioka.jqm.runner.api.JobRunner;
import com.enioka.jqm.runner.api.JobRunnerCallback;

import org.osgi.service.component.annotations.Component;

/**
 * Being in Java, JQM can have a special relationship with jobs coded in Java. This runner provides the capacity to run classes with
 * advanced CL handling inside the engine process. It itself has multiple plugins allowing it to load different types of classes.
 */
@Component(property = { "Plugin-Type=JobRunner", "Runner-Type=java" })
public class JavaRunner implements JobRunner
{
    private ClassloaderManager classloaderManager;

    static
    {
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManagerPayload());
        }
    }

    public JavaRunner()
    {
        classloaderManager = new ClassloaderManager(DbManager.getDb().getConn());
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
