/**
 * Copyright ?? 2013 enioka. All rights reserved
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
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Node;

public class MultiNodeTest
{
	public static Logger jqmlogger = Logger.getLogger(JobBaseTest.class);
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
		Dispatcher.resetEM();
		s.shutdown();
		s.stop();
	}

	// @Test
	public void testOneQueueTwoNodes() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testOneQueueTwoNodes");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j11 = new JobDefinition("AppliNode1-1", "MAG");

		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);

		JqmEngine engine1 = new JqmEngine();
		JqmEngine engine2 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		engine2.start(new String[] { "localhost4" });

		int i = 0;
		while (i < 3)
		{
			TestHelpers.printJobInstanceTable();

			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);

			Thread.sleep(5000);

			TestHelpers.printJobInstanceTable();
			i++;
		}

		engine1.stop();
		engine2.stop();

		TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.executionDate ASC", History.class);
		ArrayList<History> res = (ArrayList<History>) query.getResultList();

		for (History jobInstance : res)
		{
			Assert.assertEquals("ENDED", jobInstance.getState());
		}
	}

	// @Test
	public void testOneQueueThreeNodes() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testOneQueueThreeNodes");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j11 = new JobDefinition("AppliNode1-1", "MAG");

		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);

		JqmEngine engine1 = new JqmEngine();
		JqmEngine engine2 = new JqmEngine();
		JqmEngine engine3 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		engine2.start(new String[] { "localhost4" });
		engine3.start(new String[] { "localhost5" });

		int i = 0;
		while (i < 3)
		{
			TestHelpers.printJobInstanceTable();

			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);

			Thread.sleep(5000);

			TestHelpers.printJobInstanceTable();
			i++;
		}

		engine1.stop();
		engine2.stop();

		TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.executionDate ASC", History.class);
		ArrayList<History> res = (ArrayList<History>) query.getResultList();

		for (History jobInstance : res)
		{
			Assert.assertEquals("ENDED", jobInstance.getState());
		}
	}

	// @Test
	public void testTwoNodesTwoQueues() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testTwoNodesTwoQueues");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd21 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip2, 42, "AppliNode2-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j11 = new JobDefinition("AppliNode1-1", "MAG");
		JobDefinition j21 = new JobDefinition("AppliNode2-1", "MAG");

		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j21);

		JqmEngine engine1 = new JqmEngine();
		JqmEngine engine2 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		engine2.start(new String[] { "localhost2" });

		int i = 0;
		while (i < 3)
		{
			TestHelpers.printJobInstanceTable();

			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);

			Thread.sleep(5000);

			TestHelpers.printJobInstanceTable();
			i++;
		}

		engine1.stop();
		engine2.stop();

		TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.executionDate ASC", History.class);
		ArrayList<History> res = (ArrayList<History>) query.getResultList();

		for (History jobInstance : res)
		{
			Assert.assertEquals("ENDED", jobInstance.getState());
		}
	}

	// @Test
	public void testThreeNodesThreeQueues() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testThreeNodesThreeQueues");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd12 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qNormal, 42, "AppliNode1-2", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd13 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qSlow, 42, "AppliNode1-3", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd21 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip2, 42, "AppliNode2-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd22 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qNormal2, 42, "AppliNode2-2", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd23 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qSlow2, 42, "AppliNode2-3", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd31 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip3, 42, "AppliNode3-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd32 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qNormal3, 42, "AppliNode3-2", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd33 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qSlow3, 42, "AppliNode3-3", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j11 = new JobDefinition("AppliNode1-1", "MAG");
		JobDefinition j12 = new JobDefinition("AppliNode1-2", "MAG");
		JobDefinition j13 = new JobDefinition("AppliNode1-3", "MAG");

		JobDefinition j21 = new JobDefinition("AppliNode2-1", "MAG");
		JobDefinition j22 = new JobDefinition("AppliNode2-2", "MAG");
		JobDefinition j23 = new JobDefinition("AppliNode2-3", "MAG");

		JobDefinition j31 = new JobDefinition("AppliNode3-1", "MAG");
		JobDefinition j32 = new JobDefinition("AppliNode3-2", "MAG");
		JobDefinition j33 = new JobDefinition("AppliNode3-3", "MAG");

		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j12);
		Dispatcher.enQueue(j12);
		Dispatcher.enQueue(j12);
		Dispatcher.enQueue(j13);
		Dispatcher.enQueue(j13);
		Dispatcher.enQueue(j13);

		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j22);
		Dispatcher.enQueue(j22);
		Dispatcher.enQueue(j22);
		Dispatcher.enQueue(j23);
		Dispatcher.enQueue(j23);
		Dispatcher.enQueue(j23);

		Dispatcher.enQueue(j31);
		Dispatcher.enQueue(j31);
		Dispatcher.enQueue(j31);
		Dispatcher.enQueue(j32);
		Dispatcher.enQueue(j32);
		Dispatcher.enQueue(j32);
		Dispatcher.enQueue(j33);
		Dispatcher.enQueue(j33);
		Dispatcher.enQueue(j33);

		JqmEngine engine1 = new JqmEngine();
		JqmEngine engine2 = new JqmEngine();
		JqmEngine engine3 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		engine2.start(new String[] { "localhost2" });
		engine3.start(new String[] { "localhost3" });

		int i = 0;
		while (i < 3)
		{
			TestHelpers.printJobInstanceTable();

			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j13);
			Dispatcher.enQueue(j13);
			Dispatcher.enQueue(j13);

			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j22);
			Dispatcher.enQueue(j22);
			Dispatcher.enQueue(j22);
			Dispatcher.enQueue(j23);
			Dispatcher.enQueue(j23);
			Dispatcher.enQueue(j23);

			Dispatcher.enQueue(j31);
			Dispatcher.enQueue(j31);
			Dispatcher.enQueue(j31);
			Dispatcher.enQueue(j32);
			Dispatcher.enQueue(j32);
			Dispatcher.enQueue(j32);
			Dispatcher.enQueue(j33);
			Dispatcher.enQueue(j33);
			Dispatcher.enQueue(j33);

			Thread.sleep(5000);

			TestHelpers.printJobInstanceTable();
			i++;
		}

		engine1.stop();
		engine2.stop();
		engine3.stop();

		TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.executionDate ASC", History.class);
		ArrayList<History> res = (ArrayList<History>) query.getResultList();

		for (History jobInstance : res)
		{
			Assert.assertEquals("ENDED", jobInstance.getState());
		}
	}

	// @Test
	public void testHighlanderMode() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testHighlanderMode");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
				"ModuleMachin", "other", "other", true, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		TestHelpers.printJobInstanceTable();

		em.getTransaction().begin();

		JqmEngine engine1 = new JqmEngine();
		JqmEngine engine2 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		engine2.start(new String[] { "localhost4" });

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		Thread.sleep(10000);
		engine1.stop();
		engine2.stop();

		em.getTransaction().commit();

		EntityManager emm = Helpers.getNewEm();

		ArrayList<History> res = (ArrayList<History>) emm.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC",
				History.class).getResultList();

		TestHelpers.printJobInstanceTable();

		Assert.assertEquals(2, res.size());
		Assert.assertEquals("ENDED", res.get(0).getState());
		Assert.assertEquals("ENDED", res.get(1).getState());
		Assert.assertEquals(true, true);
	}

	// @Test
	public void testThreeNodesThreeQueuesLock() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testThreeNodesThreeQueuesLock");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);
		ArrayList<History> job = new ArrayList<History>();

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd12 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qNormal, 42, "AppliNode1-2", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd13 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qSlow, 42, "AppliNode1-3", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd21 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip2, 42, "AppliNode2-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd22 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qNormal2, 42, "AppliNode2-2", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd23 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qSlow2, 42, "AppliNode2-3", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd31 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip3, 42, "AppliNode3-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd32 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qNormal3, 42, "AppliNode3-2", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd33 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qSlow3, 42, "AppliNode3-3", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j11 = new JobDefinition("AppliNode1-1", "MAG");
		JobDefinition j12 = new JobDefinition("AppliNode1-2", "MAG");
		JobDefinition j13 = new JobDefinition("AppliNode1-3", "MAG");

		JobDefinition j21 = new JobDefinition("AppliNode2-1", "MAG");
		JobDefinition j22 = new JobDefinition("AppliNode2-2", "MAG");
		JobDefinition j23 = new JobDefinition("AppliNode2-3", "MAG");

		JobDefinition j31 = new JobDefinition("AppliNode3-1", "MAG");
		JobDefinition j32 = new JobDefinition("AppliNode3-2", "MAG");
		JobDefinition j33 = new JobDefinition("AppliNode3-3", "MAG");

		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j12);
		Dispatcher.enQueue(j12);
		Dispatcher.enQueue(j12);
		Dispatcher.enQueue(j13);
		Dispatcher.enQueue(j13);
		Dispatcher.enQueue(j13);

		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j22);
		Dispatcher.enQueue(j22);
		Dispatcher.enQueue(j22);
		Dispatcher.enQueue(j23);
		Dispatcher.enQueue(j23);
		Dispatcher.enQueue(j23);

		Dispatcher.enQueue(j31);
		Dispatcher.enQueue(j31);
		Dispatcher.enQueue(j31);
		Dispatcher.enQueue(j32);
		Dispatcher.enQueue(j32);
		Dispatcher.enQueue(j32);
		Dispatcher.enQueue(j33);
		Dispatcher.enQueue(j33);
		Dispatcher.enQueue(j33);

		JqmEngine engine1 = new JqmEngine();
		JqmEngine engine2 = new JqmEngine();
		JqmEngine engine3 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		engine2.start(new String[] { "localhost2" });
		engine3.start(new String[] { "localhost3" });

		int i = 0;
		while (i < 3)
		{
			TestHelpers.printJobInstanceTable();

			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j13);
			Dispatcher.enQueue(j13);
			Dispatcher.enQueue(j13);

			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j22);
			Dispatcher.enQueue(j22);
			Dispatcher.enQueue(j22);
			Dispatcher.enQueue(j23);
			Dispatcher.enQueue(j23);
			Dispatcher.enQueue(j23);

			Dispatcher.enQueue(j31);
			Dispatcher.enQueue(j31);
			Dispatcher.enQueue(j31);
			Dispatcher.enQueue(j32);
			Dispatcher.enQueue(j32);
			Dispatcher.enQueue(j32);
			Dispatcher.enQueue(j33);
			Dispatcher.enQueue(j33);
			Dispatcher.enQueue(j33);

			Thread.sleep(10000);

			TestHelpers.printJobInstanceTable();
			i++;
		}

		engine1.stop();
		engine2.stop();
		engine3.stop();

		TypedQuery<History> query = em.createQuery("SELECT j FROM History j WHERE j.status = :ss", History.class);
		// 134 messages must be printed

		query.setParameter("ss", "ENDED");
		job = (ArrayList<History>) query.getResultList();

		// 171
		ArrayList<Message> msgs = (ArrayList<Message>) em.createQuery("SELECT m FROM Message m WHERE m.textMessage = :m", Message.class)
				.setParameter("m", "DateTime will be printed").getResultList();

		Assert.assertEquals(job.size(), msgs.size());
	}

	// @Test
	public void testStopNicely() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testStopNicely");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip, 42, "AppliNode1-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd12 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qNormal, 42, "AppliNode1-2", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd21 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
				"jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip2, 42, "AppliNode2-1", null, "Franquin",
				"ModuleMachin", "other", "other", false, em);

		JobDefinition j11 = new JobDefinition("AppliNode1-1", "MAG");
		JobDefinition j12 = new JobDefinition("AppliNode1-2", "MAG");

		JobDefinition j21 = new JobDefinition("AppliNode2-1", "MAG");

		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j11);
		Dispatcher.enQueue(j12);
		Dispatcher.enQueue(j12);
		Dispatcher.enQueue(j12);

		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j21);
		Dispatcher.enQueue(j21);

		JqmEngine engine1 = new JqmEngine();
		JqmEngine engine2 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		engine2.start(new String[] { "localhost2" });

		int i = 0;
		while (i <= 2)
		{
			System.out.println(i);
			if (i == 1)
			{
				em.getTransaction().begin();
				em.createQuery("UPDATE Node n SET n.stop = 'true' WHERE n.listeningInterface = 'localhost'").executeUpdate();
				em.getTransaction().commit();
				em.clear();
				Node n = (Node) em.createQuery("SELECT n FROM Node n WHERE n.listeningInterface = 'localhost'").getSingleResult();
				jqmlogger.debug("Node stop updated: " + n.isStop());
			}
			TestHelpers.printJobInstanceTable();

			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j11);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);
			Dispatcher.enQueue(j12);

			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);
			Dispatcher.enQueue(j21);

			Thread.sleep(4000);

			TestHelpers.printJobInstanceTable();
			i++;
		}

		engine1.stop();
		engine2.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		for (JobInstance jobInstance : res)
		{
			if (jobInstance.getState().equals("ATTRIBUTED") || jobInstance.getState().equals("RUNNING"))
			{
				Assert.assertEquals(true, false);
			}
		}

		TestHelpers.printJobInstanceTable();
		Assert.assertEquals(true, true);
	}

}
