package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;

import org.apache.log4j.Logger;

public class JndiContextFactory
{
	private static Logger jqmlogger = Logger.getLogger(JndiContextFactory.class);

	private JndiContextFactory()
	{

	}

	/**
	 * Will create a JNDI Context and register it as the initial context factory builder
	 * 
	 * @param cl
	 *            a classloader with visibility on the JPA files
	 * @return the context
	 * @throws Exception
	 */
	public static JndiContext createJndiContext(ClassLoader cl) throws Exception
	{
		try
		{
			JndiContext ctx = new JndiContext(cl);
			if (!NamingManager.hasInitialContextFactoryBuilder())
			{
				NamingManager.setInitialContextFactoryBuilder(ctx);
			}
			return ctx;
		} catch (Exception e)
		{
			jqmlogger.error("Could not create JNDI context: " + e.getMessage());
			throw new Exception("could not init Jndi Context", e);
		}

	}
}
