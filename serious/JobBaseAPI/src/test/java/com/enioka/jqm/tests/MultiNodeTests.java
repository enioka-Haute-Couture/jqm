package com.enioka.jqm.tests;

import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.tools.CreationTools;
import com.enioka.jqm.tools.Main;

public class MultiNodeTests
{

	public static Server s;
	public static Server s2;

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
	public void testTwoNodesOneQueue() throws Exception
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
				"other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		Main.main(new String[] { "localhost" });

		int i = 0;
		while (i < 5)
		{
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Thread.sleep(5000);

			Helpers.printJobInstanceTable();
			i++;
		}

		Main.stop();
	}

	public void testTwoNodesTwoQueues()
	{

	}

	public void testMultiNode()
	{

	}

	public void testHighlander()
	{

	}

	public void testTwoFiboMultiNode()
	{

	}
}
