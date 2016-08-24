package com.enioka.jqm.tools;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.JqmInvalidRequestException;

/**
 * This is a dumbed down version of the JQM engine that, instead of checking jobs from a database, will run at once a specified job
 * instance.
 */
public class JqmSingleRunner
{
    private final static Logger jqmlogger = Logger.getLogger(JqmSingleRunner.class);

    private JqmSingleRunner()
    {
        // Static class
    }

    public static JobInstance run(int jobInstanceId)
    {
        jqmlogger.debug("Single runner was asked to start with ID " + jobInstanceId);
        EntityManager em = Helpers.getNewEm();
        com.enioka.jqm.jpamodel.JobInstance jr = em.find(com.enioka.jqm.jpamodel.JobInstance.class, jobInstanceId);
        em.close();
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
    public static JobInstance run(com.enioka.jqm.jpamodel.JobInstance job)
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
        EntityManager em = Helpers.getNewEm();
        job = em.find(com.enioka.jqm.jpamodel.JobInstance.class, job.getId());

        // Parameters
        final int poll = Integer.parseInt(Helpers.getParameter("internalPollingPeriodMs", "10000", em));
        final int jobId = job.getId();

        // Security
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManagerPayload());
        }

        // Create run container
        final Loader l = new Loader(job, (JqmEngine) null, (QueuePoller) null, new ClassloaderManager());

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
                    l.endOfRun(com.enioka.jqm.jpamodel.State.KILLED);
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
                EntityManager em2 = null;

                while (!Thread.interrupted())
                {
                    em2 = Helpers.getNewEm();
                    com.enioka.jqm.jpamodel.JobInstance job = em2.find(com.enioka.jqm.jpamodel.JobInstance.class, jobId);
                    em2.close();

                    if (job != null && job.getState().equals(com.enioka.jqm.jpamodel.State.KILLED))
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
        em.close();
        stopper.interrupt();

        // Get result
        return JqmClientFactory.getClient().getJob(job.getId());
    }

    /**
     * <strong>Not part of any API - for JQM internal tests only</strong><br>
     * Sets the connection that will be used by the engine and its APIs.
     */
    public static void setConnection(EntityManagerFactory emf)
    {
        Helpers.setEmf(emf);
    }
}
