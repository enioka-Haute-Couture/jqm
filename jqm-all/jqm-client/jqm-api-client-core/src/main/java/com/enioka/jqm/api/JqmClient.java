package com.enioka.jqm.api;

import java.io.InputStream;
import java.util.List;

/**
 * The one and only interface for all JQM client operations. Implementations are NOT expected to be thread safe.
 * 
 */
public interface JqmClient
{
    // /////////////////////////////////////////////////////////////////////
    // Enqueue functions
    // /////////////////////////////////////////////////////////////////////

    /**
     * Will create a new job instance inside an execution queue. All parameters (JQM parameters such as queue name, etc) as well as job
     * parameters) are given inside the job request argument <br>
     * <br>
     * Do not use this complex version for simple requests - there is a dedicated overload.
     * 
     * @param jobRequest
     *            a property bag for all the parameters that can be specified at enqueue time.
     * @return the ID of the job instance. Use this ID to track the job instance later on (it is a very common parameter inside the JQM
     *         client API)
     * 
     * @throws JqmInvalidRequestException
     *             when input data is invalid.
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public int enqueue(JobRequest jobRequest);

    /**
     * Will create a new job instance inside an execution queue. All parameters (JQM parameters such as queue name, etc) as well as job
     * parameters) are given inside the job request argument <br>
     * 
     * @param applicationName
     *            name of the job to launch
     * @param userName
     *            the user at the origin of the enqueue request (can be null or empty)
     * @return the ID of the job instance. Use this ID to track the job instance later on (it is a very common parameter inside the JQM
     *         client API)
     * @throws JqmInvalidRequestException
     *             when input data is invalid.
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public int enqueue(String applicationName, String userName);

    /**
     * Create a new job instance from another job instance that has successfully ended. This does not change the copied instance. Everything
     * is copied: parameters, queue, etc.
     * 
     * @param jobIdToCopy
     *            the id of the job instance to copy
     * @return the id of the new instance
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job does not exist).
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public int enqueueFromHistory(int jobIdToCopy);

    // /////////////////////////////////////////////////////////////////////
    // Job destruction
    // /////////////////////////////////////////////////////////////////////

    /**
     * Cancel a job, leaving a trace inside execution history. This is the normal way of canceling jobs - having traces of cancelled
     * requests is often important to draw statistics and educate users.<br>
     * This only works if the job instance is not already running: one cannot cancel a request that was already accepted. (throws exception
     * in that case)
     * 
     * @param jobId
     *            the id of the job to cancel
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job already run, job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public void cancelJob(int jobId);

    /**
     * Remove an enqueued job from the queue, leaving no trace of it. This is an exceptional event - usually cancelJob would be used <br>
     * This only works if the job instance is not already running: one cannot cancel a request that was already accepted. (throws exception
     * in that case)
     * 
     * @param idJob
     * @see {@link #cancelJob(int)} for the more conventional way of removing job requests.
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job already done, job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public void deleteJob(int jobId);

    /**
     * Kill a running job. Kill is not immediate, and is only possible when a job payload calls some JQM APIs. If none are called, the job
     * cannot be killed.
     * 
     * @param jobId
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job already done, job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public void killJob(int jobId);

    // /////////////////////////////////////////////////////////////////////
    // Job Pause/restart
    // /////////////////////////////////////////////////////////////////////

    /**
     * Prevent a queued job request (not already accepted by an engine) from running. It can be restored afterwards.
     * 
     * @param jobId
     *            id of the job instance to pause
     * @see {@link #resumeJob(int)} for resuming the paused request.
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job already run, job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public void pauseQueuedJob(int jobId);

    /**
     * Resume a paused request.
     * 
     * @param jobId
     *            id of the job instance to resume
     * @see {@link #pauseQueuedJob(int)} for the reverse operation.
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job already run, job does not exist...)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public void resumeJob(int jobId);

    /**
     * Will restart a crashed job. This will remove all trace of the failed execution.
     * 
     * @param jobId
     *            id of the job instance that has failed.
     * @return the ID of the restarted job instance. Use this ID to track the job instance later on (it is a very common parameter inside
     *         the JQM client API). This is not the same ID as the one that has failed.
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job not crashed, job does not exist...)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public int restartCrashedJob(int jobId);

    // /////////////////////////////////////////////////////////////////////
    // Misc.
    // /////////////////////////////////////////////////////////////////////

    /**
     * Move a job instance from a queue to another queue
     * 
     * @param jobId
     *            the job instance to modify
     * @param queueId
     *            id of the queue object to which the job should be affected
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job does not exist, queue does not exist...)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public void setJobQueue(int jobId, int queueId);

    /**
     * Move a job instance from a queue to another queue
     * 
     * @param jobId
     *            the job instance to modify
     * @param queue
     *            the queue object to which the job should be affected
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job does not exist, queue does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public void setJobQueue(int jobId, Queue queue);

    /**
     * Change the position of a waiting job instance inside a queue.
     * 
     * @param jobId
     *            id of the job instance to modify
     * @param newPosition
     *            its new position
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job already run, job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public void setJobQueuePosition(int jobId, int newPosition);

    // /////////////////////////////////////////////////////////////////////
    // Job queries
    // /////////////////////////////////////////////////////////////////////

    /**
     * Retrieve a job instance. The returned object will contain all relevant data on that job instance such as its status. Call this
     * function again to refresh the data. <br>
     * This function queries both the active queues and the history. If not job is found, an exception is raised.
     * 
     * @param jobId
     * @return the characteristics of the job instance.
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public JobInstance getJob(int jobId);

    /**
     * Administrative method. List all currently running or waiting or finished job instances.
     * 
     * @return the characteristics of the job instances.
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<JobInstance> getJobs();

    /**
     * Administrative method. List all currently running or waiting job instances.
     * 
     * @return the characteristics of the job instances.
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<JobInstance> getActiveJobs();

    /**
     * List all currently running or waiting job instances for a given "user" (see userName parameter at enqueue time)
     * 
     * @return the characteristics of the job instances.
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<JobInstance> getUserActiveJobs(String userName);

    /**
     * Generic query method. See {@link Query} for arguments.
     * 
     * @param query
     *            the query parameters.
     * @return the selected JobInstances
     * @throws JqmInvalidRequestException
     *             when query is null or inconsistent (e.g. trying to use pagination on a queue query)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<JobInstance> getJobs(Query query);

    // /////////////////////////////////////////////////////////////////////
    // Helpers to quickly access some job instance properties
    // /////////////////////////////////////////////////////////////////////

    /**
     * Get all messages that were created by a given job instance (running or done). Note that in addition to eventual messages created by
     * the payload itself, the JQM engine creates some messages so there should always be some for a completed job instance.
     * 
     * @param jobId
     * @return all messages as strings
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<String> getJobMessages(int jobId);

    /**
     * Get the progress indication that may have been given by a job instance (running or done).
     * 
     * @param jobId
     * @return the progress, or 0 if none was given.
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public int getJobProgress(int jobId);

    // /////////////////////////////////////////////////////////////////////
    // Deliverables retrieval
    // /////////////////////////////////////////////////////////////////////

    /**
     * Return all metadata concerning the (potential) files created by the job instance.
     * 
     * @param jobId
     * @return
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<Deliverable> getJobDeliverables(int jobId);

    /**
     * Return all files created by a job instance if any. The stream is not open: opening and closing it is the caller's responsibility.<br>
     * <strong>The underlying temporary files are deleted at stream closure</strong>.
     * 
     * @param jobId
     * @return a list of streams
     * @throws JqmInvalidRequestException
     *             when input data is invalid (job does not exist)
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<InputStream> getJobDeliverablesContent(int jobId);

    /**
     * Return one file created by a job instance. The stream is not open: opening and closing it is the caller's responsibility.<br>
     * <strong>The underlying temporary files are deleted at stream closure</strong>.
     * 
     * @param file
     *            the file to retrieve (usually obtained through {@link #getJobDeliverables(int)})
     * @return a stream
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public InputStream getDeliverableContent(Deliverable file);

    // /////////////////////////////////////////////////////////////////////
    // Parameters retrieval
    // /////////////////////////////////////////////////////////////////////

    /**
     * List all available queues with their characteristics. Useful mostly for admin operations and changing a job instance from one queue
     * to another.
     * 
     * @return a list of queues
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<Queue> getQueues();

    /**
     * Lists all the available {@link JobDef} objects, i.e. the different payloads that can be launched by JQM.
     * 
     * @return a list of JobDef
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<JobDef> getJobDefinitions();

    /**
     * Lists all the available {@link JobDef} objects for a given application, i.e. the different payloads that can be launched by JQM. The
     * "application" is the optional tag that can be given inside the <code> &ltapplication&gt</code> tag of the JobDef XML file.<br>
     * If application is null, this method is equivalent to {@link #getJobDefinitions()}.
     * 
     * @return a list of JobDef
     * @throws JqmClientException
     *             when an internal API implementation occurs. Usually linked to a configuration issue.
     */
    public List<JobDef> getJobDefinitions(String application);

    // /////////////////////////////////////////////////////////////////////
    // Technical
    // /////////////////////////////////////////////////////////////////////

    /**
     * Free resources. Client is unusable after calling this method. What is freed depends on the implementation, it may be nothing at all.
     * This method should never throw any exception.
     */
    public void dispose();
}
