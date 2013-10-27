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
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;

public class MultiNodeTests
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
	}

	@AfterClass
	public static void stop()
	{
		s.stop();
	}

	// @Test
	public void testOneQueueTwoNodes() throws Exception
	{
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "AppliNode1-1", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

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
		while (i < 5)
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
	}

	// @Test
	public void testOneQueueThreeNodes() throws Exception
	{
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "AppliNode1-1", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

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
		while (i < 5)
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
	}

	// @Test
	public void testTwoNodesTwoQueues() throws Exception
	{
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "AppliNode1-1", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd21 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip2, 42, "AppliNode2-1", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

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
		while (i < 5)
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
	}

	// @Test
	public void testThreeNodesThreeQueues() throws Exception
	{
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd11 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "AppliNode1-1", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd12 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/DateTimeMaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qNormal, 42, "AppliNode1-2", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd13 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qSlow, 42, "AppliNode1-3", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd21 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip2, 42, "AppliNode2-1", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd22 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qNormal2, 42, "AppliNode2-2", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd23 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qSlow2, 42, "AppliNode2-3", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd31 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip3, 42, "AppliNode3-1", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd32 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qNormal3, 42, "AppliNode3-2", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

		@SuppressWarnings("unused")
		JobDef jd33 = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qSlow3, 42, "AppliNode3-3", "Franquin", "ModuleMachin",
				"other", "other", "other", false, em);

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
		while (i < 5)
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
	}

	@Test
	public void testHighlanderMode() throws Exception
	{
		jqmlogger.debug("Starting test testHighlanderMode");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/jqm-test-datetimemaven/",
				"jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", "Franquin", "ModuleMachin",
				"other", "other", "other", true, em);

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

		em.getTransaction().begin();

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
				.setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();

		em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId())
				.executeUpdate();

		em.getTransaction().commit();

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

		ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC",
				JobInstance.class).getResultList();

		TestHelpers.printJobInstanceTable();

		Assert.assertEquals(11, res.size());
		Assert.assertEquals("ENDED", res.get(0).getState());
		Assert.assertEquals("CANCELLED", res.get(1).getState());
		Assert.assertEquals("CANCELLED", res.get(2).getState());
	}

	public void testTwoFiboMultiNode()
	{

	}

	public void testMultiNode()
	{

	}

}
