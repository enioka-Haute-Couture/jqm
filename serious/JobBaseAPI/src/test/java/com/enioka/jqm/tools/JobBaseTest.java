package com.enioka.jqm.tools;

import java.io.File;
import java.io.InputStream;
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
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;
import com.enioka.jqm.tools.JqmEngine;

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
		jqmlogger.debug("Starting test testHighlanderMode");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"./testprojects/jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		em.getTransaction().begin();

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
				.setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId())
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

		Assert.assertEquals("ENDED", res.get(0).getState());
		Assert.assertEquals("CANCELLED", res.get(1).getState());
		Assert.assertEquals("CANCELLED", res.get(2).getState());
	}

	@Test
	public void testHighlanderModeMultiQueue() throws Exception
	{
		jqmlogger.debug("Starting test testHighlanderModeMultiQueue");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"./testprojects/jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", true, em);

		JobDef jdDemoMaven2 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"./testprojects/jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qNormal, 42, "MarsuApplication2", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		JobDefinition jj = new JobDefinition("MarsuApplication2", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(jj);
		Dispatcher.enQueue(jj);

		em.getTransaction().begin();

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
				.setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId())
				.executeUpdate();

		em.getTransaction().commit();

		em.getTransaction().begin();

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(10000);
		engine1.stop();

		em.getTransaction().commit();

		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm
				.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :j ORDER BY j.position ASC", JobInstance.class)
				.setParameter("j", jdDemoMaven.getId()).getResultList();

		ArrayList<JobInstance> res2 = (ArrayList<JobInstance>) emm
				.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :j ORDER BY j.position ASC", JobInstance.class)
				.setParameter("j", jdDemoMaven2.getId()).getResultList();

		Assert.assertEquals(3, res.size());
		Assert.assertEquals("ENDED", res.get(0).getState());
		Assert.assertEquals("CANCELLED", res.get(1).getState());
		Assert.assertEquals("CANCELLED", res.get(2).getState());

		Assert.assertEquals(2, res2.size());
		Assert.assertEquals("ENDED", res2.get(0).getState());
		Assert.assertEquals("ENDED", res2.get(1).getState());
	}

	@Test
	public void testGetDeliverables() throws Exception
	{
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

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-deliverable/",
				"./testprojects/jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "getDeliverables", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

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
		@SuppressWarnings("unused")
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

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-deliverable/",
				"./testprojects/jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

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
		@SuppressWarnings("unused")
		InputStream tmp = Dispatcher.getOneDeliverable(d);
		engine1.stop();
		Assert.assertTrue(tmp.available() >0);
		
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
		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-deliverable/",
				"./testprojects/jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication1", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven2 = CreationTools.createJobDef(true, "App", jdargs2, "./testprojects/jqm-test-deliverable/",
				"./testprojects/jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication2", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven3 = CreationTools.createJobDef(true, "App", jdargs3, "./testprojects/jqm-test-deliverable/",
				"./testprojects/jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication3", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

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
		jqmlogger.debug("Starting test testGetUserJobs");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"./testprojects/jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", true, em);

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
		jqmlogger.debug("Starting test testGetJobs");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"./testprojects/jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", true, em);

		ArrayList<JobDefParameter> jdargs2 = new ArrayList<JobDefParameter>();
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
		JobDefParameter jdp3 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable42.txt", em);
		jdargs2.add(jdp2);
		jdargs2.add(jdp3);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven2 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-deliverable/",
				"./testprojects/jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "test", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

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
		jqmlogger.debug("Starting test testGoodOrder");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"./testprojects/jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", true, em);

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"./testprojects/jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qNormal, 42, "MarsuApplication2", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", true, em);

		@SuppressWarnings("unused")
		JobDef jd2 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"./testprojects/jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qSlow, 42, "MarsuApplication3", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", true, em);

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
		jqmlogger.debug("Starting test testSecurityDeliverable");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-deliverable/",
				"./testprojects/jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", true, em);

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-deliverable/",
				"./testprojects/jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qNormal, 42, "MarsuApplication2", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", true, em);

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
}
