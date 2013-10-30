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

package com.enioka.jqm.api;

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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;

public class TestSuite
{
	static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	static EntityManager em = emf.createEntityManager();
	private static Logger jqmlogger = Logger.getLogger(TestSuite.class);

	Queue qVip = null;
	Queue qNormal = null;
	Queue qSlow = null;

	JobDef jd = null;
	JobDef jdDemoMaven = null;
	JobDef jdDemo = null;

	Node node = null;

	DeploymentParameter dp = null;
	DeploymentParameter dpNormal = null;

	@BeforeClass
	public static void before()
	{
		java.lang.System.setProperty("log4j.debug", "true");
		// Db init
		em = Dispatcher.getEm();
		emf = em.getEntityManagerFactory();
	}

	public void testInit()
	{
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

		this.qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners", 42, 100, em);
		this.qNormal = CreationTools.initQueue("NormalQueue", "Queue for the ordinary job", 7, 100, em);
		this.qSlow = CreationTools.initQueue("SlowQueue", "Queue for the bad guys", 0, 100, em);

		this.jd = CreationTools.createJobDef(true, "App", null, "/Users/pico/Dropbox/projets/enioka/jqm/tests/PrintArg/",
				"/Users/pico/Dropbox/projets/enioka/jqm/tests/PrintArg/target/PrintArg-0.0.1-SNAPSHOT.jar", qVip, 42, "MarsuApplication", null,
				"Franquin", "ModuleMachin", "other", "other", "other", true, em);

		this.jdDemoMaven = CreationTools.createJobDef(true, "DemoMavenClassName", null, "jqm-test-datetimemaven/pom.xml", "", qNormal, 42,
				"MarsuApplication2", null, "Franquin", "ModuleMachin", "other", "other", "other", true, em);

		this.jdDemo = CreationTools.createJobDef(true, "DemoClassName", null, "./testprojects/jqm-test-datetimemaven/", "", qSlow, 42,
				"MarsuApplication3", null, "Franquin", "ModuleMachin", "other", "other", "other", true, em);

		node = CreationTools.createNode("localhost", 8081, "../JobBaseAPI/testprojects/jqm-test-deliverable/", "./testprojects/", em);

		dp = CreationTools.createDeploymentParameter(1, node, 1, 5, qVip, em);
		dpNormal = CreationTools.createDeploymentParameter(1, node, 2, 500, qNormal, em);

	}

	@Test
	public void testEnQueue()
	{
		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");
		final JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2", "MAG");
		final JobDefinition jdDemo = new JobDefinition("MarsuApplication3", "MAG");

		Dispatcher.enQueue(jd);
		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);

