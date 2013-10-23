package com.enioka.jqm.tests;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

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
import com.enioka.jqm.tools.Main;

public class JobBaseTests
{
	public static Server s;

	@BeforeClass
	public static void testInit()
	{
		s = new Server();
		s.setDatabaseName(0, "testdbengine");
		s.setDatabasePath(0, "mem:testdbengine");
		s.setLogWriter(null);
		s.setSilent(true);
		s.start();
	}

	@AfterClass
	public static void stop()
	{
		s.stop();
	}

	@Test
	public void testHighlanderMode() throws Exception
	{

		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/DateTimeMaven/",
				"./testprojects/DateTimeMaven/DateTimeMaven.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", true, em);

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

		Main.main(new String[] { "localhost" });

		Thread.sleep(10000);
		Main.stop();

		em.getTransaction().commit();

		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC",
				JobInstance.class).getResultList();

		Assert.assertEquals("ENDED", res.get(0).getState());
		Assert.assertEquals("CANCELLED", res.get(1).getState());
		Assert.assertEquals("CANCELLED", res.get(2).getState());
	}

	@Test
	public void testGetDeliverables() throws Exception
	{

		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		EntityManager emm = Helpers.getNewEm();
		Helpers.createLocalNode(emm);
		EntityManager emmm = Helpers.getNewEm();

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/JobGenADeliverable/", em);
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable.txt", em);
		jdargs.add(jdp);
		jdargs.add(jdp2);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/JobGenADeliverable/",
				"./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "getDeliverables", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("getDeliverables", "MAG");

		Helpers.printJobInstanceTable();

		Dispatcher.enQueue(j);

		Helpers.printJobInstanceTable();

		JobInstance ji = emmm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
				.setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		Main.main(new String[] { "localhost" });

		Thread.sleep(10000);

		File f = new File("./testprojects/JobGenADeliverable/JobGenADeliverable.txt");

		Assert.assertEquals(true, f.exists());

		Helpers.printJobInstanceTable();

		@SuppressWarnings("unused")
		List<InputStream> tmp = Dispatcher.getDeliverables(ji.getId());
		Main.stop();

		File res = new File("./testprojects/JobGenADeliverable/JobGenADeliverableFamily/" + ji.getId() + "/JobGenADeliverable.txt");

		Assert.assertEquals(true, res.exists());
	}

	@Test
	public void testGetOneDeliverable() throws Exception
	{

		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);
		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/JobGenADeliverable/", em);
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable42.txt", em);
		jdargs.add(jdp);
		jdargs.add(jdp2);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/JobGenADeliverable/",
				"./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "Franquin");

		Dispatcher.enQueue(j);

		JobInstance ji = emm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
				.setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		Main.main(new String[] { "localhost" });

		Thread.sleep(10000);

		File f = new File("./testprojects/JobGenADeliverable/JobGenADeliverable42.txt");

		Assert.assertEquals(true, f.exists());

		com.enioka.jqm.api.Deliverable d = new com.enioka.jqm.api.Deliverable("./testprojects/JobGenADeliverable/",
				"JobGenADeliverable42.txt");

		@SuppressWarnings("unused")
		InputStream tmp = Dispatcher.getOneDeliverable(d);
		Main.stop();

		File res = new File("./testprojects/JobGenADeliverable/JobGenADeliverableFamily/" + ji.getId() + "/JobGenADeliverable42.txt");

		Assert.assertEquals(true, res.exists());
	}

	@Test
	public void testGetUserDeliverables() throws Exception
	{

		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/JobGenADeliverable/", em);
		JobDefParameter jdpp = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable.txt", em);
		jdargs.add(jdp);
		jdargs.add(jdpp);

		ArrayList<JobDefParameter> jdargs2 = new ArrayList<JobDefParameter>();
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("filepath", "./testprojects/JobGenADeliverable/", em);
		JobDefParameter jdpp2 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable2.txt", em);
		jdargs2.add(jdp2);
		jdargs2.add(jdpp2);

		ArrayList<JobDefParameter> jdargs3 = new ArrayList<JobDefParameter>();
		JobDefParameter jdp3 = CreationTools.createJobDefParameter("filepath", "./testprojects/JobGenADeliverable/", em);
		JobDefParameter jdpp3 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable3.txt", em);
		jdargs3.add(jdp3);
		jdargs3.add(jdpp3);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/JobGenADeliverable/",
				"./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication1", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven2 = CreationTools.createJobDef(true, "App", jdargs2, "./testprojects/JobGenADeliverable/",
				"./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication2", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven3 = CreationTools.createJobDef(true, "App", jdargs3, "./testprojects/JobGenADeliverable/",
				"./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication3", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", false, em);

		JobDefinition j1 = new JobDefinition("MarsuApplication1", "Franquin");
		JobDefinition j2 = new JobDefinition("MarsuApplication2", "Franquin");
		JobDefinition j3 = new JobDefinition("MarsuApplication3", "Franquin");

		Dispatcher.enQueue(j1);
		Dispatcher.enQueue(j2);
		Dispatcher.enQueue(j3);

		Helpers.printJobInstanceTable();

		Main.main(new String[] { "localhost" });

		Thread.sleep(20000);
		Main.stop();

		File f1 = new File("./testprojects/JobGenADeliverable/JobGenADeliverable.txt");
		File f2 = new File("./testprojects/JobGenADeliverable/JobGenADeliverable2.txt");
		File f3 = new File("./testprojects/JobGenADeliverable/JobGenADeliverable3.txt");

		Assert.assertEquals(true, f1.exists());
		Assert.assertEquals(true, f2.exists());
		Assert.assertEquals(true, f3.exists());

		Helpers.printJobInstanceTable();

		List<Deliverable> tmp = Dispatcher.getUserDeliverables("Franquin");

		Assert.assertEquals(3, tmp.size());
		// Assert.assertEquals(, tmp.get(0).getFilePath());
	}

	@Test
	public void testGetUserJobs() throws Exception
	{

		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/DateTimeMaven/",
				"./testprojects/DateTimeMaven/DateTimeMaven.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", true, em);

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

		Main.main(new String[] { "localhost" });

		Thread.sleep(10000);
		Main.stop();

		em.getTransaction().commit();

		EntityManager emm = Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC",
				JobInstance.class).getResultList();

		Assert.assertEquals(3, res.size());
	}

	@Test
	public void testGetJobs() throws Exception
	{

		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/DateTimeMaven/",
				"./testprojects/DateTimeMaven/DateTimeMaven.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", true, em);

		ArrayList<JobDefParameter> jdargs2 = new ArrayList<JobDefParameter>();
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("filepath", "./testprojects/JobGenADeliverable/", em);
		JobDefParameter jdp3 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable42.txt", em);
		jdargs2.add(jdp2);
		jdargs2.add(jdp3);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven2 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/JobGenADeliverable/",
				"./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "test", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		JobDefinition j2 = new JobDefinition("test", "Toto");

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

		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<Queue> qs = (ArrayList<Queue>) em.createQuery("SELECT q FROM Queue q", Queue.class).getResultList();

		Assert.assertEquals(3, qs.size());
	}

	@Test
	public void testGoodOrder() throws Exception
	{

		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/DateTimeMaven/",
				"./testprojects/DateTimeMaven/DateTimeMaven.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", true, em);

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/DateTimeMaven/",
				"./testprojects/DateTimeMaven/DateTimeMaven.jar", Helpers.qNormal, 42, "MarsuApplication2", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", true, em);

		@SuppressWarnings("unused")
		JobDef jd2 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/DateTimeMaven/",
				"./testprojects/DateTimeMaven/DateTimeMaven.jar", Helpers.qSlow, 42, "MarsuApplication3", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		JobDefinition jj = new JobDefinition("MarsuApplication2", "Franquin");
		JobDefinition jjj = new JobDefinition("MarsuApplication3", "Franquin");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(jj);
		Dispatcher.enQueue(jj);
		Dispatcher.enQueue(jjj);

		Main.main(new String[] { "localhost" });

		Thread.sleep(10);

		Dispatcher.enQueue(jjj);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(jj);
		Dispatcher.enQueue(j);

		Thread.sleep(1000);
		Main.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j WHERE j.queue = :q ORDER BY j.position ASC",
				JobInstance.class);
		query.setParameter("q", Helpers.qVip);
		ArrayList<JobInstance> resVIP = (ArrayList<JobInstance>) query.getResultList();

		TypedQuery<JobInstance> query2 = em.createQuery("SELECT j FROM JobInstance j WHERE j.queue = :q ORDER BY j.position ASC",
				JobInstance.class);
		query2.setParameter("q", Helpers.qNormal);
		ArrayList<JobInstance> resNormal = (ArrayList<JobInstance>) query.getResultList();

		TypedQuery<JobInstance> query3 = em.createQuery("SELECT j FROM JobInstance j WHERE j.queue = :q ORDER BY j.position ASC",
				JobInstance.class);
		query3.setParameter("q", Helpers.qSlow);
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
		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/JobGenADeliverable/",
				"./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", true, em);

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/JobGenADeliverable/",
				"./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qNormal, 42, "MarsuApplication2", 42, "Franquin",
				"ModuleMachin", "other", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		j.addParameter("filepath", "./testprojects/JobGenADeliverable/");
		j.addParameter("fileName", "JobGenADeliverableSecurity1.txt");

		JobDefinition jj = new JobDefinition("MarsuApplication2", "Franquin");
		jj.addParameter("filepath", "./testprojects/JobGenADeliverable/");
		jj.addParameter("fileName", "JobGenADeliverableSecurity2.txt");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(jj);

		Helpers.printJobInstanceTable();

		EntityManager emm = Helpers.getNewEm();
		JobInstance ji = emm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
				.setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		Main.main(new String[] { "localhost" });

		Thread.sleep(12000);

		Helpers.printJobInstanceTable();

		File f = new File("./testprojects/JobGenADeliverable/JobGenADeliverableSecurity1.txt");

		Assert.assertEquals(true, f.exists());

		List<Deliverable> tmp = Dispatcher.getUserDeliverables("MAG");
		Main.stop();

		Assert.assertEquals(1, tmp.size());

		for (Deliverable dd : tmp)
		{
			Assert.assertEquals(ji.getId(), (int) dd.getJobId());
		}
	}
}
