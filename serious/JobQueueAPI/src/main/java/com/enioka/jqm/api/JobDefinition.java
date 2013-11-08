/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
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
	private int parentID;
	private String applicationName;
	private Integer sessionID;
	private String application;
	private String user;
	private String module;
	private String other1;
	private String other2;
	private String other3;
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
	public int getParentID()
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
	public void setParentID(int parentID)
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
	public Integer getSessionID()
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
	public void setSessionID(Integer sessionID)
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
	public String getOther1()
	{
		return other1;
	}

	/**
	 * <strong>Optional</strong><br>
	 * An optional classification axis (and therefore query criterion)
	 * 
	 * @param other1
	 */
	public void setOther1(String other1)
	{
		this.other1 = other1;
	}

	/**
	 * <strong>Optional</strong><br>
	 * An optional classification axis (and therefore query criterion)
	 * 
	 * @return
	 */
	public String getOther2()
	{
		return other2;
	}

	/**
	 * <strong>Optional</strong><br>
	 * An optional classification axis (and therefore query criterion)
	 * 
	 * @param other2
	 */
	public void setOther2(String other2)
	{
		this.other2 = other2;
	}

	/**
	 * <strong>Optional</strong><br>
	 * An optional classification axis (and therefore query criterion)
	 * 
	 * @return
	 */
	public String getOther3()
	{
		return other3;
	}

	/**
	 * <strong>Optional</strong><br>
	 * An optional classification axis (and therefore query criterion)
	 * 
	 * @param other3
	 */
	public void setOther3(String other3)
	{
		this.other3 = other3;
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

	void setParameters(Map<String, String> parameters)
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
}