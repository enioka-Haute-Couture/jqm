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

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Queue;

public class JobBaseTest
{
	public static Logger jqmlogger = Logger.getLogger(JobBaseTest.class);
	public static Server s;

	@BeforeClass
	public static void testInit() throws InterruptedException
	{
		s = new Server();
		s.setDatabaseName(0, "testdbengine");
		s.setDatabasePath(0, "mem:testdbengine");
		s.setLogWriter(null);
		s.setSilent(true);
		s.start();

		Dispatcher.resetEM();
		Helpers.resetEmf();
	}

	@AfterClass
	public static void stop()
	{
		Dispatcher.resetEM();
		s.shutdown();
		s.stop();
	}

	@Test
	public void testHighlanderMode() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testHighlanderMode");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		// em.getTransaction().begin();
		//
		// JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
		// .setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();
		//
		// em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId())
		// .executeUpdate();
		//
		// em.getTransaction().commit();

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(8000);
		engine1.stop();

		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC",
				JobInstance.class).getResultList();

		Assert.assertEquals(1, res.size());
		Assert.assertEquals("ENDED", res.get(0).getState());
	}

	@Test
	public void testHighlanderMode2() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testHighlanderMode2");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);
		List<JobParameter> jps = new ArrayList<JobParameter>();

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		em.getTransaction().begin();

		JobInstance j = CreationTools.createJobInstance(jdDemoMaven, jps, "MAG", null, "SUBMITTED", 2, TestHelpers.qVip, null, em);
		JobInstance jj = CreationTools.createJobInstance(jdDemoMaven, jps, "MAG", null, "RUNNING", 1, TestHelpers.qVip, null, em);

		History h = CreationTools.createhistory(null, null, jdDemoMaven, null, TestHelpers.qVip, null, null, j, null, null, null, null,
				TestHelpers.node, null, em);
		History hh = CreationTools.createhistory(null, null, jdDemoMaven, null, TestHelpers.qVip, null, null, jj, null, null, null, null,
				TestHelpers.node, null, em);

		em.getTransaction().commit();

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(2000);
		engine1.stop();

		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC",
				JobInstance.class).getResultList();

		Assert.assertEquals(2, res.size());
		Assert.assertEquals("RUNNING", res.get(0).getState());
		Assert.assertEquals("SUBMITTED", res.get(1).getState());
	}

	@Test
	public void testHighlanderModeMultiQueue() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testHighlanderModeMultiQueue");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(2000);
		engine1.stop();

		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm
				.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :j ORDER BY j.position ASC", JobInstance.class)
				.setParameter("j", jdDemoMaven.getId()).getResultList();

		Assert.assertEquals(1, res.size());
		Assert.assertEquals("ENDED", res.get(0).getState());
	}

	@Test
	public void testGetDeliverables() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testGetDeliverables");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		EntityManager emm = Helpers.getNewEm();
		TestHelpers.createLocalNode(emm);
		EntityManager emmm = Helpers.getNewEm();

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable.txt", em);
		jdargs.add(jdp);
		jdargs.add(jdp2);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/",
				"jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "getDeliverables", null, "Franquin", "ModuleMachin",
				"other", "other", false, em);

		JobDefinition j = new JobDefinition("getDeliverables", "MAG");

		TestHelpers.printJobInstanceTable();

		Dispatcher.enQueue(j);

		TestHelpers.printJobInstanceTable();

		JobInstance ji = emmm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
				.setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(10000);

		File f = new File("./testprojects/jqm-test-deliverable/jqm-test-deliverable.txt");

		Assert.assertEquals(true, f.exists());

		TestHelpers.printJobInstanceTable();

		Dispatcher.resetEM();
		List<InputStream> tmp = Dispatcher.getDeliverables(ji.getId());
		engine1.stop();
		Assert.assertEquals(1, tmp.size());

		File res = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableFamily/" + ji.getId() + "/jqm-test-deliverable.txt");
		Assert.assertEquals(true, res.exists());
		f.delete();
		res.delete();
		File sRep = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableFamily/" + ji.getId());
		sRep.delete();
		File rep = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableFamily/");
		rep.delete();
	}

	@Test
	public void testGetOneDeliverable() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testGetOneDeliverable");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);
		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable42.txt", em);
		jdargs.add(jdp);
		jdargs.add(jdp2);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/",
				"jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "Franquin");

		Dispatcher.enQueue(j);

		JobInstance ji = emm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
				.setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(10000);

		File f = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable42.txt");

		Assert.assertEquals(true, f.exists());

		com.enioka.jqm.api.Deliverable d = new com.enioka.jqm.api.Deliverable("./testprojects/jqm-test-deliverable/",
				"JobGenADeliverable42.txt");

		Dispatcher.resetEM();
		InputStream tmp = Dispatcher.getOneDeliverable(d);
		engine1.stop();
		Assert.assertTrue(tmp.available() > 0);

		File res = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableFamily/" + ji.getId() + "/JobGenADeliverable42.txt");

		Assert.assertEquals(true, res.exists());
		f.delete();
		res.delete();
		File sRep = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableFamily/" + ji.getId());
		sRep.delete();
		File rep = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableFamily/");
		rep.delete();
	}

	@Test
	public void testGetUserDeliverables() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testGetUserDeliverables");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
		JobDefParameter jdpp = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable.txt", em);
		jdargs.add(jdp);
		jdargs.add(jdpp);

		ArrayList<JobDefParameter> jdargs2 = new ArrayList<JobDefParameter>();
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
		JobDefParameter jdpp2 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable2.txt", em);
		jdargs2.add(jdp2);
		jdargs2.add(jdpp2);

		ArrayList<JobDefParameter> jdargs3 = new ArrayList<JobDefParameter>();
		JobDefParameter jdp3 = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
		JobDefParameter jdpp3 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable3.txt", em);
		jdargs3.add(jdp3);
		jdargs3.add(jdpp3);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/",
				"jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven2 = CreationTools.createJobDef(null, true, "App", jdargs2, "jqm-test-deliverable/",
				"jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication2", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven3 = CreationTools.createJobDef(null, true, "App", jdargs3, "jqm-test-deliverable/",
				"jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication3", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j1 = new JobDefinition("MarsuApplication1", "Franquin");
		JobDefinition j2 = new JobDefinition("MarsuApplication2", "Franquin");
		JobDefinition j3 = new JobDefinition("MarsuApplication3", "Franquin");

		Dispatcher.enQueue(j1);
		Dispatcher.enQueue(j2);
		Dispatcher.enQueue(j3);

		TestHelpers.printJobInstanceTable();

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(20000);
		engine1.stop();

		File f1 = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable.txt");
		File f2 = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable2.txt");
		File f3 = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable3.txt");

		Assert.assertEquals(true, f1.exists());
		Assert.assertEquals(true, f2.exists());
		Assert.assertEquals(true, f3.exists());

		TestHelpers.printJobInstanceTable();

		List<Deliverable> tmp = Dispatcher.getUserDeliverables("Franquin");

		Assert.assertEquals(3, tmp.size());
		f1.delete();
		f2.delete();
		f3.delete();
		// Assert.assertEquals(, tmp.get(0).getFilePath());
	}

	@Test
	public void testGetUserJobs() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testGetUserJobs");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		em.getTransaction().begin();

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
				.setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		JobInstance ji2 = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
				.setParameter("myId", 3).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		JobInstance ji3 = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
				.setParameter("myId", 1).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId())
				.executeUpdate();
		em.createQuery("UPDATE JobInstance j SET j.state = 'ENDED' WHERE j.id = :idJob").setParameter("idJob", ji2.getId()).executeUpdate();
		em.createQuery("UPDATE JobInstance j SET j.state = 'RUNNING' WHERE j.id = :idJob").setParameter("idJob", ji3.getId())
				.executeUpdate();

		em.getTransaction().commit();

		em.getTransaction().begin();

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(10000);
		engine1.stop();

		em.getTransaction().commit();

		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC",
				JobInstance.class).getResultList();

		Assert.assertEquals(3, res.size());
	}

	@Test
	public void testGetJobs() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testGetJobs");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		ArrayList<JobDefParameter> jdargs2 = new ArrayList<JobDefParameter>();
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("filepath", "jqm-test-deliverable/", em);
		JobDefParameter jdp3 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable42.txt", em);
		jdargs2.add(jdp2);
		jdargs2.add(jdp3);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven2 = CreationTools.createJobDef(null, true, "App", jdargs, "./testprojects/jqm-test-deliverable/",
				"jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "test", null, "Franquin", "ModuleMachin", "other",
				"other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		JobDefinition j2 = new JobDefinition("test", "Toto");
		Dispatcher.resetEM();

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j2);

		em.getTransaction().begin();

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
				.setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		JobInstance ji2 = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
				.setParameter("myId", 3).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		JobInstance ji3 = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
				.setParameter("myId", 1).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId())
				.executeUpdate();
		em.createQuery("UPDATE JobInstance j SET j.state = 'ENDED' WHERE j.id = :idJob").setParameter("idJob", ji2.getId()).executeUpdate();
		em.createQuery("UPDATE JobInstance j SET j.state = 'RUNNING' WHERE j.id = :idJob").setParameter("idJob", ji3.getId())
				.executeUpdate();

		em.getTransaction().commit();

		em.getTransaction().begin();

		em.getTransaction().commit();

		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC",
				JobInstance.class).getResultList();

		Assert.assertEquals(4, res.size());
	}

	@Test
	public void testGetQueues() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testGetQueues");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<Queue> qs = (ArrayList<Queue>) em.createQuery("SELECT q FROM Queue q", Queue.class).getResultList();

		Assert.assertEquals(9, qs.size());
	}

	@Test
	public void testGoodOrder() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testGoodOrder");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qNormal, 42, "MarsuApplication2", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		@SuppressWarnings("unused")
		JobDef jd2 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qSlow, 42, "MarsuApplication3", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		JobDefinition jj = new JobDefinition("MarsuApplication2", "Franquin");
		JobDefinition jjj = new JobDefinition("MarsuApplication3", "Franquin");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(jj);
		Dispatcher.enQueue(jj);
		Dispatcher.enQueue(jjj);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(10);

		Dispatcher.enQueue(jjj);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(jj);
		Dispatcher.enQueue(j);

		Thread.sleep(5000);
		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j WHERE j.queue = :q ORDER BY j.position ASC",
				JobInstance.class);
		query.setParameter("q", TestHelpers.qVip);
		ArrayList<JobInstance> resVIP = (ArrayList<JobInstance>) query.getResultList();

		TypedQuery<JobInstance> query2 = em.createQuery("SELECT j FROM JobInstance j WHERE j.queue = :q ORDER BY j.position ASC",
				JobInstance.class);
		query2.setParameter("q", TestHelpers.qNormal);
		ArrayList<JobInstance> resNormal = (ArrayList<JobInstance>) query.getResultList();

		TypedQuery<JobInstance> query3 = em.createQuery("SELECT j FROM JobInstance j WHERE j.queue = :q ORDER BY j.position ASC",
				JobInstance.class);
		query3.setParameter("q", TestHelpers.qSlow);
		ArrayList<JobInstance> resSlow = (ArrayList<JobInstance>) query.getResultList();

		for (int i = 0; i < resVIP.size() - 1; i++)
		{
			Assert.assertNotEquals(resVIP.get(i).getPosition(), resVIP.get(i + 1).getPosition());
		}

		for (int i = 0; i < resNormal.size() - 1; i++)
		{
			Assert.assertNotEquals(resNormal.get(i).getPosition(), resNormal.get(i + 1).getPosition());
		}

		for (int i = 0; i < resSlow.size() - 1; i++)
		{
			Assert.assertNotEquals(resSlow.get(i).getPosition(), resSlow.get(i + 1).getPosition());
		}
	}

	@Test
	public void testSecurityDeliverable() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testSecurityDeliverable");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/",
				"jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/",
				"jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qNormal, 42, "MarsuApplication2", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		j.addParameter("filepath", "./testprojects/jqm-test-deliverable/");
		j.addParameter("fileName", "JobGenADeliverableSecurity1.txt");

		JobDefinition jj = new JobDefinition("MarsuApplication2", "Franquin");
		jj.addParameter("filepath", "./testprojects/jqm-test-deliverable/");
		jj.addParameter("fileName", "JobGenADeliverableSecurity2.txt");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(jj);

		TestHelpers.printJobInstanceTable();

		EntityManager emm = Helpers.getNewEm();
		JobInstance ji = emm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
				.setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(12000);

		TestHelpers.printJobInstanceTable();

		File f = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableSecurity1.txt");
		File ff = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableSecurity2.txt");

		Assert.assertEquals(true, f.exists());

		List<Deliverable> tmp = Dispatcher.getUserDeliverables("MAG");
		engine1.stop();

		Assert.assertEquals(1, tmp.size());

		for (Deliverable dd : tmp)
		{
			Assert.assertEquals(ji.getId(), (int) dd.getJobId());
		}
		f.delete();
		ff.delete();
	}

	@Test
	public void testPomError() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testPomError");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/pom_error.xml",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDef jdDemoMaven2 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication2", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		JobDefinition jj = new JobDefinition("MarsuApplication2", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(jj);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(3000);
		engine1.stop();

		JobInstance ji1 = Helpers.getNewEm().createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
				.setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		JobInstance ji2 = Helpers.getNewEm().createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
				.setParameter("myId", jdDemoMaven2.getId()).getSingleResult();

		Assert.assertEquals("CRASHED", ji1.getState());
		Assert.assertEquals("ENDED", ji2.getState());
	}

	@Test
	public void testXmlParser()
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testXmlParser");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		Main.main(new String[] { "localhost", "-xml", "testprojects/jqm-test-xml/xmltest.xml" });

		List<JobDef> jd = em.createQuery("SELECT j FROM JobDef j", JobDef.class).getResultList();

		Assert.assertEquals(2, jd.size());
		Assert.assertEquals("Fibo", jd.get(0).getApplicationName());
		Assert.assertEquals(true, jd.get(0).isCanBeRestarted());
		Assert.assertEquals("com.enioka.jqm.tests.App", jd.get(0).getJavaClassName());
		Assert.assertEquals("jqm-test-fibo/", jd.get(0).getFilePath());
		Assert.assertEquals(TestHelpers.qVip, jd.get(0).getQueue());
		Assert.assertEquals((Integer) 42, jd.get(0).getMaxTimeRunning());
		Assert.assertEquals("ApplicationTest", jd.get(0).getApplication());
		Assert.assertEquals("TestModuleRATONLAVEUR", jd.get(0).getModule());
		Assert.assertEquals(false, jd.get(0).isHighlander());
		Assert.assertEquals("1", jd.get(0).getParameters().get(0).getValue());
		Assert.assertEquals("2", jd.get(0).getParameters().get(1).getValue());

	}

	@Test
	public void testRestartJob() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testRestartJob");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		int i = Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(5000);

		Dispatcher.restartJob(i);

		Thread.sleep(5000);
		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		Assert.assertEquals(2, res.size());
		Assert.assertEquals(jdDemoMaven.getId(), res.get(0).getJd().getId());
		Assert.assertEquals(jdDemoMaven.getId(), res.get(1).getJd().getId());

	}

	@Test
	public void testPomOnlyInJar() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testPomOnlyInJar");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemavennopom/",
				"jqm-test-datetimemavennopom/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		int i = Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(3000);

		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		Assert.assertEquals(1, res.size());
		Assert.assertEquals("ENDED", res.get(0).getState());
	}

	@Test
	public void testEmail() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testSendEmail");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG", "jqm.noreply@gmail.com");

		int i = Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(6000);

		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		Assert.assertEquals(1, res.size());
		Assert.assertEquals("ENDED", res.get(0).getState());
	}

	@Test
	public void testRestartCrashedJob() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testRestartCrashedJob");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		int i = Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(2000);

		Dispatcher.restartCrashedJob(i);

		Thread.sleep(2000);
		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		Assert.assertEquals(2, res.size());
		Assert.assertEquals(jdDemoMaven.getId(), res.get(0).getJd().getId());
		Assert.assertEquals(jdDemoMaven.getId(), res.get(1).getJd().getId());
	}

	@Test
	public void testGetAllDeliverables() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testGetAllDeliverables");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);
		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable42.txt", em);
		jdargs.add(jdp);
		jdargs.add(jdp2);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/",
				"jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "Franquin");

		Dispatcher.enQueue(j);

		JobInstance ji = emm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
				.setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(10000);

		File f = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable42.txt");

		Assert.assertEquals(true, f.exists());

		com.enioka.jqm.api.Deliverable d = new com.enioka.jqm.api.Deliverable("./testprojects/jqm-test-deliverable/",
				"JobGenADeliverable42.txt");

		Dispatcher.resetEM();
		List<com.enioka.jqm.api.Deliverable> tmp = Dispatcher.getAllDeliverables(ji.getId());
		engine1.stop();

		File res = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableFamily/" + ji.getId() + "/JobGenADeliverable42.txt");

		Assert.assertEquals(1, tmp.size());
		Assert.assertEquals(tmp.get(0).getFilePath(), d.getFilePath());
		f.delete();
		res.delete();
		File sRep = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableFamily/" + ji.getId());
		sRep.delete();
		File rep = new File("./testprojects/jqm-test-deliverable/JobGenADeliverableFamily/");
		rep.delete();
	}

	@Test
	public void testSendMsg() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testSendMsg");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);
		boolean success = false;
		boolean success2 = false;
		boolean success3 = false;

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-sendmsg/",
				"jqm-test-sendmsg/jqm-test-sendmsg.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin",
				"other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		int i = Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		Thread.sleep(2000);

		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		ArrayList<Message> m = (ArrayList<Message>) em
				.createQuery("SELECT m FROM Message m WHERE m.history.jobInstance.id = :i", Message.class)
				.setParameter("i", res.get(0).getId()).getResultList();

		Assert.assertEquals(1, res.size());
		Assert.assertEquals("ENDED", res.get(0).getState());

		for (Message msg : m)
		{
			if (msg.getTextMessage().equals("Les marsus sont nos amis, il faut les aimer aussi!"))
				success = true;
			if (msg.getTextMessage().equals("Les marsus sont nos amis, il faut les aimer aussi!2"))
				success2 = true;
			if (msg.getTextMessage().equals("Les marsus sont nos amis, il faut les aimer aussi!3"))
				success3 = true;
		}

		Assert.assertEquals(true, success);
		Assert.assertEquals(true, success2);
		Assert.assertEquals(true, success3);
	}

	@Test
	public void testHistoryFields() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testHistoryFields");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		int i = Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		Thread.sleep(2000);
		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		TestHelpers.printJobInstanceTable();

		History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :i", History.class)
				.setParameter("i", res.get(0).getId()).getSingleResult();

		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		jqmlogger.debug("EnqueueDate: " + df.format(h.getEnqueueDate().getTime()));
		jqmlogger.debug("ReturnedValue: " + h.getReturnedValue());
		jqmlogger.debug("ExecutionDate: " + df.format(h.getExecutionDate().getTime()));
		jqmlogger.debug("EndDate: " + df.format(h.getEndDate().getTime()));

		Assert.assertEquals(1, res.size());
		Assert.assertTrue(h.getEnqueueDate() != null);
		Assert.assertTrue(h.getReturnedValue() != null);
		Assert.assertTrue(h.getUserName() != null);
		Assert.assertTrue(h.getEndDate() != null);
		Assert.assertTrue(h.getExecutionDate() != null);
		Assert.assertTrue(h.getSessionId() != null);
	}

	@Test
	public void testSendProgress() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testSendProgress");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-sendprogress/",
				"jqm-test-sendprogress/jqm-test-sendprogress.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		int i = Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		Thread.sleep(5000);

		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		Assert.assertEquals(1, res.size());
		Assert.assertEquals("ENDED", res.get(0).getState());
		Assert.assertEquals((Integer) 1500, res.get(0).getProgress());
	}

	// @Test
	// public void testKillJob() throws Exception
	// {
	// jqmlogger.debug("**********************************************************");
	// jqmlogger.debug("**********************************************************");
	// jqmlogger.debug("Starting test testKillJob");
	// EntityManager em = Helpers.getNewEm();
	// TestHelpers.cleanup(em);
	// TestHelpers.createLocalNode(em);
	//
	// ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
	// JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
	// jdargs.add(jdp);
	//
	// JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-sendprogress/",
	// "jqm-test-sendprogress/jqm-test-sendprogress.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
	// "ModuleMachin", "other", "other", false, em);
	//
	// JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
	//
	// int i = Dispatcher.enQueue(j);
	// Dispatcher.enQueue(j);
	//
	// TestHelpers.printJobInstanceTable();
	//
	// JqmEngine engine1 = new JqmEngine();
	// engine1.start(new String[] { "localhost" });
	// Thread.sleep(1000);
	//
	// Dispatcher.killJob(i);
	// TestHelpers.printJobInstanceTable();
	//
	// Thread.sleep(5000);
	//
	// engine1.stop();
	//
	// TestHelpers.printJobInstanceTable();
	//
	// TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
	// ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();
	//
	// Assert.assertEquals(2, res.size());
	// Assert.assertEquals("KILLED", res.get(0).getState());
	// Assert.assertEquals("ENDED", res.get(1).getState());
	// }
}
