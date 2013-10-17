
package com.enioka.jqm.tests;

import java.io.File;
import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
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

		em.getTransaction().begin();

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
		        .setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId()).executeUpdate();

		em.getTransaction().commit();

		em.getTransaction().begin();

		printJobInstanceTable();

		Main.main(new String[]
		{ "localhost" });

		Thread.sleep(3000);
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

	public void testGetDeliverable() throws Exception {

		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "Main", jdargs, "./testprojects/JobGenADeliverable/",
		        "./testprojects/JobGenADeliverable/JobGenADeliverable.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
		        "other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication");

		Dispatcher.enQueue(j);

		Main.main(new String[]
		{ "localhost" });

		Thread.sleep(3000);
		Main.stop();

		File f = new File("./testprojects/JobGenADeliverable/JobGenADeliverable.txt");

		Assert.assertEquals(true, f.exists());

		Dispatcher.getDeliverables(1);

		File res = new File("./testprojects/JobGenADeliverable/deliverable1");

		Assert.assertEquals(true, res.exists());

	}
}
