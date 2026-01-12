/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.enioka.jqm.client.api.JobRequest;
import com.enioka.jqm.client.api.JqmClient;
import com.enioka.jqm.client.api.JqmInvalidRequestException;
import com.enioka.jqm.client.api.State;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 *
 * Job execution request. It contains the job application name (the only mandatory piece of data needed to enqueue a request), as well as
 * non-mandatory data. It is consumed by {@link JobRequest#enqueue()}
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobRequestBaseImpl implements JobRequest
{
    private static final long serialVersionUID = -2289375352629706591L;

    private String applicationName;
    private String sessionID;
    private String application;
    private String user;
    private String module;
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private String email = null;
    private String queueName = null;
    private Long parentJobId = null;
    private Long scheduleId = null;
    private State startState = null;
    private Integer priority = 0;
    private Map<String, String> parameters = new HashMap<>();

    private Calendar runAfter;
    private String recurrence;

    @XmlTransient
    private JqmClientEnqueueCallback enqueueCallback;

    // JAXB convention

    /**
     * Default constructor for JAXB.
     */
    public JobRequestBaseImpl() {}

    /**
     * Constructor
     * @param enqueueCallback enqueue callback
     */
    public JobRequestBaseImpl(JqmClientEnqueueCallback enqueueCallback)
    {
        this.enqueueCallback = enqueueCallback;
    }

    /**
     * Enqueues the job request using the provided enqueue callback.
     * @return the job ID of the enqueued job
     */
    @Override
    public long enqueue()
    {
        return enqueueCallback.enqueue(this);
    }

    /**
     * Parameters are key,value pairs that are passed at runtime to the job. The amount of required parameters depends on the requested
     * job itself.
     *
     * @param key max length is 50
     * @param value max length is 1000
     * @return this jobRequest
     */
    public JobRequest addParameter(String key, String value)
    {
        validateParameter(key, value);
        parameters.put(key, value);
        return this;
    }

    /**
     * Validates that the parameter key and value are within the allowed length limits.
     * @see #addParameter(String, String) addParameter(String key, String value) for a description of the parameters
     *
     * @param key the key of the parameter to validate
     * @param value the value of the parameter to validate
     */
    private void validateParameter(String key, String value)
    {
        if (key == null || key.isEmpty() || key.length() > 50 || value == null || value.isEmpty() || value.length() > 1000)
        {
            throw new JqmInvalidRequestException(
                    "Parameters key must be between 1 and 50 characters, parameter values between 1 and 1000 characters");
        }
    }

    /**
     * See {@link #addParameter(String, String)}
     *
     * @param prms the parameters to add
     * @return this jobRequest
     */
    public JobRequest addParameters(Map<String, String> prms)
    {
        for (Map.Entry<String, String> e : prms.entrySet())
        {
            validateParameter(e.getKey(), e.getValue());
        }
        parameters.putAll(prms);
        return this;
    }

    /**
     * Parameters are key,value pairs that are passed at runtime to the job. The amount of required parameters depends on the requested
     * job itself. If there is no parameter named key, no error is thrown.
     *
     * @param key the key of the parameter to delete
     */
    public void delParameter(String key)
    {
        parameters.remove(key);
    }

    /**
     * <strong>Compulsory</strong> (unless {@link com.enioka.jqm.client.api.JobRequest#setScheduleId(Long)} is used)<br>
     * The name of the batch job to launch. It is the "Job Definition" name, and the most important parameter in this form.
     *
     * @return the application name
     */
    public String getApplicationName()
    {
        return applicationName;
    }

    /**
     * <strong>Compulsory</strong> (unless {@link com.enioka.jqm.client.api.JobRequest#setScheduleId(Long)} is used)<br>
     * The name of the batch job to launch. It is the "Job Definition" name, and the most important parameter in this form.
     *
     * @param applicationName max length is 100
     * @return this jobRequest
     */
    public JobRequest setApplicationName(String applicationName)
    {
        if (applicationName == null || applicationName.length() > 100)
        {
            throw new JqmInvalidRequestException("Job definition name must be between 1 and 100 characters");
        }
        this.applicationName = applicationName;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * It is possible to link a job instance to an arbitrary ID, such as a session ID and later query result by this ID.<br>
     * Default is null.
     *
     * @return the session ID
     */
    public String getSessionID()
    {
        return sessionID;
    }

    /**
     * <strong>Optional</strong><br>
     * It is possible to link a job instance to an arbitrary ID, such as a session ID and later query result by this ID.<br>
     * Default is null.
     *
     * @param sessionID max length is 100
     * @return this jobRequest
     */
    public JobRequest setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * The application making the query. E.g.: Accounting, Interfaces, ...
     *
     * @return the application name
     */
    public String getApplication()
    {
        return application;
    }

    /**
     * <strong>Optional</strong><br>
     * The application making the query. E.g.: Accounting, Interfaces, ...
     *
     * @param application max length is 50
     * @return this jobRequest
     */
    public JobRequest setApplication(String application)
    {
        this.application = application;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     *
     * @return the module name
     */
    public String getModule()
    {
        return module;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     *
     * @param module max length is 50
     * @return this jobRequest
     */
    public JobRequest setModule(String module)
    {
        this.module = module;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     *
     * @return the keyword1
     */
    public String getKeyword1()
    {
        return keyword1;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     *
     * @param keyword1 max length is 50
     * @return this jobRequest
     */
    public JobRequest setKeyword1(String keyword1)
    {
        this.keyword1 = keyword1;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     *
     * @return the keyword2
     */
    public String getKeyword2()
    {
        return keyword2;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     *
     * @param keyword2 max length is 50
     * @return this jobRequest
     */
    public JobRequest setKeyword2(String keyword2)
    {
        this.keyword2 = keyword2;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     *
     * @return the keyword3
     */
    public String getKeyword3()
    {
        return keyword3;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     *
     * @param keyword3 max length is 50
     * @return this jobRequest
     */
    public JobRequest setKeyword3(String keyword3)
    {
        this.keyword3 = keyword3;
        return this;
    }

    /**
     * Get the Map of all parameters. This is a copy, not the original map so changes made to the result map are not taken into account.
     *
     * @return a copy of the parameters map
     */
    public Map<String, String> getParameters()
    {
        return new HashMap<>(parameters);
    }

    /**
     * After validating the parameters, set them to the job request.
     * @param parameters dictionary of all parameters.
     * @return this jobRequest
     */
    public JobRequest setParameters(Map<String, String> parameters)
    {
        for (Map.Entry<String, String> e : parameters.entrySet())
        {
            validateParameter(e.getKey(), e.getValue());
        }
        this.parameters = parameters;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * It is possible to associate a user to a job execution request, and later query job execution by user.
     *
     * @return the username
     */
    public String getUser()
    {
        return user;
    }

    /**
     * <strong>Optional</strong><br>
     * It is possible to associate a user to a job execution request, and later query job execution by user.
     *
     * @param user max length is 50
     * @return this jobRequest
     */
    public JobRequest setUser(String user)
    {
        this.user = user;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * The email of the user that want to received a notification.
     *
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * <strong>Optional</strong><br>
     * The user can enter an email to receive an email when the job is ended.
     *
     * @param email max length is 100
     * @return this jobRequest
     */
    public JobRequest setEmail(String email)
    {
        this.email = email;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * A job instance can be the child of another job instance. This allows you to retrieve the ID of that parent. It is null if there is no
     * parent.
     *
     * @return the parent job ID, or null if there is no parent.
     */
    public String getParentJobId()
    {
        return parentJobId == null ? null : parentJobId.toString();
    }

    /**
     * <strong>Optional</strong><br>
     * A job instance can be the child of another job instance. This allows you to set the ID of that parent. It should be left null if
     * there is no parent.
     *
     * @param parentJobId the parent job ID
     * @return this jobRequest
     */
    public JobRequest setParentJobId(String parentJobId)
    {
        this.parentJobId = Long.parseLong(parentJobId);
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * A job instance can be the child of another job instance. This allows you to retrieve the ID of that parent. It is null if there is no
     * parent.
     *
     * @return the parent job ID
     */
    public Long getParentID()
    {
        return parentJobId;
    }

    /**
     * <strong>Optional</strong><br>
     * A job instance can be the child of another job instance. This allows you to set the ID of that parent. It should be left null if
     * there is no parent.
     *
     * @param parentJobId the parent job ID
     * @return this jobRequest
     */
    public JobRequest setParentID(Long parentJobId)
    {
        this.parentJobId = parentJobId;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * The (FIFO) queue inside which the job request should wait for a free execution slot inside an engine. If null, the queue designated
     * as the default queue for this "application name" will be used.<br>
     * <strong>Most of the time, this should be left to null.</strong> This parameter is only provided to avoid doing two API calls for a
     * single execution request (first enqueue, then change queue) when it is certain a specific queue will have to be used.<br>
     * If there is no queue of this name, the enqueue method will throw a <code>JqmInvalidRequestException</code>.
     *
     * @return the queue name
     */
    public String getQueueName()
    {
        return queueName;
    }

    /**
     * <strong>Optional</strong><br>
     * The (FIFO) queue inside which the job request should wait for a free execution slot inside an engine. If null, the queue designated
     * as the default queue for this "application name" will be used.<br>
     * <strong>Most of the time, this should be left to null.</strong> This parameter is only provided to avoid doing two API calls for a
     * single execution request (first enqueue, then change queue) when it is certain a specific queue will have to be used.<br>
     * If there is no queue of this name, the enqueue method will throw a <code>JqmInvalidRequestException</code>.
     *
     * @param queueName the queue name
     * @return this jobRequest
     */
    public JobRequest setQueueName(String queueName)
    {
        this.queueName = queueName;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * This request is actually to create an occurrence of the specified recurrence. If specified, the {@link #getApplicationName()} is
     * ignored.
     *
     * @return the schedule ID
     */
    public Long getScheduleId()
    {
        return this.scheduleId;
    }

    /**
     * <strong>Optional</strong><br>
     * This request is actually to create an occurrence of the specified recurrence. If specified, the {@link #getApplicationName()} is
     * ignored.
     *
     * @param id the schedule ID
     * @return this jobRequest
     */
    public JobRequest setScheduleId(Long id)
    {
        this.scheduleId = id;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * The default behaviour for a newly submitted JobRequest is to run as soon as possible (i.e. as soon as there is a free slot inside a
     * JQM node). This method allows to change this, and to put the request inside the queue but not run it when it reaches the top of the
     * queue. It will only be eligible for run when the given date is reached. When the given date is reached, standard queuing resumes.<br>
     * The resolution of this function is the minute: seconds and lower are ignored (truncated).<br>
     *
     * @param whenToRun the date when the job should run
     * @return this jobRequest
     */
    public JobRequest setRunAfter(Calendar whenToRun)
    {
        if (whenToRun == null)
        {
            this.runAfter = null;
            return this;
        }

        this.runAfter = (Calendar) whenToRun.clone();
        this.runAfter.set(Calendar.SECOND, 0);
        this.runAfter.set(Calendar.MILLISECOND, 0);
        return this;
    }

    /**
     * Getter for the run after date.
     * @return the run after date
     */
    public Calendar getRunAfter()
    {
        return this.runAfter;
    }

    /**
     * <strong>Optional</strong><br>
     * This method allows to request for the run to be recurring. This actually creates a Scheduled Job for the given applicationName and
     * optionally queue and parameters. (all other JobRequest elements are ignored). Note that when using this, there is no request
     * immediately added to the queues - the actual requests will be created by the schedule.<br>
     * When creating a new recurrence, the ID returned by {@link JobRequest#enqueue()} is actually the schedule ID.
     *
     * @param cronExpression the cron expression for the recurrence
     * @return this jobRequest
     */
    public JobRequest setRecurrence(String cronExpression)
    {
        this.recurrence = cronExpression;
        return this;
    }

    /**
     * Getter for the recurrence cron expression
     * @see #setRecurrence(String)
     * @return the cron expression for the recurrence
     */
    public String getRecurrence()
    {
        return this.recurrence;
    }

    /**
     * <strong>Optional</strong><br>
     * The default behaviour for a newly submitted JobRequest is to run as soon as possible (i.e. as soon as there is a free slot inside a
     * JQM node). This method allows to change this, and to put the request inside the queue but not run it until the
     * {@link JqmClient#resumeQueuedJob(long)} method is called on the newly created job instance.
     *
     * @return this jobRequest
     */
    public JobRequest startHeld()
    {
        this.startState = State.HOLDED;
        return this;
    }

    /**
     * Getter for the start state of the job request
     * @return the start state
     */
    public State getStartState()
    {
        return this.startState;
    }

    /**
     * <strong>Optional</strong><br>
     * By default, JQM queues are pure FIFO. Setting a priority changes that: job instances with a higher priority always run before job
     * instances with a lower priority (or no priority at all). Also, this is directly translated into Thread priority when the job instance
     * actually runs.<br>
     * Higher priority is better (runs before the others, has more CPU share).<br>
     * Priority must be between {@link Thread#MIN_PRIORITY} and {@link Thread#MAX_PRIORITY}. To remove priority, set it to null.
     *
     * @param priority the priority to set.
     * @return this jobRequest
     */
    public JobRequest setPriority(Integer priority)
    {
        if (priority == null || priority == 0)
        {
            priority = 0;
            return this;
        }
        if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY)
        {
            throw new JqmInvalidRequestException("Priority must be between Thread.MIN_PRIORITY and Thread.MAX_PRIORITY, not " + priority);
        }

        this.priority = priority;
        return this;
    }

    /**
     * Getter for the priority
     *
     * @see #setPriority(Integer)
     * @return the priority
     */
    public Integer getPriority()
    {
        return this.priority;
    }
}
