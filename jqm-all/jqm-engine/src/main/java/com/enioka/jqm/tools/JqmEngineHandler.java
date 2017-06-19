package com.enioka.jqm.tools;

import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Node;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * A set of callbacks for creating hooks on a JQM engine life cycle. Used by {@link JqmEngineFactory}.<br>
 *
 */
public interface JqmEngineHandler
{
    /**
     * Called by the internal poller on each loop.
     */
    void onConfigurationChanged(Node n);

    /**
     * During node startup, called just after configuration retrieval (before any engine component is actually created).
     * 
     * @param n
     *            the node in question, with fresh configuration.
     */
    void onNodeConfigurationRead(Node n);

    /**
     * Called even before the configuration is read. Mostly useful for dealing with static contexts.
     */
    void onNodeStarting(String nodeName);

    /**
     * When the node is actually fully stopped.
     */
    void onNodeStopped();

    /**
     * After all threads have been created and all pollers poll.
     */
    void onNodeStarted();

    /**
     * Called when a job instance is being prepared to run.
     * 
     * @param ji
     */
    void onJobInstancePreparing(JobInstance ji);

    /**
     * Called after job run, whatever the result, but before the result is stored in the database.
     * 
     * @param ji
     *            the ended job instance
     */
    void onJobInstanceDone(JobInstance ji);
}
