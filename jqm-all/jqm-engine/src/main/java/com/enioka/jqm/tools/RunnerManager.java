package com.enioka.jqm.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.api.JobRunner;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;

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

    RunnerManager(DbConn cnx)
    {
        jqmlogger.info("Registering java runner");
        runners.add(new JavaRunner(cnx));
        runners.add(new ShellRunner(cnx));
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
