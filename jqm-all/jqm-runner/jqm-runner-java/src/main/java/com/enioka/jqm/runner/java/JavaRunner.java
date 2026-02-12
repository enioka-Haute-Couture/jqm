package com.enioka.jqm.runner.java;

import java.io.PrintStream;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger jqmlogger = LoggerFactory.getLogger(JavaRunner.class);

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

        // Check that all referenced runners exist
        validateRunnersExistence(javaJobRunners);

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
                // That way all logging specific to a JobInstance goes to the JobInstance
                // log file and not the main log file.
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

    /**
     * Validates that all runners referenced in classloader configurations exist. Logs an error for any missing runner references.
     *
     * @param javaJobRunners
     *            list of available JavaJobRunner implementations
     */
    private void validateRunnersExistence(List<JavaJobRunner> javaJobRunners)
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            var classloaders = com.enioka.jqm.model.Cl.select(cnx, "cl_select_all");

            Set<String> availableRunnerNames = javaJobRunners.stream().map(runner -> runner.getClass().getCanonicalName())
                    .collect(Collectors.toSet());

            for (var cl : classloaders)
            {
                String allowedRunners = cl.getAllowedRunners();

                if (allowedRunners != null && !allowedRunners.trim().isEmpty())
                {
                    String[] runnerClassNames = allowedRunners.split(",");

                    for (String runnerClassName : runnerClassNames)
                    {
                        String runnerName = runnerClassName.trim();

                        if (!runnerName.isEmpty() && !availableRunnerNames.contains(runnerName))
                        {
                            jqmlogger.error(
                                    "Class loader '{}' references a runner '{}' which does not exist. "
                                            + "To prevent a failure at runtime install the runner or update the configuration.",
                                    cl.getName(), runnerName);
                        }
                    }
                }
            }
        }
    }
}
