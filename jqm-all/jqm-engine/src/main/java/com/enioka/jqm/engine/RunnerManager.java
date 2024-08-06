package com.enioka.jqm.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.runner.api.JobRunner;
import com.enioka.jqm.shared.exceptions.JqmRuntimeException;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;

/**
 * This class manages all the {@link JobRunner} instances inside an engine.
 */
class RunnerManager implements AutoCloseable
{
    private Logger jqmlogger = LoggerFactory.getLogger(RunnerManager.class);

    private Map<Long, JobRunner> runnerCache = new HashMap<>();

    private ConcurrentLinkedQueue<JobRunner> runners = new ConcurrentLinkedQueue<>();

    RunnerManager(DbConn cnx)
    {
        jqmlogger.info("Registering the runners");

        // Use the loader to get all the JobRunners available in the environment and add them to the list of runners.
        runners = new ConcurrentLinkedQueue<>(ServiceLoaderHelper.getServices(ServiceLoader.load(JobRunner.class)));
    }

    @Override
    public void close() throws Exception
    {
        for (JobRunner runner : runners)
        {
            runner.close();
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
}
