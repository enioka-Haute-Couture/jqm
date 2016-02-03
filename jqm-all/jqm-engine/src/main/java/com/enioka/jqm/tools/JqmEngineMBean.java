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
package com.enioka.jqm.tools;

import com.enioka.jqm.jpamodel.GlobalParameter;

/**
 * Monitoring interface for the Engine
 */
public interface JqmEngineMBean
{
    /**
     * Stops the engine. It cannot be restarted afterwards.
     */
    void stop();

    /**
     * Pauses the engine. It will not take any new job instances after this, but already running instance will continue as before.
     */
    void pause();

    /**
     * Un-pauses the engine. See {@link #pause()}.
     */
    void resume();

    /**
     * Forces a full refresh of base configuration (HTTP port, log level, ...). Usually configuration is updated automatically every
     * <code>internalPollingPeriodMs</code> (a {@link GlobalParameter}) milliseconds. This triggers the same refresh method.
     */
    void refreshConfiguration();

    /**
     * The total number of job instances that were run on this node since the last node restart.
     */
    long getCumulativeJobInstancesCount();

    /**
     * The number of currently running job instances
     */
    long getCurrentlyRunningJobCount();

    /**
     * True if, for all pollers, the last time the poller looped was less than a polling period ago.
     */
    boolean isAllPollersPolling();

    /**
     * True if at least one queue is full.
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
    
    /**
     * The count, for all pollers, of running jobs that have run for more than their maxTimeRunning time.
     */
    int getLateJobs();
}
