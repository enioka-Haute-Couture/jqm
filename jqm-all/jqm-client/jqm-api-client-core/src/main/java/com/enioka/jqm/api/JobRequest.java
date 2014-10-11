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

package com.enioka.jqm.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Job execution request. It contains all the data needed to enqueue a request (the application name), as well as non-mandatory data. It is
 * consumed by {@link JqmClient#enqueue(JobRequest)}
 */
@XmlRootElement
public class JobRequest implements Serializable
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
    private Integer parentJobId = null;
    private Map<String, String> parameters = new HashMap<String, String>();

    JobRequest()
    {

    }

    /**
     * Public constructor
     * 
     * @param applicationName
     *            name (key) of the job to launch
     * @param user
     *            name of the human user that is at the origin of the request. If no user (e.g. inside an automated system), the application
     *            module name should be used.
     */
    public JobRequest(String applicationName, String user)
    {
        this.applicationName = applicationName;
        this.user = user;
    }

    /**
     * Public constructor for fluid API.
     * 
     * @param applicationName
     *            name (key) of the job to launch
     * @param user
     *            name of the human user that is at the origin of the request. If no user (e.g. inside an automated system), the application
     *            module name should be used.
     */
    public static JobRequest create(String applicationName, String user)
    {
        return new JobRequest(applicationName, user);
    }

    /**
     * Public constructor
     * 
     * @param applicationName
     *            name (key) of the job to launch
     * @param user
     *            name of the human user that is at the origin of the request. If no user, use the application module name.
     * @param email
     *            email of the human user that to want to receive a notification when the job ends.
     */
    public JobRequest(String applicationName, String user, String email)
    {
        this.applicationName = applicationName;
        this.user = user;
        this.email = email;
    }

    /**
     * Shortcut to submit the request to the JQM cluster. Equivalent to doing<br>
     * <code>JqmClientFactory.getClient().enqueue(this)</code><br>
     * See {@link JqmClient#enqueue(JobRequest)} for details on exceptions.
     * 
     * @return the ID of the job instance.
     */
    public Integer submit()
    {
        return JqmClientFactory.getClient().enqueue(this);
    }

    /**
     * Parameters are <key,value> pairs that are passed at runtime to the job. The amount of required parameters depends on the requested
     * job itself.
     * 
     * @param key
     *            max length is 50
     * @param value
     *            max length is 1000
     */
    public JobRequest addParameter(String key, String value)
    {
        parameters.put(key, value);
        return this;
    }

    /**
     * Parameters are <key,value> pairs that are passed at runtime to the job. The amount of required parameters depends on the requested
     * job itself. If there is no parameter named key, no error is thrown.
     * 
     * @param key
     */
    public void delParameter(String key)
    {
        parameters.remove(key);
    }

    /**
     * <strong>Compulsory</strong><br>
     * The name of the batch job to launch. It is the "Job Definition" name, and the most important parameter in this form.
     */
    public String getApplicationName()
    {
        return applicationName;
    }

    /**
     * <strong>Compulsory</strong><br>
     * The name of the batch job to launch. It is the "Job Definition" name, and the most important parameter in this form.
     * 
     * @param applicationName
     *            max length is 100
     */
    public JobRequest setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * It is possible to link a job instance to an arbitrary ID, such as a session ID and later query result by this ID.<br>
     * Default is null.
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
     * @param sessionID
     *            max length is 100
     */
    public JobRequest setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * The application making the query. E.g.: Accounting, Interfaces, ...
     */
    public String getApplication()
    {
        return application;
    }

    /**
     * <strong>Optional</strong><br>
     * The application making the query. E.g.: Accounting, Interfaces, ...
     * 
     * @param application
     *            max length is 50
     */
    public JobRequest setApplication(String application)
    {
        this.application = application;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     */
    public String getModule()
    {
        return module;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     * 
     * @param module
     *            max length is 50
     */
    public JobRequest setModule(String module)
    {
        this.module = module;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     */
    public String getKeyword1()
    {
        return keyword1;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     * 
     * @param keyword1
     *            max length is 50
     */
    public JobRequest setKeyword1(String keyword1)
    {
        this.keyword1 = keyword1;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     */
    public String getKeyword2()
    {
        return keyword2;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     * 
     * @param keyword2
     *            max length is 50
     */
    public JobRequest setKeyword2(String keyword2)
    {
        this.keyword2 = keyword2;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     */
    public String getKeyword3()
    {
        return keyword3;
    }

    /**
     * <strong>Optional</strong><br>
     * An optional classification axis (and therefore query criterion)
     * 
     * @param keyword3
     *            max length is 50
     */
    public JobRequest setKeyword3(String keyword3)
    {
        this.keyword3 = keyword3;
        return this;
    }

    /**
     * Get the Map of all parameters
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public JobRequest setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * It is possible to associate a user to a job execution request, and later query job execution by user.
     */
    public String getUser()
    {
        return user;
    }

    /**
     * <strong>Optional</strong><br>
     * It is possible to associate a user to a job execution request, and later query job execution by user.
     * 
     * @param user
     *            max length is 50
     */
    public JobRequest setUser(String user)
    {
        this.user = user;
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * The email of the user that want to received a notification.
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * <strong>Optional</strong><br>
     * The user can enter an email to receive an email when the job is ended.
     * 
     * @param email
     *            max length is 100
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
     */
    public String getParentJobId()
    {
        return parentJobId == null ? null : parentJobId.toString();
    }

    /**
     * <strong>Optional</strong><br>
     * A job instance can be the child of another job instance. This allows you to set the ID of that parent. It should be left null if
     * there is no parent.
     */
    public JobRequest setParentJobId(String parentJobId)
    {
        this.parentJobId = Integer.parseInt(parentJobId);
        return this;
    }

    /**
     * <strong>Optional</strong><br>
     * A job instance can be the child of another job instance. This allows you to retrieve the ID of that parent. It is null if there is no
     * parent.
     */
    public Integer getParentID()
    {
        return parentJobId;
    }

    /**
     * <strong>Optional</strong><br>
     * A job instance can be the child of another job instance. This allows you to set the ID of that parent. It should be left null if
     * there is no parent.
     */
    public JobRequest setParentID(Integer parentJobId)
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
     */
    public JobRequest setQueueName(String queueName)
    {
        this.queueName = queueName;
        return this;
    }
}