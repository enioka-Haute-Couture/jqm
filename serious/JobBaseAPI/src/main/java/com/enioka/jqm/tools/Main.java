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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.temp.Polling;

public class Main {

	public static ArrayList<DeploymentParameter> dps = new ArrayList<DeploymentParameter>();
	public static ArrayList<Polling> pollers = new ArrayList<Polling>();
	public static Node node = null;
	public static ArrayList<ThreadPool> tps = new ArrayList<ThreadPool>();
	public static AtomicBoolean isRunning = new AtomicBoolean(true);
	public static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	public static EntityManager em = emf.createEntityManager();
	public static EntityTransaction t = em.getTransaction();
	public static Map<String, ClassLoader> cache = new HashMap<String, ClassLoader>();
	public static Server server = null;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// @SuppressWarnings("unused")
		// JndiContext ctx = JndiContextFactory.createJndiContext(db);
		server = new Server(8081);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);

		context.addServlet(new ServletHolder(new Servlet()), "/getfile");

		server.start();

		if (args.length == 1) {

			node = em.createQuery("SELECT n FROM Node n WHERE n.listeningInterface = :li", Node.class).setParameter("li", args[0]).getSingleResult();

			dps = (ArrayList<DeploymentParameter>) em
			        .createQuery("SELECT dp FROM DeploymentParameter dp WHERE dp.node.id = :n", DeploymentParameter.class)
			        .setParameter("n", node.getId()).getResultList();
		}

		for (DeploymentParameter i : dps) {

			Polling p = new Polling(i, cache);
			pollers.add(p);
			Thread t = new Thread(p);
			t.start();
		}
		System.out.println("End of main");
	}

	public static void stop() {

		for (Polling p : pollers) {
			p.stop();
		}
		try {
			server.stop();
		} catch (Exception e) {

		}
	}

	public static void run() {

		for (Polling p : pollers) {
			Thread t = new Thread(p);
			t.start();
		}
	}
}
