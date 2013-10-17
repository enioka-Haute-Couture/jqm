package com.enioka.jqm.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;


public class JndiContext extends InitialContext implements
InitialContextFactoryBuilder, InitialContextFactory {

	public JndiContext() throws NamingException {
		super();
	}

	@Override
	public Object lookup(String name) throws NamingException {
		if (name.equals("jdbc/marsu"))
			return new DbDataSource("jdbc:hsqldb:mem:testdb", "", "");
		if (name.equals("jdbc/pico"))
			return new DbDataSource("jdbc:hsqldb:hsql://localhost/testdb", "SA", "");
		if (name.equals("dialect/pico"))
			return null;

		throw new NameNotFoundException("name " + name + " cannot be found");
	}

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment)
			throws NamingException {
		return this;
	}

	@Override
	public InitialContextFactory createInitialContextFactory(
			Hashtable<?, ?> environment) throws NamingException {
		return this;
	}
}
