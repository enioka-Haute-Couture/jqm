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

package com.enioka.jqm.tools;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.enioka.jqm.jndi.JndiContext;
import com.enioka.jqm.jndi.JndiContextFactory;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.Node;

class JqmEngine
{
	private ArrayList<DeploymentParameter> dps = new ArrayList<DeploymentParameter>();
	private ArrayList<Polling> pollers = new ArrayList<Polling>();
	private Node node = null;
	private EntityManager em = Helpers.getNewEm();
	private Map<String, URL[]> cache = new HashMap<String, URL[]>();
	private Server server = null;
	private JndiContext jndiCtx = null;
	private static Logger jqmlogger = Logger.getLogger(JarClassLoader.class);

	/**
	 * Starts the engine
	 * 
	 * @param args
	 *            - [0] = nodename
	 * @throws Exception
	 */
	public void start(String[] args) throws Exception
	{
		java.lang.System.setProperty("log4j.debug", "true");

		node = em.createQuery("SELECT n FROM Node n WHERE n.listeningInterface = :l", Node.class).setParameter("l", args[0])
				.getSingleResult();

		// JNDI
		if (jndiCtx == null)
		{
			jndiCtx = JndiContextFactory.createJndiContext(Thread.currentThread().getContextClassLoader());
		}

		// Jetty
		server = new Server(node.getPort());
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);

		context.addServlet(new ServletHolder(new Servlet()), "/getfile");

		server.start();

		if (args.length == 1)
		{
			node = em.createQuery("SELECT n FROM Node n WHERE n.listeningInterface = :li", Node.class).setParameter("li", args[0])
					.getSingleResult();

			dps = (ArrayList<DeploymentParameter>) em
					.createQuery("SELECT dp FROM DeploymentParameter dp WHERE dp.node.id = :n", DeploymentParameter.class)
					.setParameter("n", node.getId()).getResultList();
		}

		for (DeploymentParameter i : dps)
		{

			Polling p = new Polling(i, cache);
			pollers.add(p);
			Thread t = new Thread(p);
			t.start();
		}
		jqmlogger.debug("End of main");
	}

	/**
	 * Nicely stops the engine
	 */
	public void stop()
	{

		for (Polling p : pollers)
		{
			p.stop();
		}
		try
		{
			server.stop();
		} catch (Exception e)
		{

		}
	}
}
