/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.jndi;

import javax.naming.spi.NamingManager;

import org.apache.log4j.Logger;

public final class JndiContextFactory
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
