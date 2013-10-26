package com.enioka.jqm.tools;

import java.util.ArrayList;

import javax.persistence.EntityManager;

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

public class GeoTests
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
	public void testGeo() throws Exception
	{
		EntityManager em = Helpers.getNewEm();
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-geo/",
				"./testprojects/jqm-test-geo/jqm-test-geo.jar", TestHelpers.qVip, 42, "Geo", 42, "Franquin", "ModuleMachin", "other1",
				"other2", "other3", false, em);

		JobDefinition form = new JobDefinition("Geo", "MAG");
		form.addParameter("nbJob", "1");
		Dispatcher.enQueue(form);

		// Create JNDI connection to write inside the engine database
		em.getTransaction().begin();
		CreationTools.createDatabaseProp("jdbc/jqm", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/testdbengine", "SA", "", em);
		em.getTransaction().commit();

		// Start the engine
		JqmEngine engine1 = new JqmEngine();
		JqmEngine engine2 = new JqmEngine();
		JqmEngine engine3 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		engine2.start(new String[] { "localhost4" });
		engine3.start(new String[] { "localhost5" });

		Thread.sleep(30000);
		jqmlogger.debug("###############################################################");
		jqmlogger.debug("SHUTDOWN");
		jqmlogger.debug("###############################################################");
		engine1.stop();
		engine2.stop();
		engine3.stop();

		TestHelpers.printJobInstanceTable();

		long i = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
		Assert.assertTrue(i > 3);
	}
}
