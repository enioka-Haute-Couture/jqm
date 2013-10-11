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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.temp.Polling;

public class Main {

	public static ArrayList<DeploymentParameter> dps = new ArrayList<DeploymentParameter>();
	public static Node node = null;
	public static Polling p = null;
	public static ArrayList<ThreadPool> tps = new ArrayList<ThreadPool>();

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Get the informations about the current node
		// Add no node in base case

		Server server = new Server(8081);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);

		context.addServlet(new ServletHolder(new Servlet()), "/getfile");

		server.start();

		if (args.length == 1) {

			node = CreationTools.em.createQuery("SELECT n FROM Node n WHERE n.listeningInterface = :li", Node.class).setParameter("li", args[0])
			        .getSingleResult();

			// dp = (ArrayList<DeploymentParameter>) CreationTools.em
			// .createQuery(
			// "SELECT dp FROM DeploymentParameter dp WHERE dp.node = :n",
			// DeploymentParameter.class).setParameter("n", node)
			// .getResultList();

			dps = (ArrayList<DeploymentParameter>) CreationTools.em
			        .createQuery("SELECT dp FROM DeploymentParameter dp WHERE dp.node.id = :n", DeploymentParameter.class)
			        .setParameter("n", node.getId()).getResultList();
		}

		// for (int i = 0; i < dps.size(); i++) {
		//
		// queues.add(dps.get(i).getQueue());
		// }
		System.out.println("BIP: " + dps.size());
		for (int i = 0; i < dps.size(); i++) {

			tps.add(new ThreadPool(dps.get(i).getQueue(), dps.get(i).getNbThread()));
			System.out.println("BIP: " + dps.get(i).getNbThread());
		}

		int j = 0;

		while (true) {

			while (j < dps.size()) {
				System.out.println(dps.get(j).getPollingInterval());

				Thread.sleep(dps.get(j).getPollingInterval());
				System.out.println("TOTO");

				p = new Polling(dps.get(j).getQueue());
				System.out.println("APRES POLLING");
				for (DeploymentParameter i : dps) {
					System.out.println("DPS QUEUE: " + i.getQueue());
				}

				if (p.getJob() != null) {

					for (int i = 0; i < tps.size(); i++) {

						System.out.println("TPS QUEUE: " + tps.get(i).getQueue().getId());
						System.out.println("POLLING QUEUE: " + p.getJob().get(0).getJd().getQueue().getId());

						if (p.getJob().get(0).getQueue().getId() == tps.get(i).getQueue().getId())
							tps.get(i).run(p);
						System.out.println("APRES THREADPOOL RUN");
					}
				}

				if (j == dps.size() - 1)
					j = 0;
				else
					j++;
			}
		}
	}
}
