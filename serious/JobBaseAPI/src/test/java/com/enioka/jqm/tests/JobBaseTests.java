
package com.enioka.jqm.tests;

import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;
import com.enioka.jqm.tools.Main;

public class JobBaseTests {

	Queue qVip = null;
	Queue qNormal = null;
	Queue qSlow = null;

	JobDef jd = null;
	JobDef jdDemoMaven = null;
	JobDef jdDemo = null;

	JobDefParameter jdp = null;

	Node node = null;

	DeploymentParameter dp = null;
	DeploymentParameter dpNormal = null;

	ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

	// public void testInit() {
	//
	// EntityTransaction transac = em.getTransaction();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM DeploymentParameter").executeUpdate();
	// transac.commit();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM Node").executeUpdate();
	// transac.commit();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM JobHistoryParameter").executeUpdate();
	// transac.commit();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM Message").executeUpdate();
	// transac.commit();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM History").executeUpdate();
	// transac.commit();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM JobDefParameter").executeUpdate();
	// transac.commit();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM JobParameter").executeUpdate();
	// transac.commit();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM JobInstance").executeUpdate();
	// transac.commit();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM JobDef").executeUpdate();
	// transac.commit();
	// transac = em.getTransaction();
	// transac.begin();
	// em.createQuery("DELETE FROM Queue").executeUpdate();
	// transac.commit();
	//
	// jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
	// jdargs.add(jdp);
	//
	// this.qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners",
	// 42, 100, em);
	// this.qNormal = CreationTools.initQueue("NormalQueue",
	// "Queue for the ordinary job", 7, 100, em);
	// this.qSlow = CreationTools.initQueue("SlowQueue",
	// "Queue for the bad guys", 3, 100, em);
	//
	// jd = CreationTools.createJobDef(true, "App", jdargs,
	// "./testprojects/PrintArg/", "./testprojects/PrintArg/PrintArg.jar", qVip,
	// 42,
	// "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other",
	// "other", false, em);
	//
	// jdDemoMaven = CreationTools.createJobDef(true, "Main", null,
	// "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/",
	// "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/target/DateTimeMaven-0.0.1-SNAPSHOT.jar",
	// qNormal, 42, "Highlander", 42,
	// "Franquin", "ModuleBidule", "other", "other", "other", true, em);
	//
	// jdDemo = CreationTools.createJobDef(true, "Main", null,
	// "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/",
	// "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/target/DateTimeMaven-0.0.1-SNAPSHOT.jar",
	// qNormal, 42, "MarsuApplication2",
	// 42, "Franquin", "ModuleMachin", "other", "other", "other", true, em);
	//
	// node = CreationTools.createNode("localhost", 8081, em);
	// dp = CreationTools.createDeploymentParameter(1, node, 1, 5, qVip, em);
	// dpNormal = CreationTools.createDeploymentParameter(1, node, 2, 500,
	// qNormal, em);
	//
	// }

	public void printJobInstanceTable() {

		EntityManager em = Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j", JobInstance.class).getResultList();

		for (JobInstance jobInstance : res) {

			System.out.println("==========================================================================================");
			System.out.println("JobInstance Id: " + jobInstance.getId() + " ---> " + jobInstance.getPosition() + " | " + jobInstance.getState()
			        + " | " + jobInstance.getJd().getId() + " | " + jobInstance.getQueue().getName());
			System.out.println("==========================================================================================");
		}
	}

	@Test
	public void testHighlanderMode() throws Exception {

		EntityManager em = Helpers.getNewEm();
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "Main", jdargs, "./testprojects/DateTimeMaven/",
		        "./testprojects/DateTimeMaven/DateTimeMaven.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other",
		        "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		// Dispatcher.enQueue(tmp);
		// Dispatcher.enQueue(tmp);

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
		        .setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		em.getTransaction().begin();

		em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId()).executeUpdate();

		em.getTransaction().commit();

		em.getTransaction().begin();

		printJobInstanceTable();

		Main.main(new String[]
		{ "localhost" });

		Thread.sleep(5000);
		Main.stop();

		em.getTransaction().commit();
		printJobInstanceTable();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) em
		        .createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class).getResultList();

		// Assert.assertEquals("ENDED", res.get(0).getState());
		// // Assert.assertEquals("ENDED", res.get(1).getState());
		// Assert.assertEquals("CANCELLED", res.get(1).getState());
		// Assert.assertEquals("CANCELLED", res.get(2).getState());
		// Assert.assertEquals("CANCELLED", res.get(4).getState());
	}
}
