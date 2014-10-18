package com.enioka.jqm.tools;

import java.io.IOException;

import javax.persistence.EntityManager;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;

/**
 * This is a dumbed down version of the JQM engine that, instead of checking jobs from a database, will run at once a specified job
 * instance.
 */
public class JqmSingleRunner
{
    private static LibraryCache cache = new LibraryCache();

    private JqmSingleRunner()
    {
        // Static class
    }

    public static JobInstance run(int jobInstanceId, String logFile)
    {
        EntityManager em = Helpers.getNewEm();
        com.enioka.jqm.jpamodel.JobInstance jr = em.find(com.enioka.jqm.jpamodel.JobInstance.class, jobInstanceId);
        em.close();
        if (jr == null)
        {
            throw new IllegalArgumentException("There is no JobRequest by ID " + jobInstanceId);
        }
        return run(jr, logFile);
    }

    /**
     * Runs an existing JobInstance.
     * 
     * @param job
     * @param logFile
     *            the file to which output the run log. if null, only stdout will be used.
     * @return the result of the run
     */
    public static JobInstance run(com.enioka.jqm.jpamodel.JobInstance job, String logFile)
    {
        if (job == null)
        {
            throw new IllegalArgumentException("Argument jr cannot be null");
        }

        // Get a copy of the instance, to be sure to get a non detached item.
        EntityManager em = Helpers.getNewEm();
        job = em.find(com.enioka.jqm.jpamodel.JobInstance.class, job.getId());

        // Set thread name - used in audits
        Thread.currentThread().setName("JQM single runner;" + job.getId() + ";");

        // JNDI first - the engine itself uses JNDI to fetch its connections!
        Helpers.registerJndiIfNeeded();

        // Logs & log level
        PatternLayout layout = new PatternLayout("%d{dd/MM HH:mm:ss.SSS}|%-5p|%-40.40t|%-17.17c{1}|%x%m%n");
        if (logFile != null)
        {
            // In this case, redirect everything (not only JQM-related) to the given file. Don't restore at the end - one shot JVM use case.
            Logger.getRootLogger().setLevel(Level.toLevel(job.getNode().getRootLogLevel()));
            Logger.getRootLogger().removeAllAppenders();
            Logger.getRootLogger().addAppender(new ConsoleAppender(layout));
            try
            {
                Logger.getRootLogger().addAppender(new FileAppender(layout, logFile));
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("Log file " + logFile + " cannot be created", e);
            }
        }

        // Security
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManagerPayload());
        }

        // Go.
        Loader l = new Loader(job, cache, null);
        l.run();

        // Free resources
        em.close();

        // Get result
        return JqmClientFactory.getClient().getJob(job.getId());
    }
}
