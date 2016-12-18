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
package com.enioka.jqm.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameters for querying {@link JobInstance}s. A null parameter (the default for most query parameters) is ignored in the query. 
 * To query a null String, specify "" (empty
 * String). To query a null Integer, specify -1. It is not possible to query for null Calendar values, since it is far more efficient to
 * query by status (the different Calendar fields are only null at certain statuses).<br>
 * See individual setters for the signification of query parameters.<br>
 * <br>
 * By default, i.e. by simply using <code>Query.create().run()</code>, the API retrieves the first 50 instances that have ended, ordered by launch ID (i.e. by time). 
 * See {@link Query#setQueryLiveInstances(boolean)} for details and how to retrieve living instances in addition to ended ones.<br><br>
 * 
 * Also please note that queries get more expensive with the result count, so it is <strong>strongly recommended to use pagination</strong> 
 * ({@link #setFirstRow(Integer)} and {@link #setPageSize(Integer)}).
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Query
{
    private Integer jobInstanceId, parentId;
    private List<String> applicationName = new ArrayList<String>();
    private String user, sessionId;
    private String jobDefKeyword1, jobDefKeyword2, jobDefKeyword3, jobDefModule, jobDefApplication;
    private String instanceKeyword1, instanceKeyword2, instanceKeyword3, instanceModule, instanceApplication;
    private String queueName, nodeName;
    private Integer queueId;
    private Calendar enqueuedBefore, enqueuedAfter, beganRunningBefore, beganRunningAfter, endedBefore, endedAfter;

    @XmlElementWrapper(name = "statuses")
    @XmlElement(name = "status", type = State.class)
    private List<State> status = new ArrayList<State>();

    private Integer firstRow, pageSize = 50;
    private Integer resultSize;

    @XmlElementWrapper(name = "instances")
    @XmlElement(name = "instance", type = JobInstance.class)
    private List<JobInstance> results;

    @XmlElementWrapper(name = "sortby")
    @XmlElement(name = "sortitem", type = SortSpec.class)
    private List<SortSpec> sorts = new ArrayList<Query.SortSpec>();

    private boolean queryLiveInstances = false, queryHistoryInstances = true;

    /**
     * The different fields that can be used in sorting.
     */
    public static enum Sort {
        ID("id"), APPLICATIONNAME("jd.applicationName"), QUEUENAME("queue.name"), STATUS("status", "state"), DATEENQUEUE("enqueueDate",
                "creationDate"), DATEATTRIBUTION("attributionDate"), DATEEXECUTION("executionDate"), DATEEND("endDate", null), USERNAME(
                "userName"), PARENTID("parentId");

        private String historyField, jiField;

        private Sort(String historyField, String jiField)
        {
            this.historyField = historyField;
            this.jiField = jiField;
        }

        private Sort(String commonField)
        {
            this.historyField = commonField;
            this.jiField = commonField;
        }

        String getHistoryField()
        {
            return this.historyField;
        }

        String getJiField()
        {
            return this.jiField;
        }
    }

    /**
     * The sort order
     */
    static enum SortOrder {
        ASCENDING, DESCENDING;
    }

    /**
     * Internal description of a sorting operation
     */
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    static class SortSpec
    {
        SortOrder order = SortOrder.ASCENDING;
        Sort col;

        // Bean convention
        @SuppressWarnings("unused")
        private SortSpec()
        {

        }

        SortSpec(SortOrder order, Sort column)
        {
            this.order = order;
            this.col = column;
        }
    }

    /**
     * Adds a new column a the end of the sorting clause.
     * 
     * @see #addSortDesc(Sort)
     */
    public Query addSortAsc(Sort column)
    {
        this.sorts.add(new SortSpec(SortOrder.ASCENDING, column));
        return this;
    }

    /**
     * Adds a new column a the end of the sorting clause.
     * 
     * @see #addSortAsc(Sort)
     */
    public Query addSortDesc(Sort column)
    {
        this.sorts.add(new SortSpec(SortOrder.DESCENDING, column));
        return this;
    }

    List<SortSpec> getSorts()
    {
        return this.sorts;
    }

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
        this.setApplicationName(applicationName);
        this.instanceKeyword1 = instanceKeyword1;
    }

    // //////////////////////////////////////////
    // Builder
    // //////////////////////////////////////////

    /**
     * The start of the fluent Query API. Creates a new Query object.
     */
    public static Query create()
    {
        return new Query();
    }

    /**
     * The end of the fluent Query API. It simply executes the query and returns the results.
     */
    public List<JobInstance> run()
    {
        return JqmClientFactory.getClient().getJobs(this);
    }

    // //////////////////////////////////////////
    // Results handling
    // //////////////////////////////////////////

    /**
     * This sets the maximum returned results count, for pagination purposes.<br>
     * It is <strong>highly recommended to use pagination</strong> when using the Query API, since queries are expensive. 
     * @param pageSize the maximal result count, or null for no limit (dangerous!)
     * @return the Query itself (fluent API - used to chain calls).
     * @see #setFirstRow(Integer) setFirstRow for the other pagination parameter.
     */
    public Query setPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
        return this;
    }
    
    /**
     * This sets the starting row returned by the query, for pagination purposes. Note that even if order is very important for paginated queries 
     * (to ensure that the pages stay the same between calls for new pages), a default sort is used if none is specified.<br>
     * It is <strong>highly recommended to use pagination</strong> when using the Query API, since queries are expensive. 
     * @param firstRow the first row to return. 0 is equivalent to null.
     * @return the Query itself (fluent API - used to chain calls).
     * @see #setPageSize(Integer) setPageSize for the other pagination parameter.
     */
    public Query setFirstRow(Integer firstRow)
    {
        this.firstRow = firstRow;
        return this;
    }

    /**
     * @return the available result count of the query. Available means that it does not take into account pagination. This is mostly used when pagination is used,
     * so as to be able to set a "total records count" or a "page 2 on 234" indicator. If pagination is not used, this is always equal to 
     * <code>{@link #getResults()}.size()</code>.
     */
    public Integer getResultSize()
    {
        if (results == null)
        {
            throw new IllegalStateException("Cannot retrieve the results of a query that was not run");
        }
        if (resultSize != null)
        {
            return resultSize;
        }
        else
        {
            return results.size();
        }
    }

    void setResultSize(Integer resultSize)
    {
        this.resultSize = resultSize;
    }

    public List<JobInstance> getResults()
    {
        if (results == null)
        {
            throw new IllegalStateException("Cannot retrieve the results of a query that was not run");
        }
        return results;
    }

    void setResults(List<JobInstance> results)
    {
        this.results = results;
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
    public Query setJobInstanceId(Integer jobInstanceId)
    {
        this.jobInstanceId = jobInstanceId;
        return this;
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
    public Query setParentId(Integer parentId)
    {
        this.parentId = parentId;
        return this;
    }

    List<String> getApplicationName()
    {
        return applicationName;
    }

    /**
     * The application name is the name of the job definition - the same name that is given in the Job Definition XML. This allows to query
     * all job instances for given job definitions. If the list contains multiple names, an OR query takes place.
     * 
     * @param applicationName
     */
    public Query setApplicationName(List<String> applicationName)
    {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * The application name is the name of the job definition - the same name that is given in the Job Definition XML. This allows to query
     * all job instances for a single given job definition. If other names were given previously (e.g. with
     * {@link #setApplicationName(List)} , they are removed by this method.
     * 
     * @param applicationName
     * @return
     */
    public Query setApplicationName(String applicationName)
    {
        this.applicationName.clear();
        this.applicationName.add(applicationName);
        return this;
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
    public Query setUser(String user)
    {
        this.user = user;
        return this;
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
    public Query setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
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
    public Query setJobDefKeyword1(String jobDefKeyword1)
    {
        this.jobDefKeyword1 = jobDefKeyword1;
        return this;
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
    public Query setJobDefKeyword2(String jobDefKeyword2)
    {
        this.jobDefKeyword2 = jobDefKeyword2;
        return this;
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
    public Query setJobDefKeyword3(String jobDefKeyword3)
    {
        this.jobDefKeyword3 = jobDefKeyword3;
        return this;
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
    public Query setJobDefModule(String jobDefModule)
    {
        this.jobDefModule = jobDefModule;
        return this;
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
    public Query setJobDefApplication(String jobDefApplication)
    {
        this.jobDefApplication = jobDefApplication;
        return this;
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
    public Query setInstanceKeyword1(String instanceKeyword1)
    {
        this.instanceKeyword1 = instanceKeyword1;
        return this;
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
    public Query setInstanceKeyword2(String instanceKeyword2)
    {
        this.instanceKeyword2 = instanceKeyword2;
        return this;
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
    public Query setInstanceKeyword3(String instanceKeyword3)
    {
        this.instanceKeyword3 = instanceKeyword3;
        return this;
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
    public Query setInstanceModule(String instanceModule)
    {
        this.instanceModule = instanceModule;
        return this;
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
    public Query setInstanceApplication(String instanceApplication)
    {
        this.instanceApplication = instanceApplication;
        return this;
    }

    boolean isQueryLiveInstances()
    {
        return queryLiveInstances;
    }

    /**
     * By default, querying only occurs on ended (OK or not) job instances. If this parameter is set to true, it will also include living
     * (waiting, running, ...) job instances.<br>
     * If you also query on live instances at the same time, this will reset pagination as it is impossible to use pagination with both.<br>
     * <br>
     * Setting this to true has a noticeable performance impact and should be used as little as possible (or should be used when
     * {@link #setQueryHistoryInstances(boolean)} is false, which is not the default)
     */
    public Query setQueryLiveInstances(boolean queryLiveInstances)
    {
        this.queryLiveInstances = queryLiveInstances;
        if (this.queryHistoryInstances)
        {
            this.pageSize = null;
            this.firstRow = null;
        }
        return this;
    }

    boolean isQueryHistoryInstances()
    {
        return queryHistoryInstances;
    }

    /**
     * By default, querying only occurs on ended (OK or not) job instances. If this parameter is set to false, however, the History will not
     * be used. This is usually used in conjunction with {@link #setQueryLiveInstances(boolean)}<br>
     * <br>
     */
    public Query setQueryHistoryInstances(boolean queryHistoryInstances)
    {
        this.queryHistoryInstances = queryHistoryInstances;
        return this;
    }

    Calendar getEnqueuedBefore()
    {
        return enqueuedBefore;
    }

    /**
     * The time at which the execution request was given to {@link JqmClient#enqueue(JobRequest)}. This is an <= comparison.
     * 
     * @param enqueuedBefore
     */
    public Query setEnqueuedBefore(Calendar enqueuedBefore)
    {
        this.enqueuedBefore = enqueuedBefore;
        return this;
    }

    Calendar getEnqueuedAfter()
    {
        return enqueuedAfter;
    }

    /**
     * The time at which the execution request was given to {@link JqmClient#enqueue(JobRequest)}. This is an >= comparison.
     * 
     * @param enqueuedAfter
     */
    public Query setEnqueuedAfter(Calendar enqueuedAfter)
    {
        this.enqueuedAfter = enqueuedAfter;
        return this;
    }

    Calendar getBeganRunningBefore()
    {
        return beganRunningBefore;
    }

    /**
     * The time at which the execution really began (the request arrived at the top of the queue and was run by an engine). This is an <=
     * comparison.
     * 
     * @param beganRunningBefore
     */
    public Query setBeganRunningBefore(Calendar beganRunningBefore)
    {
        this.beganRunningBefore = beganRunningBefore;
        return this;
    }

    Calendar getBeganRunningAfter()
    {
        return beganRunningAfter;
    }

    /**
     * The time at which the execution really began (the request arrived at the top of the queue and was run by an engine). This is an >=
     * comparison.
     * 
     * @param beganRunningAfter
     */
    public Query setBeganRunningAfter(Calendar beganRunningAfter)
    {
        this.beganRunningAfter = beganRunningAfter;
        return this;
    }

    Calendar getEndedBefore()
    {
        return endedBefore;
    }

    /**
     * The time at which the execution ended, resulting in an ENDED or CRASHED status. This is an <= comparison.
     * 
     * @param endedBefore
     */
    public Query setEndedBefore(Calendar endedBefore)
    {
        this.endedBefore = endedBefore;
        return this;
    }

    Calendar getEndedAfter()
    {
        return endedAfter;
    }

    /**
     * The time at which the execution ended, resulting in an ENDED or CRASHED status. This is an <= comparison.
     * 
     * @param endedAfter
     */
    public Query setEndedAfter(Calendar endedAfter)
    {
        this.endedAfter = endedAfter;
        return this;
    }

    List<State> getStatus()
    {
        return status;
    }

    /**
     * Filter by status. See {@link State} for the different possible values and their meaning. If multiple values are added, a logical OR
     * will take place.
     * 
     * @param status
     */
    public Query addStatusFilter(State status)
    {
        this.status.add(status);
        return this;
    }

    Integer getFirstRow()
    {
        return firstRow;
    }

    Integer getPageSize()
    {
        return pageSize;
    }

    String getQueueName()
    {
        return queueName;
    }

    /**
     * For querying jobs on a given queue. The list of queues can be retrieved through {@link JqmClient#getQueues()}.
     */
    public Query setQueueName(String queueName)
    {
        this.queueName = queueName;
        return this;
    }

    Integer getQueueId()
    {
        return queueId;
    }

    /**
     * For querying jobs on a given queue. The list of queues can be retrieved through {@link JqmClient#getQueues()}.<br>
     * Ignored if setQueueName is used.
     */
    public Query setQueueId(Integer queueId)
    {
        this.queueId = queueId;
        return this;
    }

    /**
     * For querying jobs that have run or are running on a specific JQM node.
     */
    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    String getNodeName()
    {
        return nodeName;
    }
}
