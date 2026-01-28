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
package com.enioka.jqm.client.shared;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.Query;
import com.enioka.jqm.client.api.State;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 *
 * Base abstract implementation of {@link Query}. Deriving types should add fluent construction APIs (which need a client implementation not
 * available in the API itself).
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryBaseImpl implements Query
{
    /** The jobInstance id to filter on. **/
    protected Long jobInstanceId;
    /** The parentId to filter on. **/
    protected Long parentId;
    /** The application names to filter on. **/
    protected List<String> applicationName = new ArrayList<>();
    /** The user to filter on. **/
    protected String user;
    /** The session ID to filter on. **/
    protected String sessionId;
    /** The jobDefKeyword1 to filter on. **/
    protected String jobDefKeyword1;
    /** The jobDefKeyword2 to filter on. **/
    protected String jobDefKeyword2;
    /** The jobDefKeyword3 to filter on. **/
    protected String jobDefKeyword3;
    /** The jobDefModule to filter on. **/
    protected String jobDefModule;
    /** The jobDefApplication to filter on. **/
    protected String jobDefApplication;
    /** The instanceKeyword1 to filter on. **/
    protected String instanceKeyword1;
    /**The instanceKeyword2 to filter on. **/
    protected String instanceKeyword2;
    /** The instanceKeyword3 to filter on. **/
    protected String instanceKeyword3;
    /** The instanceModule to filter on. **/
    protected String instanceModule;
    /** the instaceApplication to filter on. **/
    protected String instanceApplication;
    /** The queue name to filter on. **/
    protected String queueName;
    /** The node name to filter on. **/
    protected String nodeName;
    /** The queue id to filter on. **/
    protected Long queueId;
    /** The enqueuedBefore date to filter on. **/
    protected Calendar enqueuedBefore;
    /** The enqueuedAfter date to filter on. **/
    protected Calendar enqueuedAfter;
    /** The beganRunningBefore date to filter on. **/
    protected Calendar beganRunningBefore;
    /** The beganRunningAfter date to filter on. **/
    protected Calendar beganRunningAfter;
    /** The endedBefore date to filter on. **/
    protected Calendar endedBefore;
    /** The endedAfter date to filter on. **/
    protected Calendar endedAfter;

    /** The status to filter on. **/
    @XmlElementWrapper(name = "statuses")
    @XmlElement(name = "status", type = State.class)
    protected List<State> status = new ArrayList<>();

    /** The firstRow to consider. **/
    protected Integer firstRow;
    /** The pageSize of the result. **/
    protected Integer pageSize = 50;

    /** Sorting specifications. **/
    @XmlElementWrapper(name = "sortby")
    @XmlElement(name = "sortitem", type = SortSpec.class)
    protected List<SortSpec> sorts = new ArrayList<>();

    /** Whether to query live instances or not. **/
    protected boolean queryLiveInstances = false;

    /** Whether to query history instances or not. **/
    protected boolean queryHistoryInstances = true;

    /** The result size. **/
    protected Integer resultSize;

    /** The results of the query. **/
    @XmlElementWrapper(name = "instances")
    @XmlElement(name = "instance", type = JobInstance.class)
    protected List<JobInstance> results;

    /** The client used to submit the query. **/
    @XmlTransient
    protected JqmClientQuerySubmitCallback parentClient;

    // //////////////////////////////////////////
    // Construction
    // //////////////////////////////////////////

    // JAX-RS javabean convention
    /** Default constructor for JAX-RS. **/
    public QueryBaseImpl() {}

    /**
     * Constructor.
     * @param client the client to use for the query
     */
    public QueryBaseImpl(JqmClientQuerySubmitCallback client)
    {
        this.parentClient = client;
    }

    /**
     * Internal method. Not a public API. Used to change the client used by a query. The parameter type used is not a public interface on
     * purpose.
     *
     * @param parentClient the new client to use
     */
    public void setParentClient(JqmClientQuerySubmitCallback parentClient)
    {
        this.parentClient = parentClient;
    }

    // //////////////////////////////////////////
    // Execution
    // //////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobInstance> invoke()
    {
        return this.parentClient.getJobs(this);
    }

    // //////////////////////////////////////////
    // Sort
    // //////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public Query addSortAsc(Sort column)
    {
        this.sorts.add(new SortSpec(SortOrder.ASCENDING, column));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query addSortDesc(Sort column)
    {
        this.sorts.add(new SortSpec(SortOrder.DESCENDING, column));
        return this;
    }

    /**
     * Get the sorting specifications.
     * @return the sorting specifications
     */
    public List<SortSpec> getSorts()
    {
        return this.sorts;
    }

    // //////////////////////////////////////////
    // Results handling
    // //////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setFirstRow(Integer firstRow)
    {
        this.firstRow = firstRow;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * Set the result size manually.
     * @see #getResultSize()
     * @param resultSize the resultSize to set
     */
    public void setResultSize(Integer resultSize)
    {
        this.resultSize = resultSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobInstance> getResults()
    {
        if (results == null)
        {
            throw new IllegalStateException("Cannot retrieve the results of a query that was not run");
        }
        return results;
    }

    /**
     * Set the result of a query manually.
     * @see #getResults()
     * @param results the results to set
     */
    public void setResults(List<JobInstance> results)
    {
        this.results = results;
    }

    // //////////////////////////////////////////
    // Stupid get/set
    // //////////////////////////////////////////

    /**
     * Get the job instance ID to filter on.
     * @see #setJobInstanceId(Long)
     * @return the job instance ID
     */
    public Long getJobInstanceId()
    {
        return jobInstanceId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setJobInstanceId(Long jobInstanceId)
    {
        this.jobInstanceId = jobInstanceId;
        return this;
    }

    /**
     * Get the parent ID to filter on.
     * @see #setParentId(Long)
     * @return the parent ID
     */
    public Long getParentId()
    {
        return parentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setParentId(Long parentId)
    {
        this.parentId = parentId;
        return this;
    }

    /**
     * Get the application names to filter on.
     * @see #setApplicationName(List)
     * @return the application names
     */
    public List<String> getApplicationName()
    {
        return applicationName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setApplicationName(List<String> applicationName)
    {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setApplicationName(String applicationName)
    {
        this.applicationName.clear();
        this.applicationName.add(applicationName);
        return this;
    }

    /**
     * Get the user to filter on.
     * @see #setUser(String)
     * @return the user
     */
    public String getUser()
    {
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setUser(String user)
    {
        this.user = user;
        return this;
    }

    /**
     * Get the session ID to filter on.
     * @see #setSessionId(String)
     * @return the session ID
     */
    public String getSessionId()
    {
        return sessionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Get the jobDefKeyword1 to filter on.
     * @see #setJobDefKeyword1(String)
     * @return the jobDefKeyword1
     */
    public String getJobDefKeyword1()
    {
        return jobDefKeyword1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setJobDefKeyword1(String jobDefKeyword1)
    {
        this.jobDefKeyword1 = jobDefKeyword1;
        return this;
    }

    /**
     * Get the jobDefKeyword2 to filter on.
     * @see #setJobDefKeyword2(String)
     * @return the jobDefKeyword2
     */
    public String getJobDefKeyword2()
    {
        return jobDefKeyword2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setJobDefKeyword2(String jobDefKeyword2)
    {
        this.jobDefKeyword2 = jobDefKeyword2;
        return this;
    }

    /**
     * Get the jobDefKeyword3 to filter on.
     * @see #setJobDefKeyword3(String)
     * @return the jobDefKeyword3
     */
    public String getJobDefKeyword3()
    {
        return jobDefKeyword3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setJobDefKeyword3(String jobDefKeyword3)
    {
        this.jobDefKeyword3 = jobDefKeyword3;
        return this;
    }

    /**
     * Get the jobDefModule to filter on.
     * @see #setJobDefModule(String)
     * @return the jobDefModule
     */
    public String getJobDefModule()
    {
        return jobDefModule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setJobDefModule(String jobDefModule)
    {
        this.jobDefModule = jobDefModule;
        return this;
    }

    /**
     * Get the jobDefApplication to filter on.
     * @see #setJobDefApplication(String)
     * @return the jobDefApplication
     */
    public String getJobDefApplication()
    {
        return jobDefApplication;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setJobDefApplication(String jobDefApplication)
    {
        this.jobDefApplication = jobDefApplication;
        return this;
    }

    /**
     * Get the instanceKeyword1 to filter on.
     * @see #setInstanceKeyword1(String)
     * @return the instanceKeyword1
     */
    public String getInstanceKeyword1()
    {
        return instanceKeyword1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setInstanceKeyword1(String instanceKeyword1)
    {
        this.instanceKeyword1 = instanceKeyword1;
        return this;
    }

    /**
     * Get the instanceKeyword2 to filter on.
     * @see #setInstanceKeyword2(String)
     * @return the instanceKeyword2
     */
    public String getInstanceKeyword2()
    {
        return instanceKeyword2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setInstanceKeyword2(String instanceKeyword2)
    {
        this.instanceKeyword2 = instanceKeyword2;
        return this;
    }

    /**
     * Get the instanceKeyword3 to filter on.
     * @see #setInstanceKeyword3(String)
     * @return the instanceKeyword3
     */
    public String getInstanceKeyword3()
    {
        return instanceKeyword3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setInstanceKeyword3(String instanceKeyword3)
    {
        this.instanceKeyword3 = instanceKeyword3;
        return this;
    }

    /**
     * Get the instanceModule to filter on.
     * @see #setInstanceModule(String)
     * @return the instanceModule
     */
    public String getInstanceModule()
    {
        return instanceModule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setInstanceModule(String instanceModule)
    {
        this.instanceModule = instanceModule;
        return this;
    }

    /**
     * Get the instanceApplication to filter on.
     * @see #setInstanceApplication(String)
     * @return the instanceApplication
     */
    public String getInstanceApplication()
    {
        return instanceApplication;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setInstanceApplication(String instanceApplication)
    {
        this.instanceApplication = instanceApplication;
        return this;
    }

    /**
     * Get whether to query live instances or not.
     * @see #setQueryLiveInstances(boolean)
     * @return whether to query live instances
     */
    public boolean isQueryLiveInstances()
    {
        return queryLiveInstances;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * Get whether to query history instances or not.
     * @see #setQueryHistoryInstances(boolean)
     * @return whether to query history instances
     */
    public boolean isQueryHistoryInstances()
    {
        return queryHistoryInstances;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setQueryHistoryInstances(boolean queryHistoryInstances)
    {
        this.queryHistoryInstances = queryHistoryInstances;
        return this;
    }

    /**
     * Get the enqueue date before which to filter.
     * @see #setEnqueuedBefore(Calendar)
     * @return the date
     */
    public Calendar getEnqueuedBefore()
    {
        return enqueuedBefore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setEnqueuedBefore(Calendar enqueuedBefore)
    {
        this.enqueuedBefore = enqueuedBefore;
        return this;
    }

    /**
     * Get the enqueue date after which to filter.
     * @see #setEnqueuedAfter(Calendar)
     * @return the date
     */
    public Calendar getEnqueuedAfter()
    {
        return enqueuedAfter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setEnqueuedAfter(Calendar enqueuedAfter)
    {
        this.enqueuedAfter = enqueuedAfter;
        return this;
    }

    /**
     * Get the running date before which to filter.
     * @see #setBeganRunningBefore(Calendar)
     * @return the date
     */
    public Calendar getBeganRunningBefore()
    {
        return beganRunningBefore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setBeganRunningBefore(Calendar beganRunningBefore)
    {
        this.beganRunningBefore = beganRunningBefore;
        return this;
    }

    /**
     * Get the running date after which to filter.
     * @see #setBeganRunningAfter(Calendar)
     * @return the date
     */
    public Calendar getBeganRunningAfter()
    {
        return beganRunningAfter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setBeganRunningAfter(Calendar beganRunningAfter)
    {
        this.beganRunningAfter = beganRunningAfter;
        return this;
    }

    /**
     * Get the ended date before which to filter.
     * @see #setEndedBefore(Calendar)
     * @return the date
     */
    public Calendar getEndedBefore()
    {
        return endedBefore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setEndedBefore(Calendar endedBefore)
    {
        this.endedBefore = endedBefore;
        return this;
    }

    /**
     * Get the ended date after which to filter.
     * @see #setEndedAfter(Calendar)
     * @return the date
     */
    public Calendar getEndedAfter()
    {
        return endedAfter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setEndedAfter(Calendar endedAfter)
    {
        this.endedAfter = endedAfter;
        return this;
    }

    /**
     * Get the status to filter on.
     * @see #addStatusFilter(State)
     * @return the status
     */
    public List<State> getStatus()
    {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query addStatusFilter(State status)
    {
        this.status.add(status);
        return this;
    }

    /**
     * Get the first row to consider.
     * @see #setFirstRow(Integer)
     * @return the first row
     */
    public Integer getFirstRow()
    {
        return firstRow;
    }

    /**
     * Get the page size.
     * @see #setPageSize(Integer)
     * @return the page size
     */
    public Integer getPageSize()
    {
        return pageSize;
    }

    /**
     * Get the queue name to filter on.
     * @see #setQueueName(String)
     * @return the queue name
     */
    public String getQueueName()
    {
        return queueName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setQueueName(String queueName)
    {
        this.queueName = queueName;
        return this;
    }

    /**
     * Get the queue ID to filter on.
     * @see #setQueueId(Long)
     * @return the queue ID
     */
    public Long getQueueId()
    {
        return queueId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setQueueId(Long queueId)
    {
        this.queueId = queueId;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
        return this;
    }

    /**
     * Get the node name to filter on.
     * @see #setNodeName(String)
     * @return the node name
     */
    public String getNodeName()
    {
        return nodeName;
    }
}
