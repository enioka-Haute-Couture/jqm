package com.enioka.jqm.api;

import java.util.Map;

/**
 * Note: this is not a public API yet. It is not stable enough yet.<br>
 * <br>
 * For plugin writers only.<br>
 * The interface implemented by the event handlers hooked on the "a job instance has started" event. <br>
 * <br>
 * This runs inside the same context as the payload: the class is loaded by the payload class loader, and all methods are called by threads
 * which context class loader is the payload class loader. No access to the engine class loader is possible.
 */
public interface JobInstanceStartedHandler
{
    /**
     * All filter actions should go here. This runs just before the real job instance launch. This should return fast - no long operation
     * should be done here, even asynchronously. (it is forbidden to create threads or any type of background job in this method).
     * 
     * @param toRun
     *            the class which was loaded in order to launch the job. Note that the filter should not instantiate it, it is given as a
     *            reference only.
     * @param handler
     *            the engine API entry point. Gives access to the job instance details.
     * @param the
     *            parameters given to configure the filter in the engine configuration. May be empty.
     */
    public void run(Class<? extends Object> toRun, JobManager handler, Map<String, String> handlerParameters);
}
