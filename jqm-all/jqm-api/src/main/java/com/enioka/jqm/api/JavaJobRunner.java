package com.enioka.jqm.api;

import java.util.Map;

/**
 * Note: this is not a public API yet. It is not stable enough yet.<br>
 * <br>
 * For plugin writers only.<br>
 * This interface is implemented by the different job runners specialized in running Java payloads, i.e. the agents which actually launch
 * the job instances. The java job runners must be placed in the plugins directory of the engine.<br>
 * <br>
 * Implementors should always specify a no-args constructor for runners.
 */
public interface JavaJobRunner
{
    /**
     * Called when a new job instance reaches the running state, to determine which runner should be used to actually launch it.<br>
     * This method should have no side effect and be thread safe.
     *
     * @param toRun
     *            the class designated as the one to run inside the job definition.
     * @return true of this runner can run it. Returning true is not a guarantee that this specific runner will be selected for the actual
     *         launch, as others may be able to launch it too.
     */
    public boolean canRun(Class<? extends Object> toRun);

    /**
     * Called when a job instance should run for real. This method should only return when the job instance run is over.<br>
     * RuntimeException thrown by the payload are expected to bubble up. Exceptions due to the plugin itself should be reported as
     * {@link JobRunnerException}.<br>
     * When this method is called, the context class loader is already the payload class loader, so a runner has access to the full static
     * context of the payload which it should run. However, the class of the <code>JavaJobRunner</code> itself is loaded through the engine
     * class loader. So a lot of care is needed when doing operations on the payload class, to avoid class loader mismatches.
     *
     * @param toRun
     *            the class designated as the job to run inside the job definition.
     * @param metaParameters
     *            a set of parameters given by then engine. These are to be documented.
     * @param jobParameters
     *            the actual parameters the job instance should use.
     * @param handlerProxy
     *            a pre-configured proxy containing the JobManager API - it should be injected if needed inside the job instance itself
     */
    public void run(Class<? extends Object> toRun, Map<String, String> metaParameters, Map<String, String> jobParameters,
            Object handlerProxy);
}
