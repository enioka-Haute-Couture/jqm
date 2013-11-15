/**
 * Copyright �� 2013 enioka. All rights reserved
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

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Node;

public class TestHelpers
{
	public static Logger jqmlogger = Logger.getLogger(TestHelpers.class);
	public static DatabaseProp db = null;

	public static com.enioka.jqm.jpamodel.Queue qVip, qNormal, qSlow, qVip2, qNormal2, qSlow2, qVip3, qNormal3, qSlow3;
	public static Node node, node2, node3, nodeMix, nodeMix2;

	public static DeploymentParameter dpVip, dpNormal, dpSlow, dpVip2, dpNormal2, dpSlow2, dpVip3, dpNormal3, dpSlow3, dpVipMix, dpVipMix2;

	public static GlobalParameter gpCentral, gpEclipse;

	public static void createLocalNode(EntityManager em)
	{
		db = CreationTools.createDatabaseProp("jdbc/marsu", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdb", "SA", "", em);

		TestHelpers.gpCentral = CreationTools.createGlobalParameter("mavenRepo", "http://repo1.maven.org/maven2/", em);
		TestHelpers.gpCentral = CreationTools.createGlobalParameter("mavenRepo", "http://download.eclipse.org/rt/eclipselink/maven.repo/",
				em);
		TestHelpers.gpCentral = CreationTools.createGlobalParameter("mailSmtp", "smtp.gmail.com", em);
		TestHelpers.gpCentral = CreationTools.createGlobalParameter("mailFrom", "jqm-noreply@gmail.com", em);
		TestHelpers.gpCentral = CreationTools.createGlobalParameter("mailPort", "587", em);
		TestHelpers.gpCentral = CreationTools.createGlobalParameter("defaultConnection", "jdbc/marsu", em);

		TestHelpers.qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners", 42, 100, em);
		TestHelpers.qNormal = CreationTools.initQueue("NormalQueue", "Queue for the ordinary job", 7, 100, em);
		TestHelpers.qSlow = CreationTools.initQueue("SlowQueue", "Queue for the bad guys", 3, 100, em);

		TestHelpers.qVip2 = CreationTools.initQueue("VIPQueue2", "Queue for the winners2", 42, 100, em);
		TestHelpers.qNormal2 = CreationTools.initQueue("NormalQueue2", "Queue for the ordinary job2", 7, 100, em);
		TestHelpers.qSlow2 = CreationTools.initQueue("SlowQueue2", "Queue for the bad guys2", 3, 100, em);

		TestHelpers.qVip3 = CreationTools.initQueue("VIPQueue3", "Queue for the winners3", 42, 100, em);
		TestHelpers.qNormal3 = CreationTools.initQueue("NormalQueue3", "Queue for the ordinary job3", 7, 100, em);
		TestHelpers.qSlow3 = CreationTools.initQueue("SlowQueue3", "Queue for the bad guys3", 3, 100, em);

		TestHelpers.node = CreationTools.createNode("localhost", 8081, "./testprojects/jqm-test-deliverable/", "./testprojects/", em);
		TestHelpers.node2 = CreationTools.createNode("localhost2", 8082, "./testprojects/jqm-test-deliverable/", "./testprojects/", em);
		TestHelpers.node3 = CreationTools.createNode("localhost3", 8083, "./testprojects/jqm-test-deliverable/", "./testprojects/", em);
		TestHelpers.nodeMix = CreationTools.createNode("localhost4", 8084, "./testprojects/jqm-test-deliverable/", "./testprojects/", em);
		TestHelpers.nodeMix2 = CreationTools.createNode("localhost5", 8085, "./testprojects/jqm-test-deliverable/", "./testprojects/", em);

		TestHelpers.dpVip = CreationTools.createDeploymentParameter(1, node, 3, 1, qVip, em);
		TestHelpers.dpVipMix = CreationTools.createDeploymentParameter(2, nodeMix, 3, 1, qVip, em);
		TestHelpers.dpVipMix2 = CreationTools.createDeploymentParameter(2, nodeMix2, 3, 1, qVip, em);
		TestHelpers.dpNormal = CreationTools.createDeploymentParameter(1, node, 2, 300, qNormal, em);
		TestHelpers.dpSlow = CreationTools.createDeploymentParameter(1, node, 1, 1000, qSlow, em);

		TestHelpers.dpVip2 = CreationTools.createDeploymentParameter(1, node2, 3, 100, qVip2, em);
		TestHelpers.dpNormal2 = CreationTools.createDeploymentParameter(1, node2, 2, 300, qNormal2, em);
		TestHelpers.dpSlow2 = CreationTools.createDeploymentParameter(1, node2, 1, 1000, qSlow2, em);

		TestHelpers.dpVip3 = CreationTools.createDeploymentParameter(1, node3, 3, 100, qVip3, em);
		TestHelpers.dpNormal3 = CreationTools.createDeploymentParameter(1, node3, 2, 300, qNormal3, em);
		TestHelpers.dpSlow3 = CreationTools.createDeploymentParameter(1, node3, 1, 1000, qSlow3, em);
	}

	public static void cleanup(EntityManager em)
	{
		em.getTransaction().begin();
		em.createQuery("DELETE GlobalParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE Deliverable WHERE 1=1").executeUpdate();
		em.createQuery("DELETE DeploymentParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobHistoryParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE Message WHERE 1=1").executeUpdate();
		em.createQuery("DELETE History WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobDefParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobInstance WHERE 1=1").executeUpdate();
		em.createQuery("DELETE Node WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobDef WHERE 1=1").executeUpdate();
		em.createQuery("DELETE Queue WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JndiObjectResourceParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JndiObjectResource WHERE 1=1").executeUpdate();
		em.createQuery("DELETE DatabaseProp WHERE 1=1").executeUpdate();
		em.getTransaction().commit();
	}

	public static void printJobInstanceTable()
	{
		EntityManager em = com.enioka.jqm.tools.Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j", JobInstance.class)
				.getResultList();

		for (JobInstance jobInstance : res)
		{
			jqmlogger.debug("==========================================================================================");
			jqmlogger.debug("JobInstance Id: " + jobInstance.getId() + " ---> " + jobInstance.getPosition() + " | "
					+ jobInstance.getState() + " | " + jobInstance.getJd().getId() + " | " + jobInstance.getQueue().getName());
			jqmlogger.debug("==========================================================================================");
		}
	}
}
