package com.enioka.jqm.client.shared;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 *
 * Used for callback factorization between JobRequest and JqmClient.
 */
public interface JqmClientEnqueueCallback
{
    /**
     * Will create a new job instance inside an execution queue. All parameters (JQM parameters such as queue name, etc) as well as job
     * parameters) are given inside the job request argument <br>
     * <br>
     *
     * @param runRequest
     *            a Job execution request.
     * @return the ID of the job instance. Use this ID to track the job instance later on (it is a very common parameter inside the JQM
     *         client API)
     */
    public Long enqueue(JobRequestBaseImpl runRequest);
}
