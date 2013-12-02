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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

import com.enioka.jqm.deliverabletools.Cryptonite;
import com.enioka.jqm.deliverabletools.DeliverableStruct;

/**
 * 
 * @author Pierre COPPEE <pierre.coppee@enioka.com>
 */
public class JobBase
{
	private Object myEngine = null;
	protected Integer parentID;
	protected Integer jobInstanceID;
	protected Integer canBeRestart;
	protected String applicationName;
	protected String sessionID;
	protected String application;
	protected String module;
	protected String other1;
	protected String other2;
	protected String other3;
	protected Map<String, String> parameters = new HashMap<String, String>();
	protected ArrayList<DeliverableStruct> sha1s = new ArrayList<DeliverableStruct>();
	private String defaultConnect;

	public void start()
	{

	}

	public void stop()
	{

	}

	public DataSource getDefaultConnection() throws NamingException
	{
		Object p = NamingManager.getInitialContext(null).lookup(defaultConnect);
		DataSource q = (DataSource) p;

		return q;
	}

	public void addDeliverable(final String path, final String fileName, final String fileLabel)
	{
		try
		{
			this.sha1s.add(new DeliverableStruct(path, fileName, Cryptonite.sha1(path + fileName), fileLabel));
		} catch (final NoSuchAlgorithmException e)
		{

			e.printStackTrace();
		}
	}

	public void sendMsg(final String msg)
	{
		// 1: get the method
		Method getMyEngine = null;
		Class c = null;
		try
		{
			c = myEngine.getClass();
			getMyEngine = c.getMethod("sendMsg", String.class);
		} catch (SecurityException e)
		{
			throw new JqmApiException("Could not access the injected sendMsg method", e);
		} catch (NoSuchMethodException e)
		{
			throw new JqmApiException("There was no sendMsg method in the injected object", e);
		}

		// 2: run the method
		try
		{
			getMyEngine.invoke(myEngine, msg);
		} catch (IllegalArgumentException e)
		{
			throw new JqmApiException("Inccorect parameters for sendMsg method", e);
		} catch (IllegalAccessException e)
		{
			throw new JqmApiException("Could not execute the injected sendMsg method for security reasons", e);
		} catch (InvocationTargetException e)
		{
			if (e.getCause() instanceof RuntimeException)
			{
				// it may be a Kill order, or whatever exception...
				throw (RuntimeException) e.getCause();
			}
			else
			{
				throw new JqmApiException("An unexpected issue occured during sendMsg", e);
			}
		}
	}

	public void sendProgress(final Integer msg)
	{
		// 1: get the method
		Method getMyEngine = null;
		Class c = null;
		try
		{
			c = myEngine.getClass();
			getMyEngine = c.getMethod("sendProgress", Integer.class);
		} catch (SecurityException e)
		{
			throw new JqmApiException("Could not access the injected sendProgress method", e);
		} catch (NoSuchMethodException e)
		{
			throw new JqmApiException("There was no sendProgress method in the injected object", e);
		}

		// 2: run the method
		try
		{
			getMyEngine.invoke(myEngine, msg);
		} catch (IllegalArgumentException e)
		{
			throw new JqmApiException("Incorrect parameters for sendProgress method", e);
		} catch (IllegalAccessException e)
		{
			throw new JqmApiException("Could not execute the injected sendProgress method for security reasons", e);
		} catch (InvocationTargetException e)
		{
			if (e.getCause() instanceof RuntimeException)
			{
				// it may be a Kill order, or whatever exception...
				throw (RuntimeException) e.getCause();
			}
			else
			{
				throw new JqmApiException("An unexpected issue occured during sendMsg", e);
			}
		}

	}

	public int enQueue(String applicationName, String user, String mail, String sessionID, String application, String module,
			String other1, String other2, String other3, Integer parentId, Integer canBeRestart, Map<String, String> parameters)
	{
		try
		{
			// If not given, consider this is a child/parent launch.
			if (parentId == null)
			{
				parentId = this.jobInstanceID;
			}

			Class c = myEngine.getClass();

			Method getMyEngine = c.getMethod("enQueue", String.class, String.class, String.class, Integer.class, String.class,
					String.class, String.class, String.class, String.class, Integer.class, Integer.class, Map.class);
			return (Integer) getMyEngine.invoke(myEngine, applicationName, user, mail, sessionID, application, module, other1, other2,
					other3, parentId, canBeRestart, parameters);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	// ---------

	public Integer getParentID()
	{

		return parentID;
	}

	public void setParentID(final Integer parentID)
	{

		this.parentID = parentID;
	}

	public int getCanBeRestart()
	{

		return canBeRestart;
	}

	public void setCanBeRestart(final int canBeRestart)
	{

		this.canBeRestart = canBeRestart;
	}

	public String getApplicationName()
	{

		return applicationName;
	}

	public void setApplicationName(final String applicationName)
	{

		this.applicationName = applicationName;
	}

	public String getSessionID()
	{

		return sessionID;
	}

	public void setSessionID(final String sessionID)
	{

		this.sessionID = sessionID;
	}

	public String getApplication()
	{

		return application;
	}

	public void setApplication(final String application)
	{

		this.application = application;
	}

	public String getModule()
	{

		return module;
	}

	public void setModule(final String module)
	{

		this.module = module;
	}

	public String getOther1()
	{

		return other1;
	}

	public void setOther1(final String other1)
	{

		this.other1 = other1;
	}

	public String getOther2()
	{

		return other2;
	}

	public void setOther2(final String other2)
	{

		this.other2 = other2;
	}

	public String getOther3()
	{

		return other3;
	}

	public void setOther3(final String other3)
	{

		this.other3 = other3;
	}

	public ArrayList<DeliverableStruct> getSha1s()
	{

		return sha1s;
	}

	public void setSha1s(final ArrayList<DeliverableStruct> sha1s)
	{

		this.sha1s = sha1s;
	}

	public Map<String, String> getParameters()
	{

		return parameters;
	}

	public void setParameters(final Map<String, String> parameters)
	{

		this.parameters = parameters;
	}

	public String getDefaultConnect()
	{
		return defaultConnect;
	}

	public void setDefaultConnect(String defaultConnect)
	{
		this.defaultConnect = defaultConnect;
	}

	public Object getMyEngine()
	{
		return myEngine;
	}

	public void setMyEngine(Object myEngine)
	{
		this.myEngine = myEngine;
	}

	public void setJobInstanceID(Integer jobInstanceID)
	{
		this.jobInstanceID = jobInstanceID;
	}

}
