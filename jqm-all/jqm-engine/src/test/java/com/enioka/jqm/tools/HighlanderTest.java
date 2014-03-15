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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class HighlanderTest extends JqmBaseTest
{
    @Test
    public void testHighlanderMultiNode() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderMultiNode");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, null,
                "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest("MarsuApplication", "MAG");
        for (int i = 0; i < 9; i++)
        {
            JqmClientFactory.getClient().enqueue(j);
        }

        JqmEngine engine1 = new JqmEngine();
        JqmEngine engine2 = new JqmEngine();
        engine1.start("localhost");
        engine2.start("localhost4");

        for (int i = 0; i < 99; i++)
        {
            JqmClientFactory.getClient().enqueue(j);
        }

        TestHelpers.waitFor(20, 5000, em); // Actually wait.
        engine1.stop();
        engine2.stop();
        em.close();

        em = Helpers.getNewEm();
        ArrayList<History> res = (ArrayList<History>) em.createQuery("SELECT j FROM History j ORDER BY j.id ASC", History.class)
                .getResultList();
        em.close();

        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals(State.ENDED, res.get(1).getState());

        Calendar prevEnd = null;
        for (History h : res)
        {
            if (h.getExecutionDate().before(prevEnd))
            {
                Assert.fail("executions were not exclusive");
            }
            prevEnd = h.getEndDate();
        }
        System.out.println("there were n histories: " + res.size());
    }

    @Test
    public void testHighlanderenqueueEngineDead() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderModeenqueueEngineDead");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, null,
                "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest("MarsuApplication", "MAG");

        JqmClientFactory.getClient().enqueue(j);
        JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);
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
        jqmlogger.debug("Starting test testHighlanderEngineRunning");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-kill/target/test.jar", TestHelpers.qVip, 42,
                "kill", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        int firstJob = JqmClientFactory.getClient().enqueue(new JobRequest("kill", "MAG"));
        for (int i = 0; i < 100; i++)
        {
            JqmClientFactory.getClient().enqueue(new JobRequest("kill", "MAG"));
        }
        JqmClientFactory.getClient().killJob(firstJob);
        Thread.sleep(4000);
        JqmClientFactory.getClient().killJob(JqmClientFactory.getClient().getUserActiveJobs("MAG").get(0).getId());

        TestHelpers.waitFor(2, 10000, em);
        engine1.stop();

        List<History> res = em.createQuery("SELECT j FROM History j ORDER BY j.id ASC", History.class).getResultList();
        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.KILLED, res.get(0).getState());
        Assert.assertEquals(State.KILLED, res.get(1).getState());
        Assert.assertTrue(res.get(0).getAttributionDate().compareTo(res.get(1).getEnqueueDate()) <= 0);
    }

    @Test
    public void testHighlanderModeMultiQueue() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderModeMultiQueue");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, null,
                "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest("MarsuApplication", "MAG");

        JqmClientFactory.getClient().enqueue(j);
        JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);
        engine1.stop();

        EntityManager emm = Helpers.getNewEm();

        ArrayList<History> res = (ArrayList<History>) emm
                .createQuery("SELECT j FROM History j WHERE j.jd.id = :j ORDER BY j.enqueueDate ASC", History.class)
                .setParameter("j", jdDemoMaven.getId()).getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

}
