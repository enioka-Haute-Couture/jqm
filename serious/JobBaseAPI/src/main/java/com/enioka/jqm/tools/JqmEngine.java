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
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.enioka.jqm.jndi.JndiContext;
import com.enioka.jqm.jndi.JndiContextFactory;
import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;

class JqmEngine
{
	private List<DeploymentParameter> dps = new ArrayList<DeploymentParameter>();
	private List<Polling> pollers = new ArrayList<Polling>();
	private Node node = null;
	private EntityManager em = Helpers.getNewEm();
	private Map<String, URL[]> cache = new HashMap<String, URL[]>();
	private Server server = null;
	private JndiContext jndiCtx = null;
	private static Logger jqmlogger = Logger.getLogger(JqmEngine.class);

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

		// Node configuration is in the database
		node = checkAndUpdateNode(args[0]);

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

	Node checkAndUpdateNode(String nodeName)
	{
		em.getTransaction().begin();

		// Node
		Node n = null;
		try
		{
			n = em.createQuery("SELECT n FROM Node n WHERE n.listeningInterface = :l", Node.class).setParameter("l", nodeName)
					.getSingleResult();
			jqmlogger.info("Node " + nodeName + " was found in the configuration");
		} catch (NoResultException e)
		{
			jqmlogger.info("Node " + nodeName + " does not exist in the configuration and will be created with default values");
			n = new Node();
			n.setDlRepo(System.getProperty("user.dir") + "/outputfiles/");
			n.setListeningInterface(nodeName);
			n.setPort(1789);
			n.setRepo(System.getProperty("user.dir") + "/jobs/");
			em.persist(n);
		}

		// Default queue
		Queue q = null;
		long i = (Long) em.createQuery("SELECT COUNT(qu) FROM Queue qu").getSingleResult();
		jqmlogger.info("There are " + i + " queues defined in the database");
		if (i == 0L)
		{
			q = new Queue();
			q.setDefaultQueue(true);
			q.setDescription("default queue");
			q.setMaxTempInQueue(1024);
			q.setMaxTempRunning(1024);
			q.setName("DEFAULT");
			em.persist(q);

			jqmlogger.info("A default queue was created in the configuration");
		}
		else
		{
			try
			{
				q = em.createQuery("SELECT q FROM Queue q WHERE q.defaultQueue = true", Queue.class).getSingleResult();
				jqmlogger.info("Default queue is named " + q.getName());
			} catch (NonUniqueResultException e)
			{
				// Faulty configuration, but why not
				q = em.createQuery("SELECT q FROM Queue q", Queue.class).getResultList().get(0);
				q.setDefaultQueue(true);
				jqmlogger.warn("Queue " + q.getName() + " was modified to become the default queue as there was no default queue");
			} catch (NoResultException e)
			{
				// Faulty configuration, but why not
				q = em.createQuery("SELECT q FROM Queue q", Queue.class).getResultList().get(0);
				q.setDefaultQueue(true);
				jqmlogger.warn("Queue " + q.getName() + " was modified to become the default queue as there was no default queue");
			}
		}

		// GlobalParameter
		GlobalParameter gp = null;
		i = (Long) em.createQuery("SELECT COUNT(gp) FROM GlobalParameter gp WHERE gp.key = :key").setParameter("key", "mavenRepo")
				.getSingleResult();
		if (i == 0)
		{
			gp = new GlobalParameter();

			gp.setKey("mavenRepo");
			gp.setValue("http://repo1.maven.org/maven2/");
			em.persist(gp);

			gp = new GlobalParameter();
			gp.setKey("mavenRepo");
			gp.setValue("http://download.eclipse.org/rt/eclipselink/maven.repo/");
			em.persist(gp);

			jqmlogger.info("This GlobalParameter will allow to download maven resources");
		}
		else
		{
			jqmlogger.info("This GlobalParameter is already exists");
		}

		// Deployment parameter
		DeploymentParameter dp = null;
		i = (Long) em.createQuery("SELECT COUNT(dp) FROM DeploymentParameter dp WHERE dp.node = :localnode").setParameter("localnode", n)
				.getSingleResult();
		if (i == 0)
		{
			dp = new DeploymentParameter();
			dp.setClassId(1);
			dp.setNbThread(5);
			dp.setNode(n);
			dp.setPollingInterval(1000);
			dp.setQueue(q);
			em.persist(dp);
			jqmlogger.info("This node will poll from the default queue with default parameters");
		}
		else
		{
			jqmlogger.info("This node is already configured to take jobs from the default queue");
		}

		// JNDI alias for the JDBC connection to the JQM database
		DatabaseProp localDb = null;
		try
		{
			localDb = (DatabaseProp) em.createQuery("SELECT dp FROM DatabaseProp dp WHERE dp.name = 'jdbc/jqm'").getSingleResult();
			jqmlogger.info("The jdbc/jqm alias already exists and references " + localDb.getUrl());
		} catch (NoResultException e)
		{
			Map<String, Object> props = em.getEntityManagerFactory().getProperties();
			localDb = new DatabaseProp();
			localDb.setDriver((String) props.get("javax.persistence.jdbc.driver"));
			localDb.setName("jdbc/jqm");
			localDb.setPwd((String) props.get("javax.persistence.jdbc.password"));
			localDb.setUrl((String) props.get("javax.persistence.jdbc.url"));
			localDb.setUserName((String) props.get("javax.persistence.jdbc.user"));
			em.persist(localDb);

			jqmlogger.info("A  JNDI alias towards the JQM db has been created. It references: " + localDb.getUrl());
		}

		// Done
		em.getTransaction().commit();
		return n;
	}
}
