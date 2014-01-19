/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.State;

public class HighlanderTest
{
    public static Logger jqmlogger = Logger.getLogger(JobBaseTest.class);
    public static Server s;

    @Before
    public void before()
    {
        Dispatcher.resetEM();
        Helpers.resetEmf();
    }

    @BeforeClass
    public static void testInit() throws FileNotFoundException
    {
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        // s.setLogWriter(new PrintWriter("C:\\TEMP\\toto2.log"));
        // s.setSilent(false);
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
    public void testHighlanderMultiNode() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderMultiNode");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimesendmsg/",
                "jqm-test-datetimesendmsg/jqm-test-datetimesendmsg.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);
        em.close();

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
        for (int i = 0; i < 9; i++)
        {
            Dispatcher.enQueue(j);
        }

        JqmEngine engine1 = new JqmEngine();
        JqmEngine engine2 = new JqmEngine();
        engine1.start("localhost");
        engine2.start("localhost4");

        for (int i = 0; i < 9; i++)
        {
            Dispatcher.enQueue(j);
        }

        TestHelpers.waitFor(2, 10000);
        engine1.stop();
        engine2.stop();

        em = Helpers.getNewEm();
        ArrayList<History> res = (ArrayList<History>) em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class)
                .getResultList();
        em.close();

        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals(State.ENDED, res.get(1).getState());
    }

    @Test
    public void testHighlanderEnqueueEngineDead() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderModeEnqueueEngineDead");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        Dispatcher.enQueue(j);
        Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        EntityManager emm = Helpers.getNewEm();

        ArrayList<History> res = (ArrayList<History>) emm.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class)
                .getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testHighlanderEngineRunning() throws Exception
    {
        // This test launches an infinite loop as Highlander, checks if no other job can launch. Job is killed at the end - which allows a
        // second one to run, which also has to be killed.
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderEngineRunning");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-kill/", "jqm-test-kill/jqm-test-kill.jar", TestHelpers.qVip, 42,
                "kill", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        int firstJob = Dispatcher.enQueue(new JobDefinition("kill", "MAG"));
        for (int i = 0; i < 100; i++)
        {
            Dispatcher.enQueue(new JobDefinition("kill", "MAG"));
        }
        Dispatcher.killJob(firstJob);
        Thread.sleep(4000);
        Dispatcher.killJob(Dispatcher.getUserJobs("MAG").get(0).getId());

        TestHelpers.waitFor(2, 10000);
        engine1.stop();

        List<History> res = em.createQuery("SELECT j FROM History j ORDER BY j.id ASC", History.class).getResultList();
        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.KILLED, res.get(0).getState());
        Assert.assertEquals(State.KILLED, res.get(1).getState());
        TestHelpers.printHistoryTable();
        Assert.assertTrue(res.get(0).getAttributionDate().compareTo(res.get(1).getEnqueueDate()) <= 0);
    }

    @Test
    public void testHighlanderModeMultiQueue() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderModeMultiQueue");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        Dispatcher.enQueue(j);
        Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        EntityManager emm = Helpers.getNewEm();

        ArrayList<History> res = (ArrayList<History>) emm
                .createQuery("SELECT j FROM History j WHERE j.jd.id = :j ORDER BY j.enqueueDate ASC", History.class)
                .setParameter("j", jdDemoMaven.getId()).getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

}
