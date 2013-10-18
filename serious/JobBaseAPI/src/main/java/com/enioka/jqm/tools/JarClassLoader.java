
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;

import com.enioka.jqm.api.JobBase;
import com.enioka.jqm.jpamodel.JobInstance;

public class JarClassLoader extends URLClassLoader {

	URL jarUrl;

	public JarClassLoader(URL url) {

		super(new URL[]
		{ url });
		this.jarUrl = url;
	}

	private static URL[] addUrls(URL url, URL[] libs) {

		URL[] urls = new URL[libs.length + 1];
		urls[0] = url;
		for (int i = 0; i < libs.length; i++)
			urls[i + 1] = libs[i];
		return urls;
	}

	public JarClassLoader(URL url, URL[] libs) {

		super(addUrls(url, libs));
		this.jarUrl = url;
	}

	public JarClassLoader(URL url, URL[] libs, ClassLoader parent) {

		super(addUrls(url, libs), parent);
		this.jarUrl = url;
	}

	public String getMainClassName() throws IOException {

		URL u = new URL("jar", "", jarUrl + "!/");
		JarURLConnection uc = (JarURLConnection) u.openConnection();
		Attributes attr = uc.getMainAttributes();
		return attr != null ? attr.getValue(Attributes.Name.MAIN_CLASS) : null;
	}

	public void invokeClass(String name, String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

		@SuppressWarnings("rawtypes")
		Class c = loadClass(name);
		@SuppressWarnings("unchecked")
		Method m = c.getMethod("main", new Class[]
		{ args.getClass() });
		m.setAccessible(true);
		int mods = m.getModifiers();
		if (m.getReturnType() != void.class || !Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
			throw new NoSuchMethodException("main");
		}
		try {
			m.invoke(null, new Object[]
			{ args });
		} catch (IllegalAccessException e) {
			// This should not happen, as we have disabled access checks
		}
	}

	public JobBase invokeMain(JobInstance job) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException,
	        InstantiationException, IllegalAccessException {

		// this.invokeClass("Main", new String[]
		// {});
		System.out.println("HHHHHHHHHHHHHHH: " + job.getJd().getJavaClassName());
		Class<? extends JobBase> c = loadClass(job.getJd().getJavaClassName()).asSubclass(JobBase.class);
		System.out.println("IIIIIIIIIIII");
		Object o = c.newInstance();

		JobBase t = (JobBase) o;

		t.setParams(job);
		return t;

	}
}
