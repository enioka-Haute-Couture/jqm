package com.enioka.jqm.test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;



public class TestSuite {

	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	EntityManager em = emf.createEntityManager();

	Queue qVip = null;
	Queue qNormal = null;
	Queue qSlow = null;

	JobDef jd = null;
	JobDef jdDemoMaven = null;
	JobDef jdDemo = null;

	Node node = null;

	DeploymentParameter dp = null;
	DeploymentParameter dpNormal = null;

	public void testInit() {

		EntityTransaction transac = em.getTransaction();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM DeploymentParameter").executeUpdate();
		transac.commit();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM Node").executeUpdate();
		transac.commit();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM JobHistoryParameter").executeUpdate();
		transac.commit();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM Message").executeUpdate();
		transac.commit();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM History").executeUpdate();
		transac.commit();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM JobDefParameter").executeUpdate();
		transac.commit();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM JobParameter").executeUpdate();
		transac.commit();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM JobInstance").executeUpdate();
		transac.commit();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM JobDef").executeUpdate();
		transac.commit();
		transac = em.getTransaction();
		transac.begin();
		em.createQuery("DELETE FROM Queue").executeUpdate();
		transac.commit();

		this.qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners", 42 , 100, em);
		this.qNormal = CreationTools.initQueue("NormalQueue", "Queue for the ordinary job", 7 , 100, em);
		this.qSlow = CreationTools.initQueue("SlowQueue", "Queue for the bad guys", 0 , 100, em);

		this.jd = CreationTools.createJobDef(true, "App", null, "/Users/pico/Dropbox/projets/enioka/jqm/tests/PrintArg/",
				"/Users/pico/Dropbox/projets/enioka/jqm/tests/PrintArg/target/PrintArg-0.0.1-SNAPSHOT.jar", qVip,
				42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other", "other", true, em);

		this.jdDemoMaven = CreationTools.createJobDef(true, "DemoMavenClassName", null, "./testprojects/DateTimeMaven/", "", qNormal,
				42, "MarsuApplication2", 42, "Franquin", "ModuleMachin", "other", "other", "other", true, em);

		this.jdDemo = CreationTools.createJobDef(true, "DemoClassName", null, "./testprojects/DateTimeMaven/", "", qSlow,
				42, "MarsuApplication3", 42, "Franquin", "ModuleMachin", "other", "other", "other", true, em);

		node = CreationTools.createNode("localhost", 8081, em);

		dp = CreationTools.createDeploymentParameter(1, node, 1, 5, qVip, em);
		dpNormal = CreationTools.createDeploymentParameter(1, node, 2, 500, qNormal, em);

	}

	@Test
	public void testEnQueue() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2");
		JobDefinition jdDemo = new JobDefinition("MarsuApplication3");

		Dispatcher.enQueue(jd);
		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position",
				JobInstance.class).getResultList();

