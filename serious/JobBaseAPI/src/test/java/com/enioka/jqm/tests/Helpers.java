package com.enioka.jqm.tests;

import java.util.ArrayList;

import javax.persistence.EntityManager;

import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.tools.CreationTools;

public class Helpers
{

	public static DatabaseProp db = null;

	public static com.enioka.jqm.jpamodel.Queue qVip, qNormal, qSlow, qVip2, qNormal2, qSlow2, qVip3, qNormal3, qSlow3;
	public static Node node, node2, node3, nodeMix, nodeMix2;

	public static DeploymentParameter dpVip, dpNormal, dpSlow, dpVip2, dpNormal2, dpSlow2, dpVip3, dpNormal3, dpSlow3, dpVipMix, dpVipMix2;

	public static void createLocalNode(EntityManager em)
	{

		db = CreationTools.createDatabaseProp("jdbc/marsu", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdb", "SA", "", em);

		Helpers.qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners", 42, 100, em);
		Helpers.qNormal = CreationTools.initQueue("NormalQueue", "Queue for the ordinary job", 7, 100, em);
		Helpers.qSlow = CreationTools.initQueue("SlowQueue", "Queue for the bad guys", 3, 100, em);

		Helpers.qVip2 = CreationTools.initQueue("VIPQueue2", "Queue for the winners2", 42, 100, em);
		Helpers.qNormal2 = CreationTools.initQueue("NormalQueue2", "Queue for the ordinary job2", 7, 100, em);
		Helpers.qSlow2 = CreationTools.initQueue("SlowQueue2", "Queue for the bad guys2", 3, 100, em);

		Helpers.qVip3 = CreationTools.initQueue("VIPQueue3", "Queue for the winners3", 42, 100, em);
		Helpers.qNormal3 = CreationTools.initQueue("NormalQueue3", "Queue for the ordinary job3", 7, 100, em);
		Helpers.qSlow3 = CreationTools.initQueue("SlowQueue3", "Queue for the bad guys3", 3, 100, em);

		Helpers.node = CreationTools.createNode("localhost", 8081, "./testprojects/jqm-test-deliverable/", em);
		Helpers.node2 = CreationTools.createNode("localhost2", 8082, "./testprojects/jqm-test-deliverable/", em);
		Helpers.node3 = CreationTools.createNode("localhost3", 8083, "./testprojects/jqm-test-deliverable/", em);
		// Helpers.nodeMix = CreationTools.createNode("localhost4", 8084, "./testprojects/jqm-test-deliverable/", em);
		// Helpers.nodeMix2 = CreationTools.createNode("localhost5", 8085, "./testprojects/jqm-test-deliverable/", em);

		Helpers.dpVip = CreationTools.createDeploymentParameter(1, node, 3, 1, qVip, em);
		// Helpers.dpVipMix = CreationTools.createDeploymentParameter(2, nodeMix, 3, 1, qVip, em);
		// Helpers.dpVipMix2 = CreationTools.createDeploymentParameter(2, nodeMix2, 3, 1, qVip, em);
		Helpers.dpNormal = CreationTools.createDeploymentParameter(1, node, 2, 300, qNormal, em);
		Helpers.dpSlow = CreationTools.createDeploymentParameter(1, node, 1, 1000, qSlow, em);

		Helpers.dpVip2 = CreationTools.createDeploymentParameter(1, node2, 3, 100, qVip2, em);
		Helpers.dpNormal2 = CreationTools.createDeploymentParameter(1, node2, 2, 300, qNormal2, em);
		Helpers.dpSlow2 = CreationTools.createDeploymentParameter(1, node2, 1, 1000, qSlow2, em);

		Helpers.dpVip3 = CreationTools.createDeploymentParameter(1, node3, 3, 100, qVip3, em);
		Helpers.dpNormal3 = CreationTools.createDeploymentParameter(1, node3, 2, 300, qNormal3, em);
		Helpers.dpSlow3 = CreationTools.createDeploymentParameter(1, node3, 1, 1000, qSlow3, em);
	}

	public static void cleanup(EntityManager em)
	{
		em.getTransaction().begin();
		em.createQuery("DELETE Deliverable WHERE 1=1").executeUpdate();
		em.createQuery("DELETE DeploymentParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE Node WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobHistoryParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE Message WHERE 1=1").executeUpdate();
		em.createQuery("DELETE History WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobDefParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobParameter WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobInstance WHERE 1=1").executeUpdate();
		em.createQuery("DELETE JobDef WHERE 1=1").executeUpdate();
		em.createQuery("DELETE Queue WHERE 1=1").executeUpdate();
		em.getTransaction().commit();
	}

	public static void printJobInstanceTable()
	{
		EntityManager em = com.enioka.jqm.tools.Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j", JobInstance.class)
				.getResultList();

		for (JobInstance jobInstance : res)
		{

			System.out.println("==========================================================================================");
			System.out.println("JobInstance Id: " + jobInstance.getId() + " ---> " + jobInstance.getPosition() + " | "
					+ jobInstance.getState() + " | " + jobInstance.getJd().getId() + " | " + jobInstance.getQueue().getName());
			System.out.println("==========================================================================================");
		}
	}
}
