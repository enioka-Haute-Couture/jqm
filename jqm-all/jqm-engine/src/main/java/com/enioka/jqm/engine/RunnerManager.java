package com.enioka.jqm.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.runner.api.JobRunner;
import com.enioka.jqm.shared.exceptions.JqmRuntimeException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.loader.Loader;
import com.enioka.jqm.model.JobInstance;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages all the {@link JobRunner} instances inside an engine.
 */
class RunnerManager
{
    private Logger jqmlogger = LoggerFactory.getLogger(RunnerManager.class);

    private Map<Integer, JobRunner> runnerCache = new HashMap<>();

    private List<JobRunner> runners = new ArrayList<>(2);

    private ArrayList<ServiceReference> refList = new ArrayList<ServiceReference>();

    RunnerManager(DbConn cnx)
    {
        jqmlogger.info("Registering the runners");

        // Use the loader to get all the JobRunners available in the environment and add them to the list of runners.
        try
        {
            BundleContext context = org.osgi.framework.FrameworkUtil.getBundle(getClass()).getBundleContext();
            Loader<JobRunner> loader = new Loader<JobRunner>(context, JobRunner.class, null);
            loader.start();
            for (ServiceReference<?> ref : loader.references)
            {
                runners.add((JobRunner) context.getService(ref));
                refList.add(ref);
            }
        }
        catch (Exception e)
        {
            throw new JqmRuntimeException("Issue when loading the runners");
        }
    }

    /**
     * Retrieves the most adequate {@link JobRunner} for a given {@link JobInstance}. Throws {@link JqmRuntimeException} if none was found.
     *
     * @param ji
     * @return
     */
    JobRunner getRunner(JobInstance ji)
    {
        if (runnerCache.containsKey(ji.getJdId()))
        {
            return runnerCache.get(ji.getJdId());
        }

        for (JobRunner runner : runners)
        {
            if (runner.canRun(ji))
            {
                runnerCache.put(ji.getJdId(), runner);
                return runner;
            }
        }

        throw new JqmRuntimeException("there is no runner able to run job definition " + ji.getJD().getApplicationName());
    }

    void stop()
    {
        BundleContext context = org.osgi.framework.FrameworkUtil.getBundle(getClass()).getBundleContext();
        for (ServiceReference ref : refList)
        {
            context.ungetService(ref);
        }
    }
}
