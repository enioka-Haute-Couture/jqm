/**
 * Copyright � 2013 enioka. All rights reserved
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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.persistence.EntityTransaction;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.JobDefinition;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;



public class Main
{

	static DeploymentParameter dp = null;
	static DeploymentParameter dpNormal = null;
	static Node node = null;

	static Queue qVip = null;
	static Queue qNormal = null;
	static Queue qSlow = null;

//	static JobParameter jp = null;
//	static JobParameter jpd = null;
//	static JobParameter jpdm = null;

	static JobDefinition jd = null;

	static JobDefinition jdDemoMaven = null;

	static JobDefinition jdDemo = null;

	/**
	 * @param args
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();
		CreationTools.em.createQuery("DELETE FROM Message").executeUpdate();
		transac.commit();
		transac = CreationTools.em.getTransaction();
		transac.begin();
		CreationTools.em.createQuery("DELETE FROM DeploymentParameter").executeUpdate();
		transac.commit();
		transac = CreationTools.em.getTransaction();
		transac.begin();
		CreationTools.em.createQuery("DELETE FROM Node").executeUpdate();
		transac.commit();
		transac = CreationTools.em.getTransaction();
		transac.begin();
		CreationTools.em.createQuery("DELETE FROM History").executeUpdate();
		transac.commit();
		transac = CreationTools.em.getTransaction();
		transac.begin();
		CreationTools.em.createQuery("DELETE FROM JobInstance").executeUpdate();
		transac.commit();
		transac = CreationTools.em.getTransaction();
		transac.begin();
		CreationTools.em.createQuery("DELETE FROM JobParameter").executeUpdate();
		transac.commit();
		transac = CreationTools.em.getTransaction();
		transac.begin();
		CreationTools.em.createQuery("DELETE FROM JobDefinition").executeUpdate();
		transac.commit();
		transac = CreationTools.em.getTransaction();
		transac.begin();
		CreationTools.em.createQuery("DELETE FROM Queue").executeUpdate();
		transac.commit();

		qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners", 42 , 100);
		qNormal = CreationTools.initQueue("NormalQueue", "Queue for the ordinary job", 7 , 100);
		qSlow = CreationTools.initQueue("SlowQueue", "Queue for the bad guys", 3 , 100);

		jd = CreationTools.createJobDefinition(true, "Main", "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/", qVip,
				42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other", "other", true);
//
//
		jdDemoMaven = CreationTools.createJobDefinition(true, "Main", "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/", qNormal,
				42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other", "other", true);


		jdDemo = CreationTools.createJobDefinition(true, "Main", "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/", qNormal,
				42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other", "other", true);
//
////		jp = CreationTools.createJobParameter("arg1", "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/target/DateTimeMaven-0.0.1-SNAPSHOT.jar", jd);
////		jpdm = CreationTools.createJobParameter("", "", jdDemoMaven);
////		jpd = CreationTools.createJobParameter("", "", jdDemo);
//
		node = CreationTools.createNode("localhost", 8081);
//
		dp = CreationTools.createDeploymentParameter(1, node, 1, 5, qVip);
		dpNormal = CreationTools.createDeploymentParameter(1, node, 2, 500, qNormal);
//
		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);
////		Dispatcher.enQueue(jd);
////		Dispatcher.enQueue(jd);
////		Dispatcher.getDeliverables(499);
		CreationTools.close();
	}

}
