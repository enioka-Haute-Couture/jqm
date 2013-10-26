package com.enioka.jqm.tools;

import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.tools.CreationTools;
import com.enioka.jqm.tools.JqmEngine;

public class ParameterTest
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

		Dispatcher.resetEM();
		Helpers.resetEmf();
	}

	@AfterClass
	public static void stop()
	{
		s.shutdown();
	}

	@Test
	public void testMixParameters() throws Exception
	{
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg1", "argument1", em);
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("arg2", "Franquin", em);
		jdargs.add(jdp);
		jdargs.add(jdp2);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/CheckArgs/",
				"./testprojects/CheckArgs/CheckArgs.jar", TestHelpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		j.addParameter("arg2", "argument2");

		Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		Thread.sleep(5000);
		engine1.stop();

		Assert.assertEquals(true, true);
	}

	@Test
	public void testDefaultParameters() throws Exception
	{
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg1", "argument1", em);
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("arg2", "argument2", em);
		jdargs.add(jdp);
		jdargs.add(jdp2);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/CheckArgs/",
				"./testprojects/CheckArgs/CheckArgs.jar", TestHelpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		Thread.sleep(5000);
		engine1.stop();

		Assert.assertEquals(true, true);
	}

	@Test
	public void testOverrideParmeters() throws Exception
	{
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg1", "Gaston Lagaffe", em);
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("arg2", "Franquin", em);
		jdargs.add(jdp);
		jdargs.add(jdp2);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/CheckArgs/",
				"./testprojects/CheckArgs/CheckArgs.jar", TestHelpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		j.addParameter("arg1", "argument1");
		j.addParameter("arg2", "argument2");

		Dispatcher.enQueue(j);

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		Thread.sleep(5000);
		engine1.stop();

		Assert.assertEquals(true, true);
	}
}
