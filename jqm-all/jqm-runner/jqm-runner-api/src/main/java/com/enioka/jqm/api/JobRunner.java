package com.enioka.jqm.api;

import com.enioka.jqm.model.JobInstance;

/**
 * Note: this is not a public API and is planned to stay private in the forseeable future.<br>
 * <br>
 * For plugin writers only.<br>
 * This interface is implemented by the different job runners, i.e. the agents which actually launch the job instances. The job runners must
 * be placed in the lib directory of the engine.<br>
 * <br>
 * Implementors should always specify a no-args constructor for runners.
 */
public interface JobRunner
{
    /**
     * Called when a new job instance reaches the running state, to determine which runner should be used to actually launch it.<br>
     * This method should have no side effect and be thread safe.<br>
     * Decisions should be taken only on the basis of the provided arguments and never from external elements (system counters...)
     * 
     * @param toRun
     *                  the resolved element which should be run.
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
}
