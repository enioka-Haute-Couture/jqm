package com.enioka.jqm.engine.api.lifecycle;

import com.enioka.jqm.model.JobInstance;

public interface JqmSingleRunnerOperations
{
    /**
     * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
     * Start immediately the given JobRequest. It will not update its status inside the database. Synchronous method.
     *
     * @param jobInstanceId
     * @return
     */
    public JobInstance runAtOnce(long jobInstanceId);
}