		Assert.assertEquals(jobs.size(), 3);
		Assert.assertEquals(jobs.get(0).getJd().getId(), this.jd.getId());
		Assert.assertEquals(jobs.get(1).getJd(), this.jdDemoMaven);
		Assert.assertEquals(jobs.get(2).getJd(), this.jdDemo);
	}

	@Test
	public void testChangeQueueIntVersion() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2");
		JobDefinition jdDemo = new JobDefinition("MarsuApplication3");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		JobInstance j = em.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

		Dispatcher.changeQueue(j.getId(), qSlow.getId());

		j = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

		Assert.assertEquals(qSlow.getId(), j.getQueue().getId());
	}

	@Test
	public void testChangeQueueQVersion() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2");
		JobDefinition jdDemo = new JobDefinition("MarsuApplication3");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		JobInstance j = em.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

		Dispatcher.changeQueue(j.getId(), qSlow.getId());

		j = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

	}

	@Test
	public void testDelJobInQueue() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2");
		JobDefinition jdDemo = new JobDefinition("MarsuApplication3");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		JobInstance q = em.createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job", JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

		Dispatcher.delJobInQueue(this.jd.getId() + 3);

		Query tmp = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job", JobInstance.class).setParameter("job", this.jd.getId());

		Assert.assertEquals(false, tmp.equals(q));
	}

	@Test
	public void testCancelJobInQueue() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2");
		JobDefinition jdDemo = new JobDefinition("MarsuApplication3");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		JobInstance q = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

		Dispatcher.cancelJobInQueue(q.getId());

		JobInstance tmp = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

		Assert.assertEquals("CANCELLED", tmp.getState());

	}

	@Test
	public void testSetPosition() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2");
		JobDefinition jdDemo = new JobDefinition("MarsuApplication3");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);
		Dispatcher.enQueue(jd);
		Dispatcher.enQueue(jd);

		JobInstance q = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

		Dispatcher.setPosition(q.getId(), 1);

		JobInstance tmp = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

		Assert.assertEquals(1, (int)tmp.getPosition());

	}

	@Test
	public void testGetUserJobs() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2");
		JobDefinition jdDemo = new JobDefinition("MarsuApplication3");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) Dispatcher.getUserJobs("MAG");

		ArrayList<JobInstance> tmp = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j WHERE j.user = :u",
				JobInstance.class).setParameter("u", "MAG").getResultList();

		Assert.assertEquals(tmp.size(), jobs.size());

		for (int i = 0; i < jobs.size(); i++) {

			Assert.assertEquals(tmp.get(i).getId(), jobs.get(i).getId());
        }
	}

	@Test
	public void testGetJobs() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2");
		JobDefinition jdDemo = new JobDefinition("MarsuApplication3");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		ArrayList<com.enioka.jqm.api.JobInstance> jobs = (ArrayList<com.enioka.jqm.api.JobInstance>) Dispatcher.getJobs();

		for (com.enioka.jqm.api.JobInstance jobInstance : jobs) {

			System.out.println("job: " + jobInstance.getId());
        }

		ArrayList<JobInstance> tmp = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j",
				JobInstance.class).getResultList();

		for (JobInstance j : tmp) {

			System.out.println("jobInstance: " + j.getId());
        }

		Assert.assertEquals(tmp.size(), jobs.size());

		for (int i = 0; i < jobs.size(); i++) {

			Assert.assertEquals(tmp.get(i).getId(), (int)jobs.get(i).getId());
		}
	}

	@Test
	public void testGetQueues() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2");
		JobDefinition jdDemo = new JobDefinition("MarsuApplication3");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		ArrayList<com.enioka.jqm.api.Queue> jobs = (ArrayList<com.enioka.jqm.api.Queue>) Dispatcher.getQueues();

		ArrayList<Queue> tmp = (ArrayList<Queue>) em.createQuery("SELECT j FROM Queue j",
				Queue.class).getResultList();

		Assert.assertEquals(tmp.size(), jobs.size());

		for (int i = 0; i < jobs.size(); i++) {

			Assert.assertEquals(tmp.get(i).getId(), jobs.get(i).getId());
		}
	}

	//@Test
	public void testGetDeliverables(){

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");

		Dispatcher.enQueue(jd);

		JobInstance job = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

		File file = new File("/Users/pico/Downloads/tests/deliverable" + job.getId());

		try {
			System.out.println("TOTO");
			Thread.sleep(2000);
	        Dispatcher.getDeliverables(job.getId());
        } catch (NoSuchAlgorithmException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }

		Assert.assertEquals(true, file.exists());

	}

	@Test
	public void testOverloadArgument() {

		testInit();

		JobDefinition jd = new JobDefinition("MarsuApplication");
		jd.addParameter("key", "value");

		Dispatcher.enQueue(jd);

		JobInstance job = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job",
				JobInstance.class).setParameter("job", this.jd.getId()).getSingleResult();

        Query q = emf.createEntityManager().createQuery(
				"SELECT j.parameters FROM JobInstance AS j WHERE j.id = :j")
				.setParameter("j", job.getId());

        @SuppressWarnings("unchecked")
        List<JobParameter> res = q.getResultList();

		Assert.assertEquals("key", res.get(0).getKey());
		Assert.assertEquals("value", res.get(0).getValue());
	}

//	@Test
//	public void testClose() {
//
//		CreationTools.close();
//	}
}
