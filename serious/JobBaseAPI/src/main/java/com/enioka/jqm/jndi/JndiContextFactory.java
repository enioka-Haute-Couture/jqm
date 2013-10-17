
package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;

import com.enioka.jqm.jpamodel.DatabaseProp;

public class JndiContextFactory {

	private JndiContextFactory() {

	}

	public static JndiContext createJndiContext(DatabaseProp db) throws Exception {

		try {
			JndiContext ctx = new JndiContext(db);
			Class.forName(db.getDriver());
			NamingManager.setInitialContextFactoryBuilder(ctx);
			return ctx;
		} catch (Exception e) {
			throw new Exception("could not init Jndi COntext", e);
		}

	}
}
