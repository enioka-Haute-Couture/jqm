package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;


public class JndiContextFactory {

	private JndiContextFactory() {}

	public static JndiContext createJndiContext(String databaseDriver) throws Exception
	{
		try
		{
			JndiContext ctx = new JndiContext();
			Class.forName(databaseDriver);
			NamingManager.setInitialContextFactoryBuilder(ctx);
			return ctx;
		}
		catch (Exception e)
		{
			throw new Exception("could not init Jndi COntext", e);
		}

	}
}
