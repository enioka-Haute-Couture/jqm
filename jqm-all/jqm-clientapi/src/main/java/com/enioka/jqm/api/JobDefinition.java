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

import java.util.HashMap;
import java.util.Map;

/**
 * Job execution request. It contains all the data needed to enqueue a request, as well as non-mandatory data.
 * 
 */
public class JobDefinition
{
	private Integer parentID;
	private String applicationName;
	private String sessionID;
	private String application;
	private String user;
	private String module;
	private String keyword1;
	private String keyword2;
	private String keyword3;
	private String email = null;
	private Map<String, String> parameters = new HashMap<String, String>();

	JobDefinition()
	{

	}

	/**
	 * Public constructor
	 * 
	 * @param applicationName
	 *            name (key) of the job to launch
	 * @param user
	 *            name of the human user that is at the origin of the request. If no user, use the application module name.
	 */
	public JobDefinition(String applicationName, String user)
	{
		this.applicationName = applicationName;
		this.user = user;
	}

	/**
	 * Public constructor
	 * 
	 * @param applicationName
	 *            name (key) of the job to launch
	 * @param user
	 *            name of the human user that is at the origin of the request. If no user, use the application module name.
	 * @param email
	 * 			  email of the human user that to want to receive it when the job will ended.
	 */
	public JobDefinition(String applicationName, String user, String email)
	{
		this.applicationName = applicationName;
		this.user = user;
		this.email = email;
	}

	/**
	 * Parameters are <key,value> pairs that are passed at runtime to the job. The amount of required parameters depends on the requested
	 * job itself.
	 * 
	 * @param key
	 * @param value
	 */
	public void addParameter(String key, String value)
	{
		parameters.put(key, value);
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
	 * <strong>Optional</strong><br>
	 * A job instance can be the child of another job instance. This allows you to retrieve the ID of that parent. It is null if there is no
	 * parent.
	 * 
	 * @return
	 */
	public Integer getParentID()
	{
		return parentID;
	}

	/**
	 * <strong>Optional</strong><br>
	 * A job instance can be the child of another job instance. This allows you to retrieve the ID of that parent. It is null if there is no
	 * parent.
	 * 
	 * @param parentID
	 */
	public void setParentID(Integer parentID)
	{
		this.parentID = parentID;
	}

	/**
	 * <strong>Compulsory</strong><br>
	 * The name of the batch job to launch. It is the "Job Definition" name, and the most important parameter in this form.
	 * 
	 * @return
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
	 */
	public void setApplicationName(String applicationName)
	{
		this.applicationName = applicationName;
	}

	/**
	 * <strong>Optional</strong><br>
	 * It is possible to link a job instance to an arbitrary ID, such as a session ID and later query result by this ID.<br>
	 * Default is null.
	 * 
	 * @return
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
	 */
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	/**
	 * <strong>Optional</strong><br>
	 * The application making the query. E.g.: Accounting, Interfaces, ...
	 * 
	 * @return
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
	 */
	public void setApplication(String application)
	{
		this.application = application;
	}

	/**
	 * <strong>Optional</strong><br>
	 * An optional classification axis (and therefore query criterion)
	 * 
	 * @return
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
	 */
	public void setModule(String module)
	{
		this.module = module;
	}

	/**
	 * <strong>Optional</strong><br>
	 * An optional classification axis (and therefore query criterion)
	 * 
	 * @return
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
	 */
	public void setKeyword1(String keyword1)
	{
		this.keyword1 = keyword1;
	}

	/**
	 * <strong>Optional</strong><br>
	 * An optional classification axis (and therefore query criterion)
	 * 
	 * @return
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
	 */
	public void setKeyword2(String keyword2)
	{
		this.keyword2 = keyword2;
	}

	/**
	 * <strong>Optional</strong><br>
	 * An optional classification axis (and therefore query criterion)
	 * 
	 * @return
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
	 */
	public void setKeyword3(String keyword3)
	{
		this.keyword3 = keyword3;
	}

	/**
	 * Get the Map of all parameters
	 * 
	 * @return
	 */
	public Map<String, String> getParameters()
	{
		return parameters;
	}

	public void setParameters(Map<String, String> parameters)
	{
		this.parameters = parameters;
	}

	/**
	 * <strong>Optional</strong><br>
	 * It is possible to associate a user to a job execution request, and later query job execution by user.
	 * 
	 * @return
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
	 */
	public void setUser(String user)
	{
		this.user = user;
	}

	/**
	 * <strong>Compulsory</strong><br>
	 * The email of the user that want to received it.
	 * 
	 * @return
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * <strong>Compulsory</strong><br>
	 * The user can enter an email to receive an email when the job is ended.
	 * 
	 * @param email
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}
}