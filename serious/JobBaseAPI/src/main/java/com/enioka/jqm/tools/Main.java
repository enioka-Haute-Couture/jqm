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

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.temp.Polling;

public class Main {

	public static DeploymentParameter dp = null;
	public static Node node = null;
	public static Polling p = null;
	public static ArrayList<Queue> queues = new ArrayList<Queue>();

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		// Get the informations about the current node
		// Add no node in base case
		if (args.length == 1) {

			node = CreationTools.em
			        .createQuery(
			                "SELECT n FROM Node n WHERE n.listeningInterface = :li",
			                Node.class).setParameter("li", args[0])
			        .getSingleResult();

			dp = CreationTools.em
			        .createQuery(
			                "SELECT dp FROM DeploymentParameter dp WHERE dp.node = :n",
			                DeploymentParameter.class).setParameter("n", node)
			        .getSingleResult();
		}

		// Get queues

		ArrayList<DeploymentParameter> dps = (ArrayList<DeploymentParameter>) CreationTools.em
		        .createQuery(
		                "SELECT dp FROM DeploymentParameter dp WHERE dp.node.id = :n",
		                DeploymentParameter.class)
		        .setParameter("n", node.getId()).getResultList();

		for (int i = 0; i < dps.size(); i++) {

			queues.add(dps.get(i).getQueue());
		}

		for (Queue q : queues) {

			System.out.println(q.getName());
		}

		int j = 0;

		while (true) {

			while (j < queues.size()) {

				Thread.sleep(dp.getPollingInterval());
				p = new Polling(queues.get(j));

				if (p.getJob() != null) {

					@SuppressWarnings("unused")
					ThreadPool tp = new ThreadPool(p);
				}

				if (j == queues.size() - 1)
					j = 0;
				else
					j++;
			}
		}
	}
}
