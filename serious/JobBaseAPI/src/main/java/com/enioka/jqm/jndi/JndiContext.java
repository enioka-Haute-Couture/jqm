package com.enioka.jqm.jndi;

import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.StringRefAddr;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JndiObjectResourceParameter;
import com.enioka.jqm.tools.Helpers;

public class JndiContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory
{
	Logger jqmlogger = Logger.getLogger(this.getClass());
	ClassLoader cl = null;
	EntityManager em = null;
	ResourceFactory rf = new ResourceFactory();

	public JndiContext(ClassLoader cl) throws NamingException
	{
		super();
		this.cl = cl;
		this.em = Helpers.getNewEm();
	}

	@Override
	public Object lookup(String name) throws NamingException
	{
		jqmlogger.info("Looking up a JNDI element named " + name);
		String baseCtx = name.split("/")[0];
		// String objName = name.split("/")[1];

		if (baseCtx.equals("jdbc"))
		{
			jqmlogger.debug("JNDI context is database");

			// Base class loader only
			ClassLoader tmp = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(this.cl);

			DatabaseProp db = null;
			try
			{
				db = this.em.createQuery("SELECT d FROM DatabaseProp d WHERE d.name = :n", DatabaseProp.class).setParameter("n", name)
						.getSingleResult();
			} catch (NonUniqueResultException e)
			{
				Thread.currentThread().setContextClassLoader(tmp);
				throw new NameNotFoundException("JNDI name " + name + " cannot be found");
			} catch (NoResultException e)
			{
				Thread.currentThread().setContextClassLoader(tmp);
				throw new NameNotFoundException("JNDI name " + name + " cannot be found");
			}

			try
			{
				Class.forName(db.getDriver());
			} catch (ClassNotFoundException e)
			{
				Thread.currentThread().setContextClassLoader(tmp);
				throw new NameNotFoundException("JDBC driver for JNDI name " + name + " cannot be loaded");
			}

			jqmlogger.info("JNDI element named " + name + " was found.");
			DbDataSource ds = new DbDataSource(db.getUrl(), db.getUser(), db.getPwd());
			Thread.currentThread().setContextClassLoader(tmp);
			return ds;
		}
		else if (baseCtx.equals("jms"))
		{
			// Retrieve the resource description from the database
			JndiObjectResource resource = null;
			try
			{
				jqmlogger.debug("Looking for a JNDI object resource in the database of name " + name);
				resource = em.createQuery("SELECT t FROM JndiObjectResource t WHERE t.name = :name", JndiObjectResource.class)
						.setParameter("name", name).getSingleResult();
			} catch (Exception e)
			{
				jqmlogger.error("Could not find a JNDI object resource of name " + name, e);
				NamingException ex = new NamingException("Could not find a JNDI object resource of name " + name);
				ex.setRootCause(e);
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
				jqmlogger.error("Could not instanciate QCF: ", e);
				NamingException ex = new NamingException(e.getMessage());
				ex.initCause(e);
				throw ex;
			}
		}

		throw new NamingException("Unknown context " + baseCtx);
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
}
