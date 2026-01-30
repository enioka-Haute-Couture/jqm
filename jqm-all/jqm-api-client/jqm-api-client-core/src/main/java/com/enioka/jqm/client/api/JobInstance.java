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

package com.enioka.jqm.client.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Represents the result of a job execution request - either a queued request, or a running job, or the result of said execution.
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobInstance
{
    /**
     * Create a new empty JobInstance.
     */
    public JobInstance()
    {}

    private long id;
    private String applicationName;
    private Long parent;
    private String user;
    private String sessionID;
    private String traceId;
    private State state;
    private Long position;
    private Integer priority;
    private Queue queue;
    private String queueName;
    private String keyword1, keyword2, keyword3, definitionKeyword1, definitionKeyword2, definitionKeyword3, module, email, application;
    private Map<String, String> parameters = new HashMap<>();
    private Integer progress;
    @XmlElementWrapper(name = "messages")
    @XmlElement(name = "message", type = Message.class)
    private List<Message> messages = new ArrayList<>();
    private Calendar enqueueDate, beganRunningDate, endDate, runAfter;
    private String nodeName;
    private boolean highlander;
    private boolean fromSchedule;

    /**
     * The Job Instance ID, i.e. the unique identifier of the execution request. This is a key for numerous {@link JqmClient} functions.
     *
     * @return the Job Instance ID
     */
    public long getId()
    {
        return id;
    }

    /**
     * See {@link #getId()}
     *
     * @param id
     *            the Job Instance ID
     */
    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * The ID of the parent job that has enqueued this job instance. Null if the execution request was not done by a running job.
     *
     * @return the parent job ID
     */
    public Long getParent()
    {
        return parent;
    }

    /**
     * See {@link #getParent()}
     *
     * @param parent
     *            the parent job ID
     */
    public void setParent(Long parent)
    {
        this.parent = parent;
    }

    /**
     * The username that was given at enqueue time. Optional.
     *
     * @return the username
     */
    public String getUser()
    {
        return user;
    }

    /**
     * See {@link #getUser()}
     *
     * @param user
     *            the username
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * The session ID that was given at enqueue time. Optional.
     *
     * @return the session ID
     */
    public String getSessionID()
    {
        return sessionID;
    }

    /**
     * See {@link #getSessionID()}
     *
     * @param sessionID
     *            the session ID
     */
    public void setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
    }

    /**
     * The trace ID that was given at enqueue time. Optional.
     *
     * @param traceId
     *            trace Id
     */
    public void setTraceId(String traceId)
    {
        this.traceId = traceId;
    }

    public String getTraceId()
    {
        return this.traceId;
    }

    /**
     * Status of the job. Usual cycle: SUBMITTED → ATTRIBUTED → RUNNING, → DONE or CRASHED.
     *
     * @return the job state
     */
    public State getState()
    {
        return state;
    }

    /**
     * See {@link #getState()}
     *
     * @param state
     *            the job state
     */
    public void setState(State state)
    {
        this.state = state;
    }

    /**
     * Position in the queue. 0 if running.<br>
     * <strong>This is the value retrieved during the latest {@link JqmClient#getJob(long)} call and may not be up to date!</strong>
     *
     * @return the position in the queue.
     */
    public Long getPosition()
    {
        return position;
    }

    /**
     * See {@link #getPosition()}
     *
     * @param position
     *            the position in the queue.
     */
    public void setPosition(Long position)
    {
        this.position = position;
    }

    /**
     * The queue in which the job was enqueued. Will be null if the queue has been deleted. In that case use {@link #getQueueName()}.
     *
     * @return the queue
     */
    public Queue getQueue()
    {
        return queue;
    }

    /**
     * See {@link #getQueue()}
     *
     * @param queue
     *            the queue
     */
    public void setQueue(Queue queue)
    {
        this.queue = queue;
    }

    /**
     * The queue in which the job was enqueued.
     *
     * @return the queue name
     */
    public String getQueueName()
    {
        return queueName;
    }

    /**
     * See {@link #getQueueName()}
     *
     * @param queueName
     *            the queue name
     */
    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }

    /**
     * A list of all the parameters used by this job (both those passed at enqueue time and those defined as default parameters for this
     * kind of jobs)
     *
     * @return a map of parameter names to values.
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * See {@link #getParameters()}
     *
     * @param parameters
     *            a map of parameter names to values.
     */
    public void setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    /**
     * An optional integer that running user code may update from time to time. Used to give an idea of the progress of the job instance.
     * <br>
     * <strong>This is the value retrieved during the latest {@link JqmClient#getJob(long)} call and may not be up to date!</strong>
     *
     * @return the progress of the job instance.
     */
    public Integer getProgress()
    {
        return progress;
    }

    /**
     * See {@link #getProgress()}
     *
     * @param progress
     *            the progress of the job instance.
     */
    public void setProgress(Integer progress)
    {
        this.progress = progress;
    }

    /**
     * An optional list of messages that running user code may emit from time to time. Used to give an idea of the progress of the job
     * instance.<br>
     * <strong>This is the value retrieved during the latest {@link JqmClient#getJob(long)} call and may not be up to date!</strong>
     *
     * @return the list of messages.
     */
    public List<Message> getMessages()
    {
        return messages;
    }

    /**
     * See {@link #getMessages()}
     *
     * @param messages
     *            the list of messages.
     */
    public void setMessages(List<Message> messages)
    {
        this.messages = messages;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     *
     * @return the keyword1
     */
    public String getKeyword1()
    {
        return keyword1;
    }

    /**
     * See {@link #getKeyword1()}
     *
     * @param keyword1
     *            the keyword1
     */
    public void setKeyword1(String keyword1)
    {
        this.keyword1 = keyword1;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     *
     * @return the keyword2
     */
    public String getKeyword2()
    {
        return keyword2;
    }

    /**
     * See {@link #getKeyword2()}
     *
     * @param keyword2
     *            the keyword2
     */
    public void setKeyword2(String keyword2)
    {
        this.keyword2 = keyword2;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     *
     * @return the keyword3
     */
    public String getKeyword3()
    {
        return keyword3;
    }

    /**
     * See {@link #getKeyword3()}
     *
     * @param keyword3
     *            the keyword3
     */
    public void setKeyword3(String keyword3)
    {
        this.keyword3 = keyword3;
    }

    /**
     * An optional classification tag which can be specified inside the definition of the job (default is NULL).
     *
     * @return the definitionKeyword1
     */
    public String getDefinitionKeyword1()
    {
        return definitionKeyword1;
    }

    /**
     * See {@link #getDefinitionKeyword1()}
     *
     * @param keyword1
     *            the definitionKeyword1
     */
    public void setDefinitionKeyword1(String keyword1)
    {
        this.definitionKeyword1 = keyword1;
    }

    /**
     * An optional classification tag which can be specified inside the definition of the job (default is NULL).
     *
     * @return the definitionKeyword2
     */
    public String getDefinitionKeyword2()
    {
        return definitionKeyword2;
    }

    /**
     * See {@link #getDefinitionKeyword2()}
     *
     * @param keyword2
     *            the definitionKeyword2
     */
    public void setDefinitionKeyword2(String keyword2)
    {
        this.definitionKeyword2 = keyword2;
    }

    /**
     * An optional classification tag which can be specified inside the definition of the job (default is NULL).
     *
     * @return the definitionKeyword3
     */
    public String getDefinitionKeyword3()
    {
        return definitionKeyword3;
    }

    /**
     * See {@link #getDefinitionKeyword3()}
     *
     * @param keyword3
     *            the definitionKeyword3
     */
    public void setDefinitionKeyword3(String keyword3)
    {
        this.definitionKeyword3 = keyword3;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     *
     * @return the application
     */
    public String getApplication()
    {
        return application;
    }

    /**
     * See {@link #getApplication()}
     *
     * @param application
     *            the application
     */
    public void setApplication(String application)
    {
        this.application = application;
    }

    /**
     * The functional key that identifies a job definition template (a JobDef, as imported from XML).
     *
     * @return the application name
     */
    public String getApplicationName()
    {
        return applicationName;
    }

    /**
     * See {@link #getApplicationName()}
     *
     * @param applicationName
     *            the application name
     */
    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    /**
     * Get the module of the Job Instance
     *
     * @return the module
     */
    public String getModule()
    {
        return module;
    }

    /**
     * Set the module of the Job Instance
     *
     * @param module
     *            the module
     */
    public void setModule(String module)
    {
        this.module = module;
    }

    /**
     * If this field is non-null, an e-mail will be sent at this address at the end of the run.
     *
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * See {@link #getEmail()}
     *
     * @param email
     *            the email
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * The time at which the execution request was given to {@link JobRequest#enqueue()}.
     *
     * @return the enqueue date
     */
    public Calendar getEnqueueDate()
    {
        return enqueueDate;
    }

    /**
     * See {@link #getEnqueueDate()}
     *
     * @param enqueueDate
     *            the enqueue date
     */
    public void setEnqueueDate(Calendar enqueueDate)
    {
        this.enqueueDate = enqueueDate;
    }

    /**
     * The time at which the execution really began (the request arrived at the top of the queue and was run by an engine).
     *
     * @return the beginning running date
     */
    public Calendar getBeganRunningDate()
    {
        return beganRunningDate;
    }

    /**
     * See {@link #getBeganRunningDate()}
     *
     * @param beganRunningDate
     *            the beginning running date
     */
    public void setBeganRunningDate(Calendar beganRunningDate)
    {
        this.beganRunningDate = beganRunningDate;
    }

    /**
     * The time at which the execution ended, resulting in an ENDED or CRASHED status.
     *
     * @return the end date
     */
    public Calendar getEndDate()
    {
        return endDate;
    }

    /**
     * See {@link #getEndDate()}
     *
     * @param endDate
     *            the end date
     */
    public void setEndDate(Calendar endDate)
    {
        this.endDate = endDate;
    }

    /**
     * The name of the JQM node that is running or has run the job instance. This is a String that logically identifies the node, not the
     * hostname.
     *
     * @return the node name.
     */
    public String getNodeName()
    {
        return nodeName;
    }

    /**
     * See {@link #getNodeName()}
     *
     * @param nodeName
     *            the node name
     */
    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    /**
     * True if the job instance was subject to the Highlander mode rules.
     *
     * @return true if highlander, false otherwise.
     */
    public boolean isHighlander()
    {
        return highlander;
    }

    /**
     * See {@link #isHighlander()}
     *
     * @param h
     *            true if highlander, false otherwise.
     */
    public void setHighlander(boolean h)
    {
        this.highlander = h;
    }

    /**
     * True if the job instance was created from a schedule.
     *
     * @return true if from schedule, false otherwise.
     */
    public boolean isFromSchedule()
    {
        return this.fromSchedule;
    }

    /**
     * See {@link #isFromSchedule()}
     *
     * @param b
     *            true if from schedule, false otherwise.
     */
    public void setFromSchedule(boolean b)
    {
        this.fromSchedule = b;
    }

    /**
     * The thread and queue priority of the job instance.
     *
     * @return the priority
     */
    public Integer getPriority()
    {
        return priority;
    }

    /**
     * See {@link #getPriority()}
     *
     * @param priority
     *            the priority
     */
    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    /**
     * An optional date in the future after which JQLM should try to run the job. It is always null for ended job instances.
     *
     * @return the runAfter date
     */
    public Calendar getRunAfter()
    {
        return this.runAfter;
    }

    /**
     * See {@link #getRunAfter()}
     *
     * @param c
     *            the runAfter date
     */
    public void setRunAfter(Calendar c)
    {
        this.runAfter = c;
    }
}
