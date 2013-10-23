package com.enioka.jqm.tests;

import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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

public class FiboTest
{
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

		Thread.sleep(1000);
	}

	@AfterClass
	public static void end()
	{
		Dispatcher.resetEM();
		s.shutdown();
		s.stop();
	}

	@Test
	public void testFibo() throws Exception
	{
		EntityManager em = com.enioka.jqm.tools.Helpers.getNewEm();
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(true, "com.enioka.jqm.tests.App", jdargs, "./testprojects/jqm-test-fibo/",
				"./testprojects/jqm-test-fibo/jqm-test-fibo.jar", Helpers.qVip, 42, "Fibo", 42, "Franquin", "ModuleMachin", "other1",
				"other2", "other3", false, em);

		JobDefinition form = new JobDefinition("Fibo", "MAG");
		form.addParameter("p1", "1");
		form.addParameter("p2", "2");
		Dispatcher.enQueue(form);

		// Create JNDI connection to write inside the engine database
		em.getTransaction().begin();
		CreationTools.createDatabaseProp("jdbc/jqm", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/testdbengine", "SA", "", em);
		em.getTransaction().commit();

		// Start the engine
		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(10000);
		engine1.stop();

		long i = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
		Assert.assertTrue(i > 2);
	}
}
