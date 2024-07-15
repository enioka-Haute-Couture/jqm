package com.enioka.jqm.runner.java;

import java.io.PrintStream;
import java.util.ServiceLoader;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.MetaInfServices;

import com.enioka.jqm.api.JavaJobRunner;
import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.cl.ExtClassLoader;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.runner.api.JobInstanceTracker;
import com.enioka.jqm.runner.api.JobRunner;
import com.enioka.jqm.runner.api.JobRunnerCallback;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;

/**
 * Being in Java, JQM can have a special relationship with jobs coded in Java. This runner provides the capacity to run classes with
 * advanced CL handling inside the engine process. It itself has multiple plugins allowing it to load different types of classes.
 */
@MetaInfServices(JobRunner.class)
public class JavaRunner implements JobRunner
{
    private ClassloaderManager classloaderManager;
    private boolean oneLogPerLaunch = false;
    private PrintStream originalStdOut, originalStdErr;

    @Override
    public void close()
    {
        if (classloaderManager != null)
        {
            classloaderManager.stop();
        }

        SecurityManagerPayloadLoader.unregisterIfPossible();

        if (oneLogPerLaunch)
        {
            System.setOut(originalStdOut);
            System.setErr(originalStdErr);
        }
    }

    public JavaRunner()
    {
        // Get all runners from registry
        var javaJobRunners = ServiceLoaderHelper.getServices(ServiceLoader.load(JavaJobRunner.class));

        // Init CL manager
        classloaderManager = new ClassloaderManager(javaJobRunners);

        // Security manager
        SecurityManagerPayloadLoader.registerIfPossible();

        // Log multicasting
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            originalStdOut = System.out;
            originalStdErr = System.err;

            String gp1 = GlobalParameter.getParameter(cnx, "logFilePerLaunch", "true");

            if ("true".equals(gp1) || "both".equals(gp1))
            {
                oneLogPerLaunch = true;

                String rootPath = ExtClassLoader.getRootDir();
                String logDirectory = FilenameUtils.concat(rootPath, "logs");

                // Override stdout so that we are able to capture it inside log files.
                MultiplexPrintStream s = new MultiplexPrintStream(System.out, logDirectory, "both".equals(gp1));
                System.setOut(s);

                // Same with stderr
                s = new MultiplexPrintStream(System.err, logDirectory, "both".equals(gp1));
                System.setErr(s);

                // Redirect JQM's own logging to the multiplexing stdout.
                // That way all logging specific to a JobInstance goes to the JobInstance log file and not the main log file. //
                // ((ConsoleAppender) root.getAppender("consoleAppender")).setTarget("System.out");
            }
        }
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
