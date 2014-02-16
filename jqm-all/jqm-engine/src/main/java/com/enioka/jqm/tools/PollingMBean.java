package com.enioka.jqm.tools;

public interface PollingMBean
{
    /**
     * The number of currently running job instances. Thread safe.
     * 
     * @return
     */
    Integer getCurrentActiveThreadCount();

    /**
     * Prevents the engine to take any more jobs on that queue. Once all running jobs end, the poller stops.
     */
    void stop();

    /**
     * Number of seconds between two database checks for new job instance to run.
     * 
     * @return
     */
    Integer getPollingIntervalMilliseconds();

    /**
     * Max number of simultaneously running job instances on this queue on this engine
     * 
     * @return
     */
    Integer getMaxConcurrentJobInstanceCount();

    /**
     * The total number of job instances that were run on this node/queue since the last history purge.
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
