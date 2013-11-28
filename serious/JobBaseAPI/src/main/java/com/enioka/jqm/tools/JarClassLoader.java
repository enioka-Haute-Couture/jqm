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

package com.enioka.jqm.tools;

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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;

class JarClassLoader extends URLClassLoader
{
	private static Logger jqmlogger = Logger.getLogger(JarClassLoader.class);

	private static URL[] addUrls(URL url, URL[] libs)
	{
		URL[] urls = new URL[libs.length + 1];
		urls[0] = url;
		for (int i = 0; i < libs.length; i++)
		{
			urls[i + 1] = libs[i];
		}
		return urls;
	}

	JarClassLoader(URL url, URL[] libs)
	{
		super(addUrls(url, libs), null);
	}

	Object invokeMain(JobInstance job, String defaultConnection, ClassLoader old, EntityManager em) throws Exception
	{
		String classQualifiedName = job.getJd().getJavaClassName();
		jqmlogger.debug("Trying to load class " + classQualifiedName);
		@SuppressWarnings("rawtypes")
		Class c = null;
		try
		{
			c = loadClass(classQualifiedName);
		} catch (Exception e)
		{
			jqmlogger.error("Could not load class", e);
			throw e;
		}
		jqmlogger.debug("Class " + classQualifiedName + " was correctly loaded");

		Object o = c.newInstance();

		try
		{
			// Job ID
			Method m = c.getMethod("setJobInstanceID", Integer.class);
			m.invoke(o, job.getId());

			// Start method that we will have to call
			Method start = c.getMethod("start", null);

			// Set parameters
			Method getParameters = c.getMethod("getParameters", null);
			Method getdefaultConnect = c.getMethod("setDefaultConnect", String.class);

			// Injection
			Link l = new Link(old, job.getId(), em);
			Method getMyEngine = c.getMethod("setMyEngine", Object.class);
			getdefaultConnect.invoke(o, defaultConnection);
			getMyEngine.invoke(o, l);

			Map<String, String> params = (Map<String, String>) getParameters.invoke(o, null);
			for (JobParameter i : job.getParameters())
			{
				jqmlogger.debug("Job has parameter " + i.getKey() + " - " + i.getValue());
				params.put(i.getKey(), i.getValue());
			}

			start.invoke(o, null);
		} catch (Exception e)
		{
			jqmlogger.error("Dynamic code error: ", e);
			throw e;
		}
		return o;
	}
}
