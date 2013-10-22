package com.enioka.jqm.tests;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.tools.CreationTools;
import com.enioka.jqm.tools.Main;

public class QueueTests
{

	public void printJobInstanceTable()
	{

		EntityManager em = Helpers.getNewEm();

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
		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);
		ArrayList<JobInstance> job = null;

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/DateTimeMaven/",
				"./testprojects/DateTimeMaven/DateTimeMaven.jar", Helpers.qNormal, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		int i = 0;

		Main.main(new String[] { "localhost" });

		while (i < 5)
		{
			EntityManager emm = Helpers.getNewEm();
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

			if (job.size() > 2)
				Assert.assertEquals(false, true);
			i++;
		}
		Main.stop();

		Assert.assertEquals(true, true);
	}
}
