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
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class HighlanderTest extends JqmBaseTest
{
    @Test
    public void testHighlanderMultiNode() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest("MarsuApplication", "TestUser");
        for (int i = 0; i < 9; i++)
        {
            JqmClientFactory.getClient().enqueue(j);
        }

        addAndStartEngine();
        addAndStartEngine("localhost4");

        for (int i = 0; i < 99; i++)
        {
            JqmClientFactory.getClient().enqueue(j);
        }
        TestHelpers.waitFor(20, 5000, em); // Actually wait.

        ArrayList<History> res = (ArrayList<History>) em.createQuery("SELECT j FROM History j ORDER BY j.id ASC", History.class).getResultList();
        em.close();

        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals(State.ENDED, res.get(1).getState());
        Assert.assertEquals(true, res.get(0).isHighlander());

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
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);
        JobRequest j = new JobRequest("MarsuApplication", "TestUser");
        JqmClientFactory.getClient().enqueue(j);
        JqmClientFactory.getClient().enqueue(j);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        EntityManager emm = getNewEm();
        Assert.assertEquals(1, TestHelpers.getOkCount(emm));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(emm));
    }

    @Test
    public void testHighlanderEngineRunning() throws Exception
    {
        // This test launches an infinite loop as Highlander, checks if no other job can launch. Job is killed at the end - which allows a
        // second one to run, which also has to be killed.
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42, "kill", null, "Franquin", "ModuleMachin",
                "other", "other", true, em);

        addAndStartEngine();

        int firstJob = JobRequest.create("kill", "TestUser").submit();
        for (int i = 0; i < 100; i++)
        {
            JobRequest.create("kill", "TestUser").submit();
        }
        Thread.sleep(3000);
        JqmClientFactory.getClient().killJob(firstJob);
        Thread.sleep(3000);
        JqmClientFactory.getClient().killJob(JqmClientFactory.getClient().getUserActiveJobs("TestUser").get(0).getId());
        TestHelpers.waitFor(2, 10000, em);

        List<History> res = em.createQuery("SELECT j FROM History j ORDER BY j.id ASC", History.class).getResultList();
        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.KILLED, res.get(0).getState());
        Assert.assertEquals(State.KILLED, res.get(1).getState());
        Assert.assertTrue(res.get(0).getAttributionDate().compareTo(res.get(1).getEnqueueDate()) <= 0);
    }

    @Test
    public void testHighlanderModeMultiQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobRequest.create("MarsuApplication", "TestUser").submit();
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        EntityManager emm = getNewEm();
        Assert.assertEquals(1, TestHelpers.getOkCount(emm));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(emm));
    }

    @Test
    public void testHighlanderMultiNodeBug195() throws Exception
    {
        Queue q = CreationTools.initQueue("q", "", 42, em);
        CreationTools.createDeploymentParameter(TestHelpers.node, 1, 1, q, em);
        CreationTools.createDeploymentParameter(TestHelpers.nodeMix, 1, 1, q, em);

        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", q, 42, "WithH", null, "Franquin", "WithH", "other", "other", true, em);
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", q, 42, "WithoutH", null, "Franquin", "WithoutH", "other", "other",
                false, em);

        int i1 = JqmClientFactory.getClient().enqueue(new JobRequest("WithH", "TestUser"));

        addAndStartEngine();
        addAndStartEngine("localhost4");

        sleep(3);
        int i2 = JqmClientFactory.getClient().enqueue(new JobRequest("WithH", "TestUser"));
        sleep(2);
        int i3 = JqmClientFactory.getClient().enqueue(new JobRequest("WithoutH", "TestUser"));
        sleep(1);

        List<JobInstance> res = em.createQuery("SELECT j FROM JobInstance j ORDER BY j.id ASC", JobInstance.class).getResultList();

        Assert.assertEquals(State.RUNNING, res.get(0).getState());
        Assert.assertEquals(State.SUBMITTED, res.get(1).getState());
        Assert.assertEquals(State.RUNNING, res.get(2).getState());
        Assert.assertEquals(true, res.get(0).getJd().isHighlander());

        JqmClientFactory.getClient().killJob(i2);
        JqmClientFactory.getClient().killJob(i1);
        JqmClientFactory.getClient().killJob(i3);
        TestHelpers.waitFor(2, 20000, em);
    }
}
