package com.enioka.jqm.api;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.State;

/**
 * An object created by a {@link JobRunner} to track a running instance. The instance is supposed to be given through a constructor called
 * by {@link JobRunner#getTracker(JobInstance, JobManager, JobRunnerCallback)}.
 */
public interface JobInstanceTracker
{
    /**
     * The runner can load whatever runner-specific configuration needed here. Called just before {@link #run()}, while the JI state is
     * still {@link State#ATTRIBUTED}.<br>
     * Must be thread-safe.
     * 
     * @param cnx
     *                a ready to use connection to the main database. Should not be stored as it closed by the engine soon after calling
     *                this method.
     */
    public void initialize(DbConn cnx);

    /**
     * Called when a job instance should run for real. This method should only return when the job instance run is over.<br>
     * RuntimeException thrown by the payload are expected to bubble up. Exceptions due to the plugin itself should be reported as
     * {@link JobRunnerException}.<br>
     * Called within a dedicated thread. - this method should NOT create any thread (the payload itself, outside JQM's responsibility,
     * may)<br>
     * When this method is called, the context class loader is the engine class loader. A loader is part of the engine.<br>
     */
    public State run();

    /**
     * Called after {@link #run(JobInstance, JobRunnerCallback)} has completed. Cleanup work should go here. Not called finalize because
     * reserved word.
     */
    public void wrap();
}