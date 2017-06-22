package com.enioka.jqm.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.JqmInvalidRequestException;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;

/**
 * This is a dumbed down version of the JQM engine that, instead of checking jobs from a database, will run at once a specified job
 * instance.
 */
public class JqmSingleRunner
{
    private final static Logger jqmlogger = LoggerFactory.getLogger(JqmSingleRunner.class);

    private JqmSingleRunner()
    {
        // Static class
    }

    public static JobInstance run(int jobInstanceId)
    {
        jqmlogger.debug("Single runner was asked to start with ID " + jobInstanceId);
        DbConn cnx = Helpers.getNewDbSession();
        com.enioka.jqm.model.JobInstance jr = com.enioka.jqm.model.JobInstance.select_id(cnx, jobInstanceId);
        cnx.close();
        if (jr == null)
        {
            throw new IllegalArgumentException("There is no JobRequest by ID " + jobInstanceId);
        }
        return run(jr);
    }

    /**
     * Runs an existing JobInstance.
     * 
     * @param job
     * @param logFile
     *            the file to which output the run log. if null, only stdout will be used.
     * @return the result of the run
     */
    public static JobInstance run(com.enioka.jqm.model.JobInstance job)
    {
        if (job == null)
        {
            throw new IllegalArgumentException("Argument jr cannot be null");
        }
        jqmlogger.info("Starting single runner for payload " + job.getId());

        // Set thread name - used in audits
        Thread.currentThread().setName("JQM single runner;;" + job.getId());

        // JNDI first - the engine itself uses JNDI to fetch its connections!
        Helpers.registerJndiIfNeeded();

        // Get a copy of the instance, to be sure to get a non detached item.
        DbConn cnx = Helpers.getNewDbSession();
        job = com.enioka.jqm.model.JobInstance.select_id(cnx, job.getId());

        // Parameters
        final int poll = Integer.parseInt(GlobalParameter.getParameter(cnx, "internalPollingPeriodMs", "10000"));
        final int jobId = job.getId();

        // Security
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManagerPayload());
        }

        // Create run container
        ClassloaderManager clm = new ClassloaderManager();
        clm.setIsolationDefault(cnx);
        final Loader l = new Loader(job, (JqmEngine) null, (QueuePoller) null, clm);

        // Kill signal handler
        final Thread mainT = Thread.currentThread();
        Thread shutHook = new Thread()
        {
            @Override
            public void run()
            {
                Thread.currentThread().setName("JQM single runner;stophook;" + jobId);
                jqmlogger.info("Shutting down the single runner before its normal end (kill order)");

                // The stop order may come from SIGTERM or SIGINT - in which case, the payload is not aware it should stop.
                try
                {
                    JqmClientFactory.getClient().killJob(jobId);
                }
                catch (JqmInvalidRequestException e)
                {
                    // Ignore - the job has already finished.
                }

                // To speed up payload learning its coming demise
                mainT.interrupt();

                // Give one second for graceful payload stop
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // Nothing to do
                }

                // Shut down the process if needed
                if (!l.isDone)
                {
                    // Timeout! Violently halt the JVM.
                    jqmlogger.info("Job has not finished gracefully and will be stopped abruptly");
                    l.endOfRun(com.enioka.jqm.model.State.CRASHED);
                    Runtime.getRuntime().halt(0);
                }
                else
                {
                    jqmlogger.debug("Stop order was handled gracefully");
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutHook);

        // Kill order (from database) handler
        Thread stopper = new Thread()
        {
            @Override
            public void run()
            {
                Thread.currentThread().setName("JQM single runner;killerloop;" + jobId);
                DbConn cnx = null;

                while (!Thread.interrupted())
                {
                    cnx = Helpers.getNewDbSession();
                    com.enioka.jqm.model.JobInstance job = com.enioka.jqm.model.JobInstance.select_id(cnx, jobId);
                    cnx.close();

                    if (job != null && job.getInstruction().equals(com.enioka.jqm.model.Instruction.KILL))
                    {
                        jqmlogger.debug(
                                "Job " + jobId + " has received a kill order. It's JVM will be killed after a grace shutdown period");
                        System.exit(1); // Launch the exit hook.
                        break;
                    }

                    try
                    {
                        Thread.sleep(poll);
                    }
                    catch (InterruptedException e)
                    {
                        break;
                    }
                }
            }
        };
        stopper.start();

        // Go.
        l.run();

        // Free resources
        Runtime.getRuntime().removeShutdownHook(shutHook);
        cnx.close();
        stopper.interrupt();

        // Get result
        return JqmClientFactory.getClient().getJob(job.getId());
    }

    /**
     * <strong>Not part of any API - for JQM internal tests only</strong><br>
     * Sets the connection that will be used by the engine and its APIs.
     */
    public static void setConnection(Db db)
    {
        Helpers.setDb(db);
    }
}
