package com.enioka.jqm.engine.api.lifecycle;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Lifecycle verbs of the engine API.
 */
public interface JqmEngineOperations
{
    /**
     * Orders the engine to stop and waits for its actual end.
     */
    public void stop();

    public boolean areAllPollersPolling();

    public void resume();

    public void pause();

    public void start(String nodeName, JqmEngineHandler h);

    public void join();
}
