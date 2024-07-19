package com.enioka.jqm.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.runner.api.JobRunner;
import com.enioka.jqm.shared.exceptions.JqmRuntimeException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages all the {@link JobRunner} instances inside an engine.
 */
class RunnerManager
{
    private Logger jqmlogger = LoggerFactory.getLogger(RunnerManager.class);

    private Map<Long, JobRunner> runnerCache = new HashMap<>();

    private ConcurrentLinkedQueue<JobRunner> runners = new ConcurrentLinkedQueue<>();

    private ArrayList<ServiceReference<JobRunner>> refList = new ArrayList<ServiceReference<JobRunner>>();

    private ServiceTracker<JobRunner, JobRunner> jobRunnerServiceTracker;

    RunnerManager(DbConn cnx)
    {
        jqmlogger.info("Registering the runners");

        // Use the loader to get all the JobRunners available in the environment and add them to the list of runners.
        BundleContext context = org.osgi.framework.FrameworkUtil.getBundle(getClass()).getBundleContext();
        jobRunnerServiceTracker = new ServiceTracker<>(context, JobRunner.class, new ServiceTrackerCustomizer<JobRunner, JobRunner>()
        {
            @Override
            public JobRunner addingService(ServiceReference<JobRunner> reference)
            {
                refList.add(reference);
                JobRunner runner = context.getService(reference);
                runners.add(runner);
                return runner;
            }

            @Override
            public void modifiedService(ServiceReference<JobRunner> reference, JobRunner service)
            {
                // Nothing to do
            }

            @Override
            public void removedService(ServiceReference<JobRunner> reference, JobRunner service)
            {
                refList.remove(reference);
                runners.remove(service);
                context.ungetService(reference);
            }
        });
        jobRunnerServiceTracker.open();
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
        for (ServiceReference<JobRunner> ref : refList)
        {
            context.ungetService(ref);
        }
        jobRunnerServiceTracker.close();
    }
}
