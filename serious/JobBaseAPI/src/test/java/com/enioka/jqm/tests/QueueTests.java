package com.enioka.jqm.tests;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.tools.CreationTools;
import com.enioka.jqm.tools.JqmEngine;

public class QueueTests
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

	public void printJobInstanceTable()
	{

		EntityManager em = com.enioka.jqm.tools.Helpers.getNewEm();

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j", JobInstance.class)
				.getResultList();

		for (JobInstance jobInstance : res)
		{

			System.out.println("==========================================================================================");
			System.out.println("JobInstance Id: " + jobInstance.getId() + " ---> " + jobInstance.getPosition() + " | "
					+ jobInstance.getState() + " | " + jobInstance.getJd().getId() + " | " + jobInstance.getQueue().getName());
			System.out.println("==========================================================================================");
		}
	}

	@Test
	public void testMaxThread() throws Exception
	{
		EntityManager em = com.enioka.jqm.tools.Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);
		ArrayList<JobInstance> job = null;

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"./testprojects/jqm-test-datetimemaven/jqm-test-datetimemaven.jar", Helpers.qNormal, 42, "MarsuApplication", 42,
				"Franquin", "ModuleMachin", "other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		int i = 0;

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		while (i < 5)
		{
			EntityManager emm = com.enioka.jqm.tools.Helpers.getNewEm();
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Thread.sleep(5000);
			printJobInstanceTable();
			TypedQuery<JobInstance> query = emm
					.createQuery("SELECT j FROM JobInstance j WHERE j.state IS NOT :s AND j.state IS NOT :ss ORDER BY j.position ASC",
							JobInstance.class);
			query.setParameter("s", "SUBMITTED").setParameter("ss", "ENDED");
			job = (ArrayList<JobInstance>) query.getResultList();

			if (job.size() > 3)
				Assert.assertEquals(false, true);
			i++;
		}
		engine1.stop();

		Assert.assertEquals(true, true);
	}
}
