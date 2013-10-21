
package com.enioka.jqm.tests;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.tools.CreationTools;
import com.enioka.jqm.tools.Main;

public class JobBaseTests {

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
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/DateTimeMaven/",
		        "./testprojects/DateTimeMaven/DateTimeMaven.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other",
		        "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		em.getTransaction().begin();

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
		        .setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId()).executeUpdate();

		em.getTransaction().commit();

		em.getTransaction().begin();

		Main.main(new String[]
		{ "localhost" });

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
	public void testGetDeliverables() throws Exception {

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
		        "./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "getDeliverables", 42, "Franquin", "ModuleMachin",
		        "other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("getDeliverables", "MAG");

		printJobInstanceTable();

		Dispatcher.enQueue(j);

		printJobInstanceTable();

		JobInstance ji = emmm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
		        .setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		Main.main(new String[]
		{ "localhost" });

		Thread.sleep(10000);

		File f = new File("./testprojects/JobGenADeliverable/JobGenADeliverable.txt");

		Assert.assertEquals(true, f.exists());

		printJobInstanceTable();

		@SuppressWarnings("unused")
		List<InputStream> tmp = Dispatcher.getDeliverables(ji.getId());
		Main.stop();

		File res = new File("./testprojects/JobGenADeliverable/JobGenADeliverableFamily/" + ji.getId() + "/JobGenADeliverable.txt");

		Assert.assertEquals(true, res.exists());
	}

	@Test
	public void testGetOneDeliverable() throws Exception {

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
		        "./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
		        "other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "Franquin");

		Dispatcher.enQueue(j);

		JobInstance ji = emm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
		        .setParameter("myId", jdDemoMaven.getId()).getSingleResult();

		Main.main(new String[]
		{ "localhost" });

		Thread.sleep(10000);

		printJobInstanceTable();

		File f = new File("./testprojects/JobGenADeliverable/JobGenADeliverable42.txt");

		Assert.assertEquals(true, f.exists());

		printJobInstanceTable();

		com.enioka.jqm.api.Deliverable d = new com.enioka.jqm.api.Deliverable("./testprojects/JobGenADeliverable/", "JobGenADeliverable42.txt");

		@SuppressWarnings("unused")
		InputStream tmp = Dispatcher.getOneDeliverable(d);
		Main.stop();

		File res = new File("./testprojects/JobGenADeliverable/JobGenADeliverableFamily/" + ji.getId() + "/JobGenADeliverable42.txt");

		Assert.assertEquals(true, res.exists());
	}

	@Test
	public void testGetUserDeliverables() throws Exception {

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
		        "./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication1", 42, "Franquin", "ModuleMachin",
		        "other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven2 = CreationTools.createJobDef(true, "App", jdargs2, "./testprojects/JobGenADeliverable/",
		        "./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication2", 42, "Franquin", "ModuleMachin",
		        "other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven3 = CreationTools.createJobDef(true, "App", jdargs3, "./testprojects/JobGenADeliverable/",
		        "./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication3", 42, "Franquin", "ModuleMachin",
		        "other", "other", "other", false, em);

		JobDefinition j1 = new JobDefinition("MarsuApplication1", "Franquin");
		JobDefinition j2 = new JobDefinition("MarsuApplication2", "Franquin");
		JobDefinition j3 = new JobDefinition("MarsuApplication3", "Franquin");

		Dispatcher.enQueue(j1);
		Dispatcher.enQueue(j2);
		Dispatcher.enQueue(j3);

		printJobInstanceTable();

		Main.main(new String[]
		{ "localhost" });

		Thread.sleep(20000);
		Main.stop();

		File f1 = new File("./testprojects/JobGenADeliverable/JobGenADeliverable.txt");
		File f2 = new File("./testprojects/JobGenADeliverable/JobGenADeliverable2.txt");
		File f3 = new File("./testprojects/JobGenADeliverable/JobGenADeliverable3.txt");

		Assert.assertEquals(true, f1.exists());
		Assert.assertEquals(true, f2.exists());
		Assert.assertEquals(true, f3.exists());

		printJobInstanceTable();

		List<Deliverable> tmp = Dispatcher.getUserDeliverables("Franquin");

		Assert.assertEquals(3, tmp.size());
		// Assert.assertEquals(, tmp.get(0).getFilePath());
	}
}
