package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;

public class JndiContextFactory
{

	private JndiContextFactory()
	{

	}

	public static JndiContext createJndiContext() throws Exception
	{

		try
		{
			JndiContext ctx = new JndiContext();

			NamingManager.setInitialContextFactoryBuilder(ctx);
			return ctx;
		} catch (Exception e)
		{
			throw new Exception("could not init Jndi COntext", e);
		}

	}
}
