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
package com.enioka.jqm.client.api;

import java.util.Calendar;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.enioka.jqm.client.shared.QueryBaseImpl;

/**
 * Parameters for querying {@link JobInstance}s. A null parameter (the default for most QueryInterface parameters) is ignored in the query.
 * To query a null String, specify "" (empty String). To QueryInterface a null Integer, specify -1. It is not possible to QueryInterface for
 * null Calendar values, since it is far more efficient to QueryInterface by status (the different Calendar fields are only null at certain
 * statuses).<br>
 * See individual setters for the signification of QueryInterface parameters.<br>
 * <br>
 * By default, i.e. by simply using <code>JqmClientFactory.getClient().newQuery().invoke()</code>, the API retrieves the first 50 instances
 * that have ended, ordered by launch ID (i.e. by time). See {@link QueryBaseImpl#setQueryLiveInstances(boolean)} for details and how to
 * retrieve living instances in addition to ended ones.<br>
 * <br>
 *
 * Also please note that queries get more expensive with the result count, so it is <strong>strongly recommended to use pagination</strong>
 * ({@link #setFirstRow(Integer)} and {@link #setPageSize(Integer)}).
 *
 */
@XmlTransient
public interface Query
{
    /**
     * The different fields that can be used in sorting.
     */
    public static enum Sort {
        /**
         * Sort by instance ID
         */
        ID("id"),
        /**
         * Sort by application name.
         */
        APPLICATIONNAME("JD_KEY"),
        /**
         * Sort by queue name.
         */
        QUEUENAME("QUEUE_NAME"),
        /**
         * Sort by Status
         */
        STATUS("STATUS"),
        /**
         * Sort by enqueue date
         */
        DATEENQUEUE("DATE_ENQUEUE"),
        /**
         * Sort by distribution date
         */

        DATEATTRIBUTION("DATE_ATTRIBUTION"),
        /**
         * Sort by execution date
         */
        DATEEXECUTION("DATE_START"),
        /**
         * Sort by end date
         */
        DATEEND("DATE_END", null),
        /**
         * Sort by username
         */
        USERNAME("USERNAME"),
        /**
         * Sort by parent ID
         */
        PARENTID("PARENT");

        private String historyField, jiField;

        /**
         * Sort by a history field and a job instance field
         * @param historyField the history field to sort by
         * @param jiField the job instance field to sort by
         */
        private Sort(String historyField, String jiField)
        {
            this.historyField = historyField;
            this.jiField = jiField;
        }

        /**
         * Sort by a common field
         * @param commonField the common field to sort by
         */
        private Sort(String commonField)
        {
            this.historyField = commonField;
            this.jiField = commonField;
        }

        /**
         * Get the history field
         * @return the history field
         */
        public String getHistoryField()
        {
            return this.historyField;
        }

        /**
         * Get the ji field
         * @return the ji field
         */
        public String getJiField()
        {
            return this.jiField;
        }
    }

    /**
     * The sort order
     */
    public static enum SortOrder {
        /**
         * Sort ascending.
         */
        ASCENDING,
        /**
         * Sort descending.
         */
        DESCENDING;
    }

    /**
     * Internal description of a sorting operation
     */
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SortSpec
    {
        /**
         * The sort order
         */
        public SortOrder order = SortOrder.ASCENDING;
        /**
         * The column to sort by
         */
        public Sort col;


        // Bean convention

        /**
         * Default constructor
         */
        @SuppressWarnings({"unused"})
        private SortSpec() {}

        /**
         * Constructor with sort order and column
         * @param order the sort order
         * @param column the column to sort by
         */
        public SortSpec(SortOrder order, Sort column)
        {
            this.order = order;
            this.col = column;
        }
    }

    /**
     * Adds a new column a the end of the sorting clause.
     * @param column the column to add, sorted ascending.
     * @return this Query
     * @see #addSortDesc(Sort)
     */
    public Query addSortAsc(Sort column);

    /**
     * Adds a new column a the end of the sorting clause.
     * @param column the column to add, sorted descending.
     * @return this Query
     * @see #addSortAsc(Sort)
     */
    public Query addSortDesc(Sort column);

    // //////////////////////////////////////////
    // Builder
    // //////////////////////////////////////////

    /**
     * The end of the fluent QueryInterface API. It simply executes the Query and returns the results.
     * @return the list of job instances matching the query.
     */
    public List<JobInstance> invoke();

    // //////////////////////////////////////////
    // Results handling
    // //////////////////////////////////////////

