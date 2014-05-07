package com.enioka.jqm.tools;

/**
 * Monitoring interface for the Engine
 */
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

    /**
     * True if, for all pollers, the last time the poller looped was less than a polling period ago.
     */
    boolean isAllPollersPolling();

    /**
     * True if at least one queue is full
     */
    boolean isFull();

    /**
     * The number of seconds since engine start.
     */
    long getUptime();

    /**
     * The package version, in x.x.x form.
     */
    String getVersion();
}
