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

package com.enioka.jqm.jndi;

// Thanks Apache Tomcat!

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import org.apache.log4j.Logger;

class ResourceFactory implements ObjectFactory
{
	private static Logger jqmlogger = Logger.getLogger(ResourceFactory.class);

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
	{
		// What kind of resource should we create?
		JndiResourceDescriptor resource = (JndiResourceDescriptor) obj;

		// Get class loader, we'll need it to load the factory class
		ClassLoader tcl = Thread.currentThread().getContextClassLoader();
		Class<?> factoryClass = null;
		ObjectFactory factory = null;

		try
		{
			jqmlogger.debug("Attempting to load ObjectFactory class " + resource.getFactoryClassName());
			factoryClass = tcl.loadClass(resource.getFactoryClassName());
			jqmlogger.debug("ObjectFactory class was successfully loaded: " + resource.getFactoryClassName());
		} catch (ClassNotFoundException e)
		{
			NamingException ex = new NamingException("Could not find resource or resource factory class in the classpath");
			ex.initCause(e);
			throw ex;
		} catch (Exception e)
		{
			NamingException ex = new NamingException("Could not load resource or resource factory class for an unknown reason");
			ex.initCause(e);
			throw ex;
		}

		try
		{
			jqmlogger.debug("Creating ObjectFactory instance");
			factory = (ObjectFactory) factoryClass.newInstance();
			jqmlogger.debug("Factory was successfully created");
		} catch (Exception e)
		{
			if (e instanceof NamingException)
			{
				throw (NamingException) e;
			}
			NamingException ex = new NamingException("Could not create resource factory instance");
			ex.initCause(e);
			throw ex;
		}

		Object result = null;
		try
		{
			jqmlogger.debug("Creating Object instance from ObjectFactory instance");
			result = factory.getObjectInstance(obj, name, nameCtx, environment);
			jqmlogger.debug("Object was successfully created");
		} catch (Exception e)
		{
			NamingException ex = new NamingException(
					"Could not create object resource from resource factory. JNDI definition & parameters may be incorrect.");
			ex.initCause(e);
			throw ex;
		}
		return result;
	}

}
