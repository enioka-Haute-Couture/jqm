
package com.enioka.jqm.tests;

import java.util.ArrayList;

import javax.persistence.EntityTransaction;

import org.junit.Assert;
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

	public void testInit() {

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

		CreationTools.em.createQuery("DELETE FROM JobDefParameter").executeUpdate();
		transac.commit();

		transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM JobParameter").executeUpdate();
		transac.commit();

		transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM JobInstance").executeUpdate();
		transac.commit();

		transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM JobDef").executeUpdate();
		transac.commit();

		transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM Queue").executeUpdate();

		transac.commit();

		jdp = CreationTools.createJobDefParameter("arg", "POUPETTE");
		jdargs.add(jdp);

		this.qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners", 42, 100);
		this.qNormal = CreationTools.initQueue("NormalQueue", "Queue for the ordinary job", 7, 100);
		this.qSlow = CreationTools.initQueue("SlowQueue", "Queue for the bad guys", 3, 100);

		jd = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/PrintArg/", "./testprojects/PrintArg/PrintArg.jar", qVip, 42,
		        "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other", "other", true);

		jdDemoMaven = CreationTools.createJobDef(true, "Main", null, "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/",
		        "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/target/DateTimeMaven-0.0.1-SNAPSHOT.jar", qNormal, 42, "MarsuApplication1",
		        42, "Franquin", "ModuleMachin", "other", "other", "other", true);

		jdDemo = CreationTools.createJobDef(true, "Main", null, "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/",
		        "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/target/DateTimeMaven-0.0.1-SNAPSHOT.jar", qNormal, 42, "MarsuApplication2",
		        42, "Franquin", "ModuleMachin", "other", "other", "other", true);

		node = CreationTools.createNode("localhost", 8081);
		dp = CreationTools.createDeploymentParameter(1, node, 1, 5, qVip);
		dpNormal = CreationTools.createDeploymentParameter(1, node, 2, 500, qNormal);

	}

	@Test
	public void testEnQueue() {

		testInit();

		String[] arg =
		{ "localhost" };

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) CreationTools.em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position",
		        JobInstance.class).getResultList();

		try {
			JobDefinition pouet = new JobDefinition("MarsuApplication");
			pouet.addParameter("ratonlaveur", "patapouf");
			Dispatcher.enQueue(pouet);

			Main.main(arg);
			Main.isRunning.set(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Assert.assertEquals("ENDED", jobs.get(0).getState());
		Assert.assertEquals(jobs.get(0).getJd(), jdDemoMaven);
		Assert.assertEquals(jobs.get(1).getJd(), jdDemo);
		Assert.assertEquals(jobs.get(2).getJd(), jd);
	}

	// @Test
	// public void testChangeQueueIntVersion() {
	//
	// testInit();
	//
	// JobInstance j =
	// CreationTools.em.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job",
	// JobInstance.class)
	// .setParameter("job", jd.getId()).getSingleResult();
	//
	// Dispatcher.changeQueue(j.getId(), qSlow.getId());
	//
	// j =
	// CreationTools.emf.createEntityManager().createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job",
	// JobInstance.class)
	// .setParameter("job", jd.getId()).getSingleResult();
	//
	// Assert.assertEquals(qSlow.getId(), j.getQueue().getId());
	// }
	//
	// @Test
	// public void testChangeQueueQVersion() {
	//
	// testInit();
	//
	// JobInstance j =
	// CreationTools.em.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job",
	// JobInstance.class)
	// .setParameter("job", jd.getId()).getSingleResult();
	//
	// Dispatcher.changeQueue(j.getId(), qSlow.getId());
	//
	// j =
	// CreationTools.emf.createEntityManager().createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job",
	// JobInstance.class)
	// .setParameter("job", jd.getId()).getSingleResult();
	//
	// }
	//
	// @Test
	// public void testDelJobInQueue() {
	//
	// testInit();
	//
	// JobInstance q =
	// CreationTools.em.createQuery("SELECT j FROM JobInstance j, JobDefinition jd WHERE j.jd.id = :job",
	// JobInstance.class)
	// .setParameter("job", jd.getId()).getSingleResult();
	//
	// Dispatcher.delJobInQueue(q.getId());
	//
	// Query tmp =
	// CreationTools.em.createQuery("SELECT j FROM JobInstance j, JobDefinition jd WHERE j.jd.id = :job",
	// JobInstance.class)
	// .setParameter("job", jd.getId());
	//
	// Assert.assertEquals(false, tmp.equals(q));
	// }
	//
	// @Test
	// public void testCancelJobInQueue() {
	//
	// testInit();
	//
	// JobInstance q = CreationTools.emf.createEntityManager()
	// .createQuery("SELECT j FROM JobInstance j, JobDefinition jd WHERE j.jd.id = :job",
	// JobInstance.class).setParameter("job", jd.getId())
	// .getSingleResult();
	//
	// Dispatcher.cancelJobInQueue(q.getId());
	//
	// JobInstance tmp = CreationTools.emf.createEntityManager()
	// .createQuery("SELECT j FROM JobInstance j, JobDefinition jd WHERE j.jd.id = :job",
	// JobInstance.class).setParameter("job", jd.getId())
	// .getSingleResult();
	//
	// Assert.assertEquals("CANCELLED", tmp.getState());
	//
	// }
	//
	// @Test
	// public void testSetPosition() {
	//
	// testInit();
	//
	// JobInstance q = CreationTools.emf.createEntityManager()
	// .createQuery("SELECT j FROM JobInstance j, JobDefinition jd WHERE j.jd.id = :job",
	// JobInstance.class).setParameter("job", jd.getId())
	// .getSingleResult();
	//
	// Dispatcher.setPosition(q.getId(), 1);
	//
	// JobInstance tmp = CreationTools.emf.createEntityManager()
	// .createQuery("SELECT j FROM JobInstance j, JobDefinition jd WHERE j.jd.id = :job",
	// JobInstance.class).setParameter("job", jd.getId())
	// .getSingleResult();
	//
	// Assert.assertEquals(1, (int) tmp.getPosition());
	//
	// }
	//
	// @Test
	// public void testGetUserJobs() {
	//
	// testInit();
	//
	// ArrayList<JobInstance> jobs = (ArrayList<JobInstance>)
	// Dispatcher.getUserJobs("MAG");
	//
	// ArrayList<JobInstance> tmp = (ArrayList<JobInstance>) CreationTools.em
	// .createQuery("SELECT j FROM JobInstance j WHERE j.user = :u",
	// JobInstance.class).setParameter("u", "MAG").getResultList();
	//
	// Assert.assertEquals(tmp.size(), jobs.size());
	//
	// for (int i = 0; i < jobs.size(); i++) {
	//
	// Assert.assertEquals(tmp.get(i), jobs.get(i));
	// }
	// }
	//
	// @Test
	// public void testGetJobs() {
	//
	// testInit();
	//
	// ArrayList<JobInstance> jobs = (ArrayList<JobInstance>)
	// Dispatcher.getJobs();
	//
	// ArrayList<JobInstance> tmp = (ArrayList<JobInstance>)
	// CreationTools.em.createQuery("SELECT j FROM JobInstance j",
	// JobInstance.class)
	// .getResultList();
	//
	// Assert.assertEquals(tmp.size(), jobs.size());
	// Assert.assertEquals(tmp, jobs);
	//
	// }
	//
	// @Test
	// public void testGetQueues() {
	//
	// testInit();
	//
	// ArrayList<Queue> jobs = (ArrayList<Queue>) Dispatcher.getQueues();
	//
	// ArrayList<Queue> tmp = (ArrayList<Queue>)
	// CreationTools.em.createQuery("SELECT j FROM Queue j",
	// Queue.class).getResultList();
	//
	// Assert.assertEquals(tmp.size(), jobs.size());
	// Assert.assertEquals(tmp, jobs);
	//
	// }

	// @Test
	// public void testClose() {
	//
	// CreationTools.close();
	// }
}
