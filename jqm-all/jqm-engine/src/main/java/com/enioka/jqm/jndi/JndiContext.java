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

import java.util.Collections;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingException;
import javax.naming.StringRefAddr;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JndiObjectResourceParameter;
import com.enioka.jqm.tools.Helpers;

/**
 * This class implements a basic JNDI context
 * 
 */
public class JndiContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory, NameParser
{
	private static Logger jqmlogger = Logger.getLogger(JndiContext.class);
	private ClassLoader cl = null;
	private ResourceFactory rf = new ResourceFactory();

	/**
	 * Create a new Context
	 * 
	 * @param cl
	 *            the classloader with access to Hibernate & JQM persistence.xml
	 * @throws NamingException
	 */
	public JndiContext(ClassLoader cl) throws NamingException
	{
		super();
		this.cl = cl;
	}

	@Override
	public Object lookup(String name) throws NamingException
	{
		jqmlogger.info("Looking up a JNDI element named " + name);
		String baseCtx = name.split("/")[0];
		ClassLoader tmp = Thread.currentThread().getContextClassLoader();

		if (baseCtx.equals("jdbc"))
		{
			jqmlogger.debug("JNDI context is database");

			// Base class loader only
			Thread.currentThread().setContextClassLoader(this.cl);

			DatabaseProp db = null;
			try
			{
				EntityManager em = Helpers.getNewEm();
				db = em.createQuery("SELECT d FROM DatabaseProp d WHERE d.name = :n", DatabaseProp.class).setParameter("n", name)
				        .getSingleResult();
				em.close();
			} catch (NonUniqueResultException e)
			{
				Thread.currentThread().setContextClassLoader(tmp);
				NameNotFoundException ex = new NameNotFoundException("JNDI name " + name + " cannot be found");
				ex.setRootCause(e);
				throw ex;
			} catch (NoResultException e)
			{
				Thread.currentThread().setContextClassLoader(tmp);
				NameNotFoundException ex = new NameNotFoundException("JNDI name " + name + " cannot be found");
				ex.setRootCause(e);
				throw ex;
			}

			try
			{
				Class.forName(db.getDriver());
			} catch (ClassNotFoundException e)
			{
				Thread.currentThread().setContextClassLoader(tmp);
				NameNotFoundException ex = new NameNotFoundException("JDBC driver for JNDI name " + name + " cannot be loaded");
				ex.setRootCause(e);
				throw ex;
			}

			jqmlogger.info("JNDI element named " + name + " was found.");
			DbDataSource ds = new DbDataSource(db.getUrl(), db.getUserName(), db.getPwd());
			Thread.currentThread().setContextClassLoader(tmp);
			return ds;
		}
		else if (baseCtx.equals("jms") || baseCtx.equals("fs"))
		{
			// Retrieve the resource description from the database
			JndiObjectResource resource = null;
			try
			{
				jqmlogger.debug("Looking for a JNDI object resource in the database of name " + name);
				Thread.currentThread().setContextClassLoader(this.cl);
				EntityManager em = Helpers.getNewEm();
				resource = em.createQuery("SELECT t FROM JndiObjectResource t WHERE t.name = :name", JndiObjectResource.class)
				        .setParameter("name", name).getSingleResult();
				em.close();
				Thread.currentThread().setContextClassLoader(tmp);
			} catch (Exception e)
			{
				jqmlogger.error("Could not find a JNDI object resource of name " + name, e);
				NamingException ex = new NamingException("Could not find a JNDI object resource of name " + name);
				ex.setRootCause(e);
				Thread.currentThread().setContextClassLoader(tmp);
				throw ex;
			}

			// Create the ResourceDescriptor from the JPA object
			JndiResourceDescriptor d = new JndiResourceDescriptor(resource.getType(), resource.getDescription(), null, resource.getAuth(),
			        false, resource.getFactory(), null);
			for (JndiObjectResourceParameter prm : resource.getParameters())
			{
				jqmlogger.debug("Setting property " + prm.getKey() + " - " + prm.getValue());
				d.add(new StringRefAddr(prm.getKey(), prm.getValue()));
			}

			// Create the resource
			try
			{
				Object o = rf.getObjectInstance(d, new CompositeName(baseCtx), this, new Hashtable<String, Object>());
				return o;
			} catch (Exception e)
			{
				jqmlogger.error("Could not instanciate JNDI object resource " + name, e);
				NamingException ex = new NamingException(e.getMessage());
				ex.initCause(e);
				throw ex;
			}
		}

		throw new NamingException("Unknown context " + baseCtx);
	}

	@Override
	public Object lookup(Name name) throws NamingException
	{
		return this.lookup(StringUtils.join(Collections.list(name.getAll()), "/"));
	}

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
	{
		return this;
	}

	@Override
	public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException
	{
		return this;
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException
	{
		return this;
	}

	@Override
	public Name parse(String name) throws NamingException
	{
		return new CompositeName(name);
	}

	@Override
	public void close() throws NamingException
	{
		// Nothing to do.
	}
}
