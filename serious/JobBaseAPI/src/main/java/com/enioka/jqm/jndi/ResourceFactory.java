package com.enioka.jqm.jndi;

// Thanks Apache Tomcat!

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import org.apache.log4j.Logger;

public class ResourceFactory implements ObjectFactory
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
				throw (NamingException) e;
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
