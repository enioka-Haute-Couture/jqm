package com.enioka.jqm.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.tools.Main;

public class JndiContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory
{
	Logger jqmlogger = Logger.getLogger(this.getClass());
	ClassLoader cl = null;

	public JndiContext(ClassLoader cl) throws NamingException
	{
		super();
		this.cl = cl;
	}

	@Override
	public Object lookup(String name) throws NamingException
	{
		jqmlogger.info("Looking up a JNDI element named " + name);

		// Base class loader only
		ClassLoader tmp = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.cl);

		DatabaseProp db = null;
		try
		{
			db = Main.em.createQuery("SELECT d FROM DatabaseProp d WHERE d.name = :n", DatabaseProp.class).setParameter("n", name)
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
