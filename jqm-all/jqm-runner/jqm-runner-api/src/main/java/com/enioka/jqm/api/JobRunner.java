package com.enioka.jqm.api;

import com.enioka.jqm.model.JobInstance;

/**
 * <strong>This is not a public API and it will stay private in the foreseeable future.</strong><br>
 * <br>
 * Job runners are responsible for the actual launch of the job instances. The job runners must be directly referenced by the engine, as
 * this is not a plugin architecture, just an internal extension point.<br>
 * <br>
 * Implementors should always specify a no-args constructor for runners.
 */
public interface JobRunner
{
    /**
     * Called when a new job instance reaches the {@link com.enioka.jqm.model.State#ATTRIBUTED} state, to determine which runner should be
     * used to actually launch it.<br>
     * This method should have no side effect and be thread safe.<br>
     * Decisions should be taken only on the basis of the provided arguments and never from external elements (system counters...). Decision
     * results are cached by the engine.
     *
     * @param toRun
     *            the resolved element which should be run.
     *
     * @return true of this runner can run it. Returning true is not a guarantee that this specific runner will be selected for the actual
     *         launch, as others may be able to launch it too.
     */
    public boolean canRun(JobInstance toRun);

    /**
     * The job runner should create a new tracker - but NOT launch it.
     *
     * @param toRun
     * @return
     */
    public JobInstanceTracker getTracker(JobInstance toRun, JobManager engineApi, JobRunnerCallback cb);

    /**
     * Called when the engine is stopping, allowing for cleanup.
     */
    public void stop();
}