		final ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position",
				JobInstance.class).getResultList();

		Assert.assertEquals(jobs.size(), 3);
		Assert.assertEquals(jobs.get(0).getJd().getId(), this.jd.getId());
		Assert.assertEquals(jobs.get(1).getJd(), this.jdDemoMaven);
		Assert.assertEquals(jobs.get(2).getJd(), this.jdDemo);
	}

	@Test
	public void testChangeQueueIntVersion()
	{

		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");
		final JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2", "MAG");
		final JobDefinition jdDemo = new JobDefinition("MarsuApplication3", "MAG");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		JobInstance j = em.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job", JobInstance.class)
				.setParameter("job", this.jd.getId()).getSingleResult();

		Dispatcher.changeQueue(j.getId(), qSlow.getId());

		j = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job", JobInstance.class)
				.setParameter("job", this.jd.getId()).getSingleResult();

		Assert.assertEquals(qSlow.getId(), j.getQueue().getId());
	}

	@Test
	public void testChangeQueueQVersion()
	{

		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");
		final JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2", "MAG");
		final JobDefinition jdDemo = new JobDefinition("MarsuApplication3", "MAG");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		JobInstance j = em.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job", JobInstance.class)
				.setParameter("job", this.jd.getId()).getSingleResult();

		Dispatcher.changeQueue(j.getId(), qSlow.getId());

		j = emf.createEntityManager().createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :job", JobInstance.class)
				.setParameter("job", this.jd.getId()).getSingleResult();

	}

	@Test
	public void testDelJobInQueue()
	{

		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");
		final JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2", "MAG");
		final JobDefinition jdDemo = new JobDefinition("MarsuApplication3", "MAG");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		final JobInstance q = em.createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job", JobInstance.class)
				.setParameter("job", this.jd.getId()).getSingleResult();

		Dispatcher.delJobInQueue(this.jd.getId() + 3);

		final Query tmp = emf.createEntityManager()
				.createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job", JobInstance.class)
				.setParameter("job", this.jd.getId());
		Assert.assertEquals(false, tmp.equals(q));
	}

	@Test
	public void testCancelJobInQueue()
	{
		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");
		final JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2", "MAG");
		final JobDefinition jdDemo = new JobDefinition("MarsuApplication3", "MAG");

		jqmlogger.debug("Enqueing elements");
		int i1 = Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);
		jqmlogger.debug("The JI to sto pis id: " + i1);

		// JobInstance q = emf.createEntityManager().find(JobInstance.class, i1);
		Dispatcher.cancelJobInQueue(i1);
		JobInstance tmp = Dispatcher.getEm().find(JobInstance.class, i1);
		Assert.assertEquals("CANCELLED", tmp.getState());
	}

	@Test
	public void testSetPosition()
	{
		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");
		final JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2", "MAG");
		final JobDefinition jdDemo = new JobDefinition("MarsuApplication3", "MAG");

		Dispatcher.enQueue(jdDemoMaven);
		int i = Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);
		Dispatcher.enQueue(jd);
		Dispatcher.enQueue(jd);

		//final JobInstance q = Dispatcher.getEm().find(JobInstance.class, i);

		Dispatcher.setPosition(i, 1);

		final JobInstance tmp = Dispatcher.getEm().find(JobInstance.class, i);
		Assert.assertEquals(1, (int) tmp.getPosition());
	}

	@Test
	public void testGetUserJobs()
	{

		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");
		final JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2", "MAG");
		final JobDefinition jdDemo = new JobDefinition("MarsuApplication3", "MAG");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		final ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) Dispatcher.getUserJobs("MAG");
		final ArrayList<JobInstance> tmp = (ArrayList<JobInstance>) em
				.createQuery("SELECT j FROM JobInstance j WHERE j.userName = :u", JobInstance.class).setParameter("u", "MAG")
				.getResultList();
		Assert.assertEquals(tmp.size(), jobs.size());

		for (int i = 0; i < jobs.size(); i++)
		{

			Assert.assertEquals(tmp.get(i).getId(), jobs.get(i).getId());
		}
	}

	@Test
	public void testGetQueues()
	{
		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");
		final JobDefinition jdDemoMaven = new JobDefinition("MarsuApplication2", "MAG");
		final JobDefinition jdDemo = new JobDefinition("MarsuApplication3", "MAG");

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);

		final ArrayList<com.enioka.jqm.api.Queue> jobs = (ArrayList<com.enioka.jqm.api.Queue>) Dispatcher.getQueues();

		final ArrayList<Queue> tmp = (ArrayList<Queue>) em.createQuery("SELECT j FROM Queue j", Queue.class).getResultList();
		Assert.assertEquals(tmp.size(), jobs.size());

		for (int i = 0; i < jobs.size(); i++)
		{
			Assert.assertEquals(tmp.get(i).getId(), jobs.get(i).getId());
		}
	}

	// @Test
	public void testGetDeliverables()
	{
		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(jd);

		final JobInstance job = emf.createEntityManager()
				.createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job", JobInstance.class)
				.setParameter("job", this.jd.getId()).getSingleResult();
		final File file = new File("/Users/pico/Downloads/tests/deliverable" + job.getId());

		try
		{
			System.out.println("TOTO");
			Thread.sleep(2000);
			Dispatcher.getDeliverables(job.getId());
		} catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(true, file.exists());
	}

	// @Test
	public void testOverloadArgument()
	{
		testInit();

		final JobDefinition jd = new JobDefinition("MarsuApplication", "MAG");
		jd.addParameter("key", "value");

		Dispatcher.enQueue(jd);

		final JobInstance job = emf.createEntityManager()
				.createQuery("SELECT j FROM JobInstance j, JobDef jd WHERE j.jd.id = :job", JobInstance.class)
				.setParameter("job", this.jd.getId()).getSingleResult();

		final Query q = emf.createEntityManager().createQuery("SELECT j.parameters FROM JobInstance AS j WHERE j.id = :j")
				.setParameter("j", job.getId());

		@SuppressWarnings("unchecked")
		final List<JobParameter> res = q.getResultList();

		Assert.assertEquals("key", res.get(0).getKey());
		Assert.assertEquals("value", res.get(0).getValue());
	}
}
