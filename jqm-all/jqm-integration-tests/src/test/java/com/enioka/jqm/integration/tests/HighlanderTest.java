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
    public void testHighlanderParentEnqueueCanTriggerNonUniqueResultBug() throws Exception
    {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("delay_ms", "200");
        HashMap<String, String> parentParameters = new HashMap<String, String>();
        parentParameters.put("burst", "1");

        // Child job is Highlander: it is the one repeatedly enqueued by parent jobs.
        CreationTools.createJobDef(null, true, "pyl.Wait", parameters, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);

        // Parent job payload enqueues MarsuApplication through the engine API.
        CreationTools.createJobDef(null, true, "pyl.EnqueueMarsuApplication", parentParameters, "jqm-tests/jqm-test-pyl/target/test.jar",
                TestHelpers.qVip, 42, "TestLaunchParentJob", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        // Reproduce reported behavior: start with a single slot, then increase while jobs are in flight.
        cnx.runUpdate("dp_update_threads_by_id", 1, TestHelpers.dpVip.getId());
        cnx.runUpdate("dp_update_interval_by_id", 10000, TestHelpers.dpVip.getId());
        cnx.commit();
        addAndStartEngine();

        final int parentEnqueueLoops = 250;
        for (int i = 0; i < parentEnqueueLoops; i++)
        {
            jqmClient.newJobRequest("TestLaunchParentJob", "TestUser").enqueue();
        }

        // Wait until at least one child is visible before changing queue mapping.
        int childrenBeforeSwitch = 0;
        long switchDeadline = System.currentTimeMillis() + 30000;
        while (System.currentTimeMillis() < switchDeadline)
        {
            childrenBeforeSwitch = jqmClient.newQuery().setApplicationName("MarsuApplication").setQueryHistoryInstances(true)
                    .setQueryLiveInstances(true).invoke().size();
            if (childrenBeforeSwitch > 0)
            {
                break;
            }
            sleepms(100);
        }
        Assert.assertTrue("No child job visible before mapping switch", childrenBeforeSwitch > 0);

        // Trigger point from the original report.
        cnx.runUpdate("dp_update_threads_by_id", 5, TestHelpers.dpVip.getId());
        cnx.commit();
        jqmlogger.info("Changed VIPQueue mapping from 1 to 5 threads during run");

        // Wait for parent launchers to leave the live queue before querying history. This avoids hammering
        // history queries while jobs are still being executed.
        int liveParents = Integer.MAX_VALUE;
        long deadline = System.currentTimeMillis() + 120000;
        while (System.currentTimeMillis() < deadline)
        {
            liveParents = jqmClient.newQuery().setApplicationName("TestLaunchParentJob").setQueryHistoryInstances(false)
                    .setQueryLiveInstances(true).invoke().size();
            if (liveParents == 0)
            {
                break;
            }
            sleepms(200);
        }
        Assert.assertEquals("Timed out waiting for parent jobs to leave live queue", 0, liveParents);

        List<com.enioka.jqm.client.api.JobInstance> children = jqmClient.newQuery().setApplicationName("MarsuApplication")
                .setQueryHistoryInstances(true).setQueryLiveInstances(true).invoke();
        Assert.assertTrue("Expected at least one MarsuApplication child job", children.size() > 0);

        HashMap<State, Integer> childStatusCounts = new HashMap<>();
        int crashedChildren = 0;
        String sampleChildCrashMessage = null;
        for (com.enioka.jqm.client.api.JobInstance ji : children)
        {
            Integer current = childStatusCounts.get(ji.getState());
            childStatusCounts.put(ji.getState(), current == null ? 1 : current + 1);

            if (ji.getState() == State.CRASHED)
            {
                crashedChildren++;
                if (sampleChildCrashMessage == null && ji.getMessages() != null && ji.getMessages().size() > 0)
                {
                    sampleChildCrashMessage = ji.getMessages().get(0);
                }
            }
        }
        jqmlogger.info("Child MarsuApplication stats: total=" + children.size() + ", byStatus=" + childStatusCounts + ", crashedChildren="
                + crashedChildren + (sampleChildCrashMessage != null ? ", sampleCrashMessage=" + sampleChildCrashMessage : ""));

        List<com.enioka.jqm.client.api.JobInstance> parents = jqmClient.newQuery().setApplicationName("TestLaunchParentJob")
                .setQueryHistoryInstances(true).setQueryLiveInstances(true).invoke();
        boolean hasNonUnique = false;
        int crashedParents = 0;
        for (com.enioka.jqm.client.api.JobInstance ji : parents)
        {
            if (ji.getState() == State.CRASHED)
            {
                crashedParents++;
                for (String msg : ji.getMessages())
                {
                    if (msg.contains("NonUniqueResultException"))
                    {
                        hasNonUnique = true;
                        break;
                    }
                }
            }
            if (hasNonUnique)
            {
                break;
            }
        }

        Assert.assertTrue("Expected at least one crashed parent job with NonUniqueResultException in messages after 1->5 mapping switch. "
                + "childrenBeforeSwitch=" + childrenBeforeSwitch + ", parentCount=" + parents.size() + ", crashedParents=" + crashedParents,
                hasNonUnique);
    }

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

    // @Test
    // public void testHighlanderEnqueueWithSubmittedInstancesDoesNotFail() throws Exception
    // {
    // CreationTools.createJobDef(null, true, "pyl.Wait", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
    // "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);

    // JobDef jd = JobDef.selectKey(cnx, "MarsuApplication");

    // JobInstance.enqueue(cnx, com.enioka.jqm.model.State.SUBMITTED, TestHelpers.qVip.longValue(), jd.getId(), null, null, null, null,
    // null, null, null, null, null, null, true, false, null, 42, Instruction.RUN, null);
    // JobInstance.enqueue(cnx, com.enioka.jqm.model.State.SUBMITTED, TestHelpers.qVip.longValue(), jd.getId(), null, null, null, null,
    // null, null, null, null, null, null, true, false, null, 42, Instruction.RUN, null);
    // cnx.commit();

    // jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();
    // }
}
