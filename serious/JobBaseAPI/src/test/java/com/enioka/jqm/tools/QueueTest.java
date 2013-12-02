/**
 * Copyright �� 2013 enioka. All rights reserved
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
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;

public class QueueTest
{

	public static Server s;
	public static Logger jqmlogger = Logger.getLogger(QueueTest.class);

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
	public void testMaxThreadNormal() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testMaxThreadNormal");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);
		ArrayList<JobInstance> job = null;

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
		        "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qNormal, 42, "MarsuApplication", null, "Franquin",
		        "ModuleMachin", "other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);
		Dispatcher.enQueue(j);

		int i = 0;

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });
		EntityManager emm = Helpers.getNewEm();

		while (i < 5)
		{
			em.getEntityManagerFactory().getCache().evictAll();
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Thread.sleep(10000);

			TypedQuery<JobInstance> query = emm
			        .createQuery("SELECT j FROM JobInstance j WHERE j.state IS NOT :s AND j.state IS NOT :ss ORDER BY j.position ASC",
			                JobInstance.class);
			query.setParameter("s", "SUBMITTED").setParameter("ss", "ENDED");
			job = (ArrayList<JobInstance>) query.getResultList();
			TestHelpers.printJobInstanceTable();
			if (job.size() > 2)
				Assert.assertEquals(false, true);
			i++;
		}
		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		for (JobInstance jobInstance : res)
		{
			Assert.assertEquals("ENDED", jobInstance.getState());
		}
	}

	@Test
	public void testMaxThreadVip() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testMaxThreadVip");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);
		ArrayList<JobInstance> job = null;

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
		        "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
		        "ModuleMachin", "other", "other", false, em);

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

		int i = 0;

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

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
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			// Dispatcher.enQueue(j);
			Thread.sleep(10000);
			TestHelpers.printJobInstanceTable();
			em.clear();
			TypedQuery<JobInstance> query = emm
			        .createQuery("SELECT j FROM JobInstance j WHERE j.state IS NOT :s AND j.state IS NOT :ss ORDER BY j.position ASC",
			                JobInstance.class);
			query.setParameter("s", "SUBMITTED").setParameter("ss", "ENDED");
			job = (ArrayList<JobInstance>) query.getResultList();

			if (job.size() > 3)
				Assert.fail();
			i++;
		}
		engine1.stop();

		TypedQuery<JobInstance> query = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.position ASC", JobInstance.class);
		ArrayList<JobInstance> res = (ArrayList<JobInstance>) query.getResultList();

		for (JobInstance jobInstance : res)
		{
			Assert.assertEquals("ENDED", jobInstance.getState());
		}
	}

	@Test
	public void testMaxThreadVipLock() throws Exception
	{
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("**********************************************************");
		jqmlogger.debug("Starting test testMaxThreadVipLock");
		EntityManager em = Helpers.getNewEm();
		TestHelpers.cleanup(em);
		TestHelpers.createLocalNode(em);
		ArrayList<JobInstance> job = null;

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
		        "jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
		        "ModuleMachin", "other", "other", false, em);

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

		int i = 0;

		JqmEngine engine1 = new JqmEngine();
		engine1.start(new String[] { "localhost" });

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
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Dispatcher.enQueue(j);
			Thread.sleep(12000);
			TestHelpers.printJobInstanceTable();
			em.clear();
			TypedQuery<JobInstance> query = emm
			        .createQuery("SELECT j FROM JobInstance j WHERE j.state IS NOT :s AND j.state IS NOT :ss ORDER BY j.position ASC",
			                JobInstance.class);
			// 134 messages must be printed

			query.setParameter("s", "SUBMITTED").setParameter("ss", "ENDED");
			job = (ArrayList<JobInstance>) query.getResultList();

			if (job.size() > 3)
				Assert.assertEquals(false, true);
			i++;
		}
		engine1.stop();

		ArrayList<Message> msgs = (ArrayList<Message>) em.createQuery("SELECT m FROM Message m WHERE m.textMessage = :m", Message.class)
		        .setParameter("m", "DateTime will be printed").getResultList();

		Assert.assertEquals(139, msgs.size());
		Assert.assertEquals(true, true);
	}
}
