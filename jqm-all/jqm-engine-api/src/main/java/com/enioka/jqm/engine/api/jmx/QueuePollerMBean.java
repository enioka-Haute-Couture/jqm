/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enioka.jqm.engine.api.jmx;

import javax.management.MXBean;

/**
 * Monitoring interface for queue pollers
 */
@MXBean
public interface QueuePollerMBean
{
    /**
     * The number of currently running job instances. Thread safe.
     *
     * @return int
     */
    Integer getCurrentActiveThreadCount();

    /**
     * Prevents the engine to take any more jobs on that queue. Once all running jobs end, the poller stops.
     */
    void stop();

    /**
     * Number of seconds between two database checks for new job instance to run.
     *
     * @return int
     */
    Integer getPollingIntervalMilliseconds();

    /**
     * Max number of simultaneously running job instances on this queue on this engine
     *
     * @return int
     */
    Integer getMaxConcurrentJobInstanceCount();

    /**
     * The total number of job instances that were run on this node/queue since the last history purge.
     *
     * @return long
     */
    long getCumulativeJobInstancesCount();

    /**
     * The number of job instances that ended in the last minute divided by 60. A better method is to call
     * {@link #getCumulativeJobInstancesCount()} and compute deltas between calls.
     *
     * @return float
     */
    float getJobsFinishedPerSecondLastMinute();

    /**
     * The number of currently running job instances
     *
     * @return long
     */
    long getCurrentlyRunningJobCount();

    /**
     * True if the last time the poller looped was less than a period ago.
     *
     * @return boolean
     */
    boolean isActuallyPolling();

    /**
     * True if running count equals max job number
     *
     * @return boolean
     */
    boolean isFull();

    /**
     * The count of running jobs that have run for more than their maxTimeRunning time.
     *
     * @return int
     */
    int getLateJobs();
}
