/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;

public class JndiTest
{
	public static Logger jqmlogger = Logger.getLogger(JndiTest.class);
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
	public static void end()
	{
		s.shutdown();
		s.stop();
	}

	// @Test // NOT AN AUTO TEST: this requires to have MQ Series jars which are not libre software!
	/*
	 * public void testJmsWmq() throws Exception { EntityManager em = com.enioka.jqm.tools.Helpers.getNewEm(); Helpers.createLocalNode(em);
	 * 
	 * ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
	 * 
	 * @SuppressWarnings("unused") JobDef jd = CreationTools.createJobDef(true, "com.enioka.jqm.testpackages.SuperTestPayload", jdargs,
	 * "./testprojects/jqm-test-jndijms-wmq/", "./testprojects/jqm-test-jndijms-wmq/jqm-test-jndijms-wmq.jar", Helpers.qVip, 42, "Jms", 42,
	 * "Franquin", "ModuleMachin", "other1", "other2", "other3", false, em);
	 * 
	 * JobDefinition form = new JobDefinition("Jms", "MAG"); form.addParameter("p1", "1"); form.addParameter("p2", "2");
	 * Dispatcher.enQueue(form);
	 * 
	 * // Create JMS JNDI references for use by the test jar em.getTransaction().begin();
	 * em.persist(CreationTools.createJndiQueueMQSeries("jms/testqueue", "test Queue", "Q.TEST", null));
	 * em.persist(CreationTools.createJndiQcfMQSeries("jms/qcf", "test QCF", "localhost", "MQ.TEST", 1414, "SYSTEM.DEF.SRVCON"));
	 * em.getTransaction().commit();
	 * 
	 * // Start the engine JqmEngine engine1 = new JqmEngine(); engine1.start(new String[] { "localhost" });
	 * 
	 * Thread.sleep(5000); engine1.stop();
	 * 
	 * long i = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult(); Assert.assertTrue(i == 1); }
	 */

	@Test
	public void testJmsAmq() throws Exception
	{
		jqmlogger.debug("AMQ: Starting");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(true, "com.enioka.jqm.testpackages.SuperTestPayload", jdargs,
				"./testprojects/jqm-test-jndijms-amq/", "jqm-test-jndijms-amq/jqm-test-jndijms-amq.jar", TestHelpers.qVip, 42, "Jms",
				"Franquin", "ModuleMachin", "other1", "other2", "other3", false, em);

		JobDefinition form = new JobDefinition("Jms", "MAG");
		Dispatcher.enQueue(form);

		// Create JMS JNDI references for use by the test jar
		em.getTransaction().begin();
		CreationTools.createJndiQueueActiveMQ(em, "jms/testqueue", "test queue", "Q.TEST", null);
		CreationTools.createJndiQcfActiveMQ(em, "jms/qcf", "test QCF", "vm:broker:(tcp://localhost:1234)?persistent=false&useJmx=false",
				null);
		em.getTransaction().commit();

		// Start the engine
		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(10000);
		engine1.stop();

		History h = null;
		try
		{
			h = (History) em.createQuery("SELECT h FROM History h").getSingleResult();
		} catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("History object was not created");
			throw e;
		}

		Assert.assertEquals("ENDED", h.getJobInstance().getState()); // Exception in jar => CRASHED
	}

	@Test
	public void testJmsAmqWrongAlias() throws Exception
	{
		jqmlogger.debug("WRONG ALIAS: Starting");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(true, "com.enioka.jqm.testpackages.SuperTestPayload", jdargs,
				"./testprojects/jqm-test-jndijms-amq/", "jqm-test-jndijms-amq/jqm-test-jndijms-amq.jar", TestHelpers.qVip, 42, "Jms",
				"Franquin", "ModuleMachin", "other1", "other2", "other3", false, em);

		JobDefinition form = new JobDefinition("Jms", "MAG");
		Dispatcher.enQueue(form);

		// Create JMS JNDI references for use by the test jar
		em.getTransaction().begin();
		CreationTools.createJndiQueueActiveMQ(em, "jms/testqueue", "test queue", "Q.TEST", null);
		CreationTools.createJndiQcfActiveMQ(em, "jms/qcf2", "test QCF", "vm:broker:(tcp://localhost:1234)?persistent=false&useJmx=false",
				null);
		em.getTransaction().commit();

		// Start the engine
		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

		Thread.sleep(10000);
		engine1.stop();

		History h = null;
		try
		{
			h = (History) em.createQuery("SELECT h FROM History h").getSingleResult();
		} catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("History object was not created");
			throw e;
		}

		Assert.assertEquals("CRASHED", h.getJobInstance().getState()); // Exception in jar => CRASHED
	}
}
