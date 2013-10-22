package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;

public class JndiContextFactory
{

	private JndiContextFactory()
	{

	}

	public static JndiContext createJndiContext(ClassLoader cl) throws Exception
	{
		try
		{
			JndiContext ctx = new JndiContext(cl);

			NamingManager.setInitialContextFactoryBuilder(ctx);
			return ctx;
		} catch (Exception e)
		{
			throw new Exception("could not init Jndi COntext", e);
		}

	}
}
