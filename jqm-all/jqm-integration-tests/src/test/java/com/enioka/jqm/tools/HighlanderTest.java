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

import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.Query.Sort;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.api.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class HighlanderTest extends JqmBaseTest
{
    @Test
    public void testHighlanderMultiNode() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);

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
        TestHelpers.waitFor(20, 5000, cnx); // Actually wait.

        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));

        List<com.enioka.jqm.api.JobInstance> res = Query.create().addSortAsc(Sort.ID).run();

        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals(State.ENDED, res.get(1).getState());
        Assert.assertEquals(true, res.get(0).isHighlander());

        Calendar prevEnd = null;
        for (com.enioka.jqm.api.JobInstance h : res)
        {
            if (h.getBeganRunningDate().before(prevEnd))
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
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        JobRequest j = new JobRequest("MarsuApplication", "TestUser");
        JqmClientFactory.getClient().enqueue(j);
        JqmClientFactory.getClient().enqueue(j);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testHighlanderEngineRunning() throws Exception
    {
        // This test launches an infinite loop as Highlander, checks if no other job can launch. Job is killed at the end - which allows a
        // second one to run, which also has to be killed.
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42, "kill",
                null, "Franquin", "ModuleMachin", "other", "other", true, cnx);

        addAndStartEngine();

        int firstJob = JobRequest.create("kill", "TestUser").submit();
        for (int i = 0; i < 100; i++)
        {
            JobRequest.create("kill", "TestUser").submit();
        }
        Thread.sleep(3000);
        Calendar killTime1 = Calendar.getInstance();
        JqmClientFactory.getClient().killJob(firstJob);
        Thread.sleep(3000);
        JqmClientFactory.getClient().killJob(JqmClientFactory.getClient().getUserActiveJobs("TestUser").get(0).getId());
        TestHelpers.waitFor(2, 10000, cnx);

        List<com.enioka.jqm.api.JobInstance> res = Query.create().addSortAsc(Sort.ID).run();
        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.CRASHED, res.get(0).getState());
        Assert.assertEquals(State.CRASHED, res.get(1).getState());
        Assert.assertTrue(killTime1.compareTo(res.get(1).getBeganRunningDate()) <= 0);
    }

    @Test
    public void testHighlanderModeMultiQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);

        JobRequest.create("MarsuApplication", "TestUser").submit();
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testHighlanderMultiNodeBug195() throws Exception
    {
        int q = Queue.create(cnx, "q", "test queue", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 1, 1, q);
        DeploymentParameter.create(cnx, TestHelpers.nodeMix.getId(), 1, 1, q);

        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", q, 42, "WithH", null,
                "Franquin", "WithH", "other", "other", true, cnx);
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", q, 42, "WithoutH", null,
                "Franquin", "WithoutH", "other", "other", false, cnx);

        int i1 = JqmClientFactory.getClient().enqueue(new JobRequest("WithH", "TestUser"));

        addAndStartEngine();
        addAndStartEngine("localhost4");

        sleep(3);
        int i2 = JqmClientFactory.getClient().enqueue(new JobRequest("WithH", "TestUser"));
        sleep(2);
        int i3 = JqmClientFactory.getClient().enqueue(new JobRequest("WithoutH", "TestUser"));
        sleep(1);

        List<com.enioka.jqm.api.JobInstance> res = Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addSortAsc(Sort.ID).run();

        Assert.assertEquals(State.RUNNING, res.get(0).getState());
        Assert.assertEquals(State.SUBMITTED, res.get(1).getState());
        Assert.assertEquals(State.RUNNING, res.get(2).getState());
        Assert.assertEquals(true, res.get(0).isHighlander());

        JqmClientFactory.getClient().killJob(i2);
        JqmClientFactory.getClient().killJob(i1);
        JqmClientFactory.getClient().killJob(i3);
        TestHelpers.waitFor(2, 20000, cnx);
    }
}
