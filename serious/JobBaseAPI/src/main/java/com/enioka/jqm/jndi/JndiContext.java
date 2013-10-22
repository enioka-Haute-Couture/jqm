package com.enioka.jqm.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.tools.Main;

public class JndiContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory
{
	Logger jqmlogger = Logger.getLogger(this.getClass());

	public JndiContext() throws NamingException
	{
		super();
	}

	@Override
	public Object lookup(String name) throws NamingException
	{
		jqmlogger.info("Looking up a JNDI element named " + name);
		DatabaseProp db = Main.em.createNamedQuery("SELECT d FROM DatabaseProp d WHERE d.name = :n", DatabaseProp.class)
				.setParameter("n", name).getSingleResult();

		if (name.equals(db.getName()))
		{
			try
			{
				Class.forName(db.getDriver());
			} catch (ClassNotFoundException e)
			{
			}
			jqmlogger.info("JNDI element named " + name + " was found.");
			return new DbDataSource(db.getUrl(), db.getUser(), db.getPwd());
		}

		throw new NameNotFoundException("name " + name + " cannot be found");
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
