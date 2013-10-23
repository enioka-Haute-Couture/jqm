package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;

import org.apache.log4j.Logger;

public class JndiContextFactory
{
	private static Logger jqmlogger = Logger.getLogger(JndiContextFactory.class);

	private JndiContextFactory()
	{

	}

	public static JndiContext createJndiContext(ClassLoader cl) throws Exception
	{
		try
		{
			JndiContext ctx = new JndiContext(cl);
			if (!NamingManager.hasInitialContextFactoryBuilder())
				NamingManager.setInitialContextFactoryBuilder(ctx);
			return ctx;
		} catch (Exception e)
		{
			jqmlogger.error("Could not create JNDI context: " + e.getMessage());
			throw new Exception("could not init Jndi Context", e);
		}

	}
}
