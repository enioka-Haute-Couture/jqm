
package com.enioka.jqm.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import com.enioka.jqm.jpamodel.DatabaseProp;

public class JndiContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory {

	DatabaseProp db = null;

	public JndiContext(DatabaseProp db) throws NamingException {

		super();
		this.db = db;
	}

	@Override
	public Object lookup(String name) throws NamingException {

		if (name.equals(db.getName()))
			return new DbDataSource(db.getUrl(), db.getUser(), db.getPwd());

		throw new NameNotFoundException("name " + name + " cannot be found");
	}

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {

		return this;
	}

	@Override
	public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {

		return this;
	}
}