    /**
     * This sets the maximum returned results count, for pagination purposes.<br>
     * It is <strong>highly recommended to use pagination</strong> when using the QueryInterface API, since queries are expensive.
     *
     * @param pageSize the maximal result count, or null for no limit (dangerous!)
     * @return the QueryInterface itself (fluent API - used to chain calls).
     * @see #setFirstRow(Integer) setFirstRow for the other pagination parameter.
     */
    public Query setPageSize(Integer pageSize);

    /**
     * This sets the starting row returned by the query, for pagination purposes. Note that even if order is very important for paginated
     * queries (to ensure that the pages stay the same between calls for new pages), a default sort is used if none is specified.<br>
     * It is <strong>highly recommended to use pagination</strong> when using the QueryInterface API, since queries are expensive.
     *
     * @param firstRow the first row to return. 0 is equivalent to null.
     * @return the QueryInterface itself (fluent API - used to chain calls).
     * @see #setPageSize(Integer) setPageSize for the other pagination parameter.
     */
    public Query setFirstRow(Integer firstRow);

    /**
     * Get the available result count of the query. Available means that it does not take into account pagination. This is mostly used
     * when pagination is used, to be able to set a "total records count" or a "page 2 on 234" indicator. If pagination is not
     * used, this is always equal to <code>{@link #getResults()}.size()</code>.
     *
     * @return the available result count of the query.
     */
    public Integer getResultSize();

    /**
     * Get the list of job instances matching the query.
     * @return the list of job instances matching the query.
     */
    public List<JobInstance> getResults();

    // //////////////////////////////////////////
    // Stupid get/set
    // //////////////////////////////////////////

    /**
     * To QueryInterface a specific job instance. This ID is returned, for example, by the {@link JobRequest#enqueue()} method.
     * <br>
     * It is pretty useless to give any other QueryInterface parameters if you know the ID. Also note that there is a shortcut method named
     * {@link JqmClient#getJob(long)} to make a QueryInterface by ID.
     *
     * @param jobInstanceId the job instance ID
     * @return this Query
     */
    public Query setJobInstanceId(Long jobInstanceId);

    /**
     * Some job instances are launched by other job instances (linked jobs which launch one another). This allows to QueryInterface all job
     * instances launched by a specific job instance.
     *
     * @param parentId the ID of the parent job instance.
     * @return this Query
     */
    public Query setParentId(Long parentId);

    /**
     * The application name is the name of the job definition - the same name that is given in the Job Definition XML. This allows to query
     * all job instances for given job definitions. If the list contains multiple names, an OR QueryInterface takes place.
     *
     * @param applicationName the application name(s) to filter by
     * @return this Query
     */
    public Query setApplicationName(List<String> applicationName);

    /**
     * The application name is the name of the job definition - the same name that is given in the Job Definition XML. This allows to query
     * all job instances for a single given job definition. If other names were given previously (e.g. with
     * {@link #setApplicationName(List)} , they are removed by this method.
     *
     * @param applicationName the application name to filter by
     * @return this Query
     */
    public Query setApplicationName(String applicationName);

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param user the user name
     * @return this Query
     */
    public Query setUser(String user);

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param sessionId the session ID
     * @return this Query
     */
    public Query setSessionId(String sessionId);

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param jobDefKeyword1 the keyword1 used to filter
     * @return this Query
     */
    public Query setJobDefKeyword1(String jobDefKeyword1);

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param jobDefKeyword2 The keyword2 used to filter
     * @return this Query
     */
    public Query setJobDefKeyword2(String jobDefKeyword2);

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param jobDefKeyword3 The keyword3 used to filter
     * @return this Query
     */
    public Query setJobDefKeyword3(String jobDefKeyword3);

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param jobDefModule The jobDefModule used to filter
     * @return this Query
     */
    public Query setJobDefModule(String jobDefModule);

    /**
     * Optionally, it is possible to specify some classification data inside the Job Definition (usually through the import of a JobDef XML
     * file). This data exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     * <br>
     * <strong>This has nothing to so with applicationName, which is the name of the Job Definition !</strong>
     *
     * @param jobDefApplication The jobDefApplication used to filter
     * @return this Query
     */
    public Query setJobDefApplication(String jobDefApplication);

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param instanceKeyword1 The instanceKeyword1 used to filter
     * @return this Query
     */
    public Query setInstanceKeyword1(String instanceKeyword1);

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param instanceKeyword2 The instanceKeyword2 used to filter
     * @return this Query
     */
    public Query setInstanceKeyword2(String instanceKeyword2);

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param instanceKeyword3 The instanceKeyword3 used to filter
     * @return this Query
     */
    public Query setInstanceKeyword3(String instanceKeyword3);

