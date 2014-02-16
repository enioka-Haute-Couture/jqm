package com.enioka.jqm.tools;

public interface JqmEngineMBean
{
    /**
     * Stops the engine. It cannot be restarted aftewards.
     */
    void stop();

    /**
     * The total number of job instances that were run on this node since the last history purge.
     * 
     * @return
     */
    long getCumulativeJobInstancesCount();

    /**
     * @return
     */
    float getJobsFinishedPerSecondLastMinute();

    /**
     * The number of currently running job instances
     * 
     * @return
     */
    long getCurrentlyRunningJobCount();
}
