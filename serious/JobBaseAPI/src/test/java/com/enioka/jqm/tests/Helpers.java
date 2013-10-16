package com.enioka.jqm.tests;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.tools.CreationTools;

public class Helpers {

	public static com.enioka.jqm.jpamodel.Queue qVip, qNormal, qSlow;
	public static Node node;
	public static DeploymentParameter dpVip, dpNormal, dpSlow;
	
	public static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	
	public static EntityManager getNewEm()
	{
		return emf.createEntityManager();
	}
	
	public static void createLocalNode(EntityManager em)
	{
		Helpers.qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners", 42, 100, em);
		Helpers.qNormal = CreationTools.initQueue("NormalQueue", "Queue for the ordinary job", 7, 100, em);
		Helpers.qSlow = CreationTools.initQueue("SlowQueue", "Queue for the bad guys", 3, 100, em);

		Helpers.node = CreationTools.createNode("localhost", 8081, em);
		Helpers.dpVip = CreationTools.createDeploymentParameter(1, node, 1, 1, qVip, em);
		Helpers.dpNormal = CreationTools.createDeploymentParameter(1, node, 2, 500, qNormal, em);
	}
	
	public static void cleanup(EntityManager em)
	{
		em.getTransaction().begin();
		em.createQuery("DELETE FROM DeploymentParameter").executeUpdate();
		em.createQuery("DELETE FROM Node").executeUpdate();
		em.createQuery("DELETE FROM JobHistoryParameter").executeUpdate();
		em.createQuery("DELETE FROM Message").executeUpdate();
		em.createQuery("DELETE FROM History").executeUpdate();
		em.createQuery("DELETE FROM JobDefParameter").executeUpdate();
		em.createQuery("DELETE FROM JobParameter").executeUpdate();
		em.createQuery("DELETE FROM JobInstance").executeUpdate();
		em.createQuery("DELETE FROM JobDef").executeUpdate();
		em.createQuery("DELETE FROM Queue").executeUpdate();
		em.getTransaction().commit();
	}
	
}