    /**
     * Get the instance module of the request
     * @return the instance module
     */
    public String getInstanceModule();

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying.
     *
     * @param instanceModule The instanceModule used to filter
     * @return this Query
     */
    public Query setInstanceModule(String instanceModule);

    /**
     * Optionally, it is possible to specify some classification data at enqueue time (inside the {@link JobRequest} object). This data
     * exists solely for later querying (no signification whatsoever for JQM itself). This parameter allows such querying. <br>
     * <strong>This has nothing to so with applicationName, which is the name of the Job Definition !</strong>
     *
     * @param instanceApplication The instanceApplication used to filter
     * @return this Query
     */
    public Query setInstanceApplication(String instanceApplication);

    /**
     * By default, querying only occurs on ended (OK or not) job instances. If this parameter is set to true, it will also include living
     * (waiting, running, ...) job instances.<br>
     * If you also QueryInterface on live instances at the same time, this will reset pagination as it is impossible to use pagination with
     * both.<br>
     * <br>
     * Setting this to true has a noticeable performance impact and should be used as little as possible (or should be used when
     * {@link #setQueryHistoryInstances(boolean)} is false, which is not the default)
     *
     * @param queryLiveInstances whether to include live job instances in the query or not.
     * @return this Query
     */
    public Query setQueryLiveInstances(boolean queryLiveInstances);

    /**
     * By default, querying only occurs on ended (OK or not) job instances. If this parameter is set to false, however, the History will not
     * be used. This is usually used in conjunction with {@link #setQueryLiveInstances(boolean)}<br>
     * <br>
     *
     * @param queryHistoryInstances whether to include history job instances in the query or not.
     * @return this Query
     */
    public Query setQueryHistoryInstances(boolean queryHistoryInstances);

    /**
     * The time at which the execution request was enqueue. This is an {@code <=} comparison.
     *
     * @param enqueuedBefore The time before which the job instance was enqueued.
     * @return this Query
     */
    public Query setEnqueuedBefore(Calendar enqueuedBefore);

    /**
     * The time at which the execution request was enqueue. This is an {@code >=} comparison.
     *
     * @param enqueuedAfter The time after which the job instance was enqueued.
     * @return this Query
     */
    public Query setEnqueuedAfter(Calendar enqueuedAfter);

    /**
     * The time at which the execution really began (the request arrived at the top of the queue and was run by an engine). This is an {@code <=}
     * comparison.
     *
     * @param beganRunningBefore The time before which the job instance began running.
     * @return this Query
     */
    public Query setBeganRunningBefore(Calendar beganRunningBefore);

    /**
     * The time at which the execution really began (the request arrived at the top of the queue and was run by an engine). This is an {@code >=}
     * comparison.
     *
     * @param beganRunningAfter The time after which the job instance began running.
     * @return this Query
     */
    public Query setBeganRunningAfter(Calendar beganRunningAfter);

    /**
     * The time at which the execution ended, resulting in an ENDED or CRASHED status. This is an {@code <=} comparison.
     *
     * @param endedBefore The time before which the job instance ended.
     * @return this Query
     */
    public Query setEndedBefore(Calendar endedBefore);

    /**
     * The time at which the execution ended, resulting in an ENDED or CRASHED status. This is an {@code >=} comparison.
     *
     * @param endedAfter The time after which the job instance ended.
     * @return this Query
     */
    public Query setEndedAfter(Calendar endedAfter);

    /**
     * Filter by status. See {@link State} for the different possible values and their meaning. If multiple values are added, a logical OR
     * will take place.
     *
     * @param status The status to filter by.
     * @return this Query
     */
    public Query addStatusFilter(State status);

    /**
     * For querying jobs on a given queue. The list of queues can be retrieved through {@link com.enioka.jqm.client.api.JqmClient#getQueues()}.
     *
     * @param queueName The name of the queue to filter by.
     * @return this Query
     */
    public Query setQueueName(String queueName);

    /**
     * For querying jobs on a given queue. The list of queues can be retrieved through {@link com.enioka.jqm.client.api.JqmClient#getQueues()}.<br>
     * Ignored if setQueueName is used.
     *
     * @param queueId The ID of the queue to filter by.
     * @return this Query
     */
    public Query setQueueId(Long queueId);

    /**
     * For querying jobs that have run or are running on a specific JQM node.
     *
     * @param nodeName The name of the node to filter by.
     * @return this Query
     */
    public Query setNodeName(String nodeName);
}
