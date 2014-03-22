package com.enioka.jqm.api;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameters for querying JobInstances. A null parameter (the default) is ignored in the query. To query a null String, specify "" (empty
 * String). To query a null Integer, specify -1. <br>
 * See individual setters for the signification of query parameters.<br>
 * <br>
 * By default, querying only retrieves instances that have ended. See {@link Query#setQueryLiveInstances(boolean)} for details and how to
 * retrieve living instances.
 * 
 */
@XmlRootElement
public final class Query
{
    private Integer jobInstanceId;
    private Integer parentId;
    private String applicationName;
    private String user;
    private String sessionId;
    private String jobDefKeyword1, jobDefKeyword2, jobDefKeyword3, jobDefModule, jobDefApplication;
    private String instanceKeyword1, instanceKeyword2, instanceKeyword3, instanceModule, instanceApplication;
    private Queue queue;

    private boolean queryLiveInstances = false;

    // //////////////////////////////////////////
    // Accelerator constructors
    // //////////////////////////////////////////

    public Query()
    {

    }

    public Query(String userName)
    {
        this.user = userName;
    }

    public Query(String applicationName, String instanceKeyword1)
    {
        this.applicationName = applicationName;
        this.instanceKeyword1 = instanceKeyword1;
    }

    // //////////////////////////////////////////
    // Stupid get/set
    // //////////////////////////////////////////

    Integer getJobInstanceId()
    {
        return jobInstanceId;
    }

    /**
     * To query a specific job instance. This ID is returned, for example, by the {@link JqmClient#enqueue(JobRequest)} method. <br>
     * It is pretty useless to give any other query parameters if you know the ID. Also note that there is a shortcut method named
     * {@link JqmClient#getJob(int)} to make a query by ID.
     * 
     * @param jobInstanceId
     *            the job instance ID
     */
    public void setJobInstanceId(Integer jobInstanceId)
    {
        this.jobInstanceId = jobInstanceId;
    }

    Integer getParentId()
    {
        return parentId;
    }

    /**
     * Some job instances are launched by other job instances (linked jobs which launch one another). This allows to query all job instances
     * launched by a specific job instance.
     * 
     * @param parentId
     *            the ID of the parent job instance.
     */
    public void setParentId(Integer parentId)
    {
        this.parentId = parentId;
    }

    String getApplicationName()
    {
        return applicationName;
    }

    /**
     * The application name is the name of the job definition - the same name that is given in the Job definition XML. This allows to query
     * all job instances for a given job definition.
     * 
     * @param applicationName
     */
    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    String getUser()
    {
        return user;
    }

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param user
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    String getSessionId()
    {
        return sessionId;
    }

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param sessionId
     */
    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    String getJobDefKeyword1()
    {
        return jobDefKeyword1;
    }

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param jobDefKeyword1
     */
    public void setJobDefKeyword1(String jobDefKeyword1)
    {
        this.jobDefKeyword1 = jobDefKeyword1;
    }

    String getJobDefKeyword2()
    {
        return jobDefKeyword2;
    }

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param jobDefKeyword2
     */
    public void setJobDefKeyword2(String jobDefKeyword2)
    {
        this.jobDefKeyword2 = jobDefKeyword2;
    }

    String getJobDefKeyword3()
    {
        return jobDefKeyword3;
    }

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param jobDefKeyword3
     */
    public void setJobDefKeyword3(String jobDefKeyword3)
    {
        this.jobDefKeyword3 = jobDefKeyword3;
    }

    String getJobDefModule()
    {
        return jobDefModule;
    }

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param jobDefModule
     */
    public void setJobDefModule(String jobDefModule)
    {
        this.jobDefModule = jobDefModule;
    }

    String getJobDefApplication()
    {
        return jobDefApplication;
    }

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying. <br>
     * <strong>This has nothing to so with applicationName, which is the name of the Job Definition !</strong>
     * 
     * @param jobDefApplication
     */
    public void setJobDefApplication(String jobDefApplication)
    {
        this.jobDefApplication = jobDefApplication;
    }

    String getInstanceKeyword1()
    {
        return instanceKeyword1;
    }

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param instanceKeyword1
     */
    public void setInstanceKeyword1(String instanceKeyword1)
    {
        this.instanceKeyword1 = instanceKeyword1;
    }

    String getInstanceKeyword2()
    {
        return instanceKeyword2;
    }

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param instanceKeyword2
     */
    public void setInstanceKeyword2(String instanceKeyword2)
    {
        this.instanceKeyword2 = instanceKeyword2;
    }

    String getInstanceKeyword3()
    {
        return instanceKeyword3;
    }

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param instanceKeyword3
     */
    public void setInstanceKeyword3(String instanceKeyword3)
    {
        this.instanceKeyword3 = instanceKeyword3;
    }

    String getInstanceModule()
    {
        return instanceModule;
    }

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * 
     * @param instanceModule
     */
    public void setInstanceModule(String instanceModule)
    {
        this.instanceModule = instanceModule;
    }

    String getInstanceApplication()
    {
        return instanceApplication;
    }

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying. <br>
     * <strong>This has nothing to so with applicationName, which is the name of the Job Definition !</strong>
     * 
     * @param instanceApplication
     */
    public void setInstanceApplication(String instanceApplication)
    {
        this.instanceApplication = instanceApplication;
    }

    Queue getQueue()
    {
        return queue;
    }

    /**
     * For querying jobs on a given queue. The list of queues can be retrieved through {@link JqmClient#getQueues()}.
     * 
     * @param queue
     */
    public void setQueue(Queue queue)
    {
        this.queue = queue;
    }

    boolean isQueryLiveInstances()
    {
        return queryLiveInstances;
    }

    /**
     * By default, querying only occurs on ended (OK or not) job instances. If this parameter is set to true, it will also include living
     * (waiting, running, ...) job instances.<br>
     * <br>
     * Setting this to true has a noticeable performance impact and should be used as little as possible.
     * 
     * @param queryLiveInstances
     */
    public void setQueryLiveInstances(boolean queryLiveInstances)
    {
        this.queryLiveInstances = queryLiveInstances;
    }

}
