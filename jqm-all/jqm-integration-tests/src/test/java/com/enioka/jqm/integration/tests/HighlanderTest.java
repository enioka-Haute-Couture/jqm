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

package com.enioka.jqm.integration.tests;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.client.api.JobRequest;
import com.enioka.jqm.client.api.Query.Sort;
import com.enioka.jqm.client.api.State;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class HighlanderTest extends JqmBaseTest
{
    @Test
    public void testHighlanderMultiNode() throws Exception
    {
        int enqueueLoops = 1000;

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("delay_ms", "200");
        CreationTools.createJobDef(null, true, "pyl.Wait", parameters, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication1", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        CreationTools.createJobDef(null, true, "pyl.WaitRandom", parameters, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication2", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);

        JobRequest j1 = jqmClient.newJobRequest("MarsuApplication1", "TestUser");
        JobRequest j2 = jqmClient.newJobRequest("MarsuApplication2", "TestUser");
        for (int i = 0; i < 9; i++)
        {
            j1.enqueue();
        }

        // Start 3 nodes which actually poll qVip (1ms).
        addAndStartEngine();
        addAndStartEngine("localhost4");
        addAndStartEngine("localhost5");

        TestHelpers.waitFor(1, 10000, cnx); // This actually ensures at least one engine actually processes requests, nothing more.

        // Now create a lot of requests! This is a synhronisation stress tests of sorts.
        for (int i = 0; i < enqueueLoops; i++)
        {
            j1.enqueue();
            j2.enqueue();
        }
        jqmlogger.info("Done creating highlander requests");
        TestHelpers.waitFor(20, 5000, cnx); // Actually wait. We have no idea how many requests will actually get processed.

        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));

        List<com.enioka.jqm.client.api.JobInstance> res = jqmClient.newQuery().addSortAsc(Sort.ID).setApplicationName("MarsuApplication1")
                .invoke();

        Assert.assertTrue(res.size() >= 2); // At least initial request + one is allowed to wait behind it. Likely more.
        Assert.assertTrue(res.size() < 10 + enqueueLoops - 1); // At least one request must have been squeezed by the highlander mode.

        Calendar prevEnd = null;
        for (com.enioka.jqm.client.api.JobInstance h : res)
        {
            Assert.assertEquals(State.ENDED, h.getState());
            Assert.assertEquals(true, h.isHighlander());

            if (h.getBeganRunningDate().before(prevEnd))
            {
                Assert.fail("executions were not exclusive");
            }
            prevEnd = h.getEndDate();
        }
        jqmlogger.info("there were n histories: " + res.size());
    }

    @Test
    public void testHighlanderenqueueEngineDead() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        JobRequest j = jqmClient.newJobRequest("MarsuApplication", "TestUser");
        j.enqueue();
        j.enqueue();

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

        long firstJob = jqmClient.newJobRequest("kill", "TestUser").enqueue();
        for (int i = 0; i < 100; i++)
        {
            jqmClient.newJobRequest("kill", "TestUser").enqueue();
        }
        Thread.sleep(3000);
        Calendar killTime1 = Calendar.getInstance();
        jqmClient.killJob(firstJob);
        Thread.sleep(3000);
        jqmClient.killJob(jqmClient.getUserActiveJobs("TestUser").get(0).getId());
        TestHelpers.waitFor(2, 10000, cnx);

        List<com.enioka.jqm.client.api.JobInstance> res = jqmClient.newQuery().addSortAsc(Sort.ID).invoke();
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

        jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();
        jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testHighlanderMultiNodeBug195() throws Exception
    {
        long q = Queue.create(cnx, "q", "test queue", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 1, 1, q);
        DeploymentParameter.create(cnx, TestHelpers.nodeMix.getId(), 1, 1, q);

        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", q, 42, "WithH", null,
                "Franquin", "WithH", "other", "other", true, cnx);
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", q, 42, "WithoutH", null,
                "Franquin", "WithoutH", "other", "other", false, cnx);

        long i1 = jqmClient.newJobRequest("WithH", "TestUser").enqueue();

        addAndStartEngine();
        addAndStartEngine("localhost4");

        TestHelpers.waitForRunning(1, 5000, cnx);
        long i2 = jqmClient.newJobRequest("WithH", "TestUser").enqueue();
        sleep(2);
        long i3 = jqmClient.newJobRequest("WithoutH", "TestUser").enqueue();

        TestHelpers.waitForRunning(2, 5000, cnx);
        sleep(1); // Additional - check no more than two running!

        List<com.enioka.jqm.client.api.JobInstance> res = jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addSortAsc(Sort.ID).invoke();

        Assert.assertEquals(State.RUNNING, res.get(0).getState());
        Assert.assertEquals(State.SUBMITTED, res.get(1).getState());
        Assert.assertEquals(State.RUNNING, res.get(2).getState());
        Assert.assertEquals(true, res.get(0).isHighlander());

        jqmClient.killJob(i2);
        jqmClient.killJob(i1);
        jqmClient.killJob(i3);
        TestHelpers.waitFor(2, 20000, cnx);
    }
}
