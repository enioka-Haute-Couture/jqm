package com.enioka.jqm.integration.tests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.JqmInvalidRequestException;
import com.enioka.jqm.client.api.State;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

import org.junit.Assert;
import org.junit.Test;

/**
 * All tests directly concerning the QueuePoller.
 */
public class QueueTest extends JqmBaseTest
{
    @Test
    public void testQueueWidth() throws Exception
    {
        // Only 3 threads
        long qId = Queue.create(cnx, "testqueue", " ", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 3, 1, qId);

        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", qId, 42, "jqm-test-kill", null,
                "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        long i1 = jqmClient.enqueue("jqm-test-kill", "test");
        long i2 = jqmClient.enqueue("jqm-test-kill", "test");
        long i3 = jqmClient.enqueue("jqm-test-kill", "test");
        long i4 = jqmClient.enqueue("jqm-test-kill", "test");
        long i5 = jqmClient.enqueue("jqm-test-kill", "test");

        addAndStartEngine();

        // Scenario is: 5 jobs in queue. 3 should run. 2 are then killed - 3 should still run.
        TestHelpers.waitForRunning(3, 10000, cnx);
        Thread.sleep(3000); // Additional wait time to ensure no additional starts

        jqmlogger.debug("COUNT RUNNING " + cnx.runSelectSingle("ji_select_count_running", Integer.class));
        jqmlogger.debug("COUNT ALL     " + cnx.runSelectSingle("ji_select_count_all", Integer.class));
        Assert.assertEquals(3, jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.RUNNING).invoke().size());
        Assert.assertEquals(2, jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.SUBMITTED).invoke().size());

        jqmClient.killJob(i1);
        jqmClient.killJob(i2);

        Thread.sleep(2000);

        Assert.assertEquals(3, jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.RUNNING).invoke().size());
        Assert.assertEquals(0, jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.SUBMITTED).invoke().size());

        jqmClient.killJob(i3);
        jqmClient.killJob(i4);
        jqmClient.killJob(i5);

        TestHelpers.waitFor(5, 10000, cnx);
    }

    // Does the poller take multiple JI on each loop?
    @Test
    public void testQueuePollWidth() throws Exception
    {
        // Only 3 threads, one poll every hour
        long qId = Queue.create(cnx, "testqueue", " ", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 3, 3600000, qId);

        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", qId, 42, "jqm-test-kill", null,
                "Franquin", "ModuleMachin", "other", "other", false, cnx);

        jqmClient.enqueue("jqm-test-kill", "test");
        jqmClient.enqueue("jqm-test-kill", "test");
        jqmClient.enqueue("jqm-test-kill", "test");
        jqmClient.enqueue("jqm-test-kill", "test");

        // Scenario is: 4 jobs in queue with 3 slots. 3 jobs should run. 1 is then killed - 3 should still run.
        // (bc: poller loops on job end).

        // Note: this tests only with 1 "cannot run" job instance (used to be 2). That's because after a kill, the engine may only
        // take one
        // JI (and not more as expected) due to the heavy load failsafe - but one is guaranteed.

        addAndStartEngine();
        TestHelpers.waitForRunning(3, 3000, cnx);

        // Check 3 running (max queue size).
        Assert.assertEquals(3, jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.RUNNING).invoke().size());
        Assert.assertEquals(1, jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.SUBMITTED).invoke().size());

        // Kill 1.
        List<JobInstance> running = jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.RUNNING).invoke();
        jqmClient.killJob(running.get(0).getId());
        TestHelpers.waitFor(1, 10000, cnx);
        TestHelpers.waitForRunning(3, 10000, cnx);

        // Check the two waiting jobs have started.
        Assert.assertEquals(3, jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.RUNNING).invoke().size());
        Assert.assertEquals(0, jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.SUBMITTED).invoke().size());

        // Kill remaining
        running = jqmClient.newQuery().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.client.api.State.RUNNING).invoke();
        jqmClient.killJob(running.get(0).getId());
        jqmClient.killJob(running.get(1).getId());
        jqmClient.killJob(running.get(2).getId());

        // Check all jobs are killed (and not cancelled as they would have been if not started)).
        TestHelpers.waitFor(4, 10000, cnx);
        this.displayAllHistoryTable();
        Assert.assertEquals(4, jqmClient.newQuery().addStatusFilter(com.enioka.jqm.client.api.State.CRASHED).invoke().size());
    }

    @Test
    public void testPriority() throws Exception
    {
        // Single thread available.
        long qId = Queue.create(cnx, "testqueue", " ", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 1, 1, qId);

        CreationTools.createJobDef(null, true, "pyl.Wait", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", qId, 42, "jqm-test-wait",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        // Enqueue a low priority first, then a higher priority one. The higher priority should run first.
        long i1 = jqmClient.newJobRequest("jqm-test-wait", "test").addParameter("ms", "100").setPriority(1).enqueue();
        long i2 = jqmClient.newJobRequest("jqm-test-wait", "test").addParameter("ms", "100").setPriority(6).enqueue();

        addAndStartEngine();

        TestHelpers.waitFor(2, 60000, cnx);

        Assert.assertEquals(2, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getQueueRunningCount(cnx));

        JobInstance ji1 = jqmClient.newQuery().setJobInstanceId(i1).invoke().get(0);
        JobInstance ji2 = jqmClient.newQuery().setJobInstanceId(i2).invoke().get(0);

        Assert.assertEquals(1, (int) ji1.getPriority());
        Assert.assertEquals(6, (int) ji2.getPriority());

        Assert.assertTrue(ji1.getBeganRunningDate().compareTo(ji2.getEndDate()) >= 0);
    }

    @Test(expected = JqmInvalidRequestException.class)
    public void testPriorityLimits()
    {
        long qId = Queue.create(cnx, "testqueue", " ", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 1, 1, qId);

        CreationTools.createJobDef(null, true, "pyl.Wait", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", qId, 42, "jqm-test-wait",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        // No priority = FIFO queue.
        jqmClient.newJobRequest("jqm-test-wait", "test").addParameter("ms", "100").setPriority(Integer.MAX_VALUE).enqueue();
    }

    @Test
    public void testFifo() throws Exception
    {
        // Single thread available.
        long qId = Queue.create(cnx, "testqueue", " ", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 1, 1, qId);

        CreationTools.createJobDef(null, true, "pyl.Wait", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", qId, 42, "jqm-test-wait",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        // No priority = FIFO queue.
        long i1 = jqmClient.newJobRequest("jqm-test-wait", "test").addParameter("ms", "100").setPriority(null).enqueue();
        long i2 = jqmClient.newJobRequest("jqm-test-wait", "test").addParameter("ms", "100").setPriority(null).enqueue();

        addAndStartEngine();
        displayAllQueueTable();

        TestHelpers.waitFor(2, 60000, cnx);

        Assert.assertEquals(2, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getQueueRunningCount(cnx));

        JobInstance ji1 = jqmClient.newQuery().setJobInstanceId(i1).invoke().get(0);
        JobInstance ji2 = jqmClient.newQuery().setJobInstanceId(i2).invoke().get(0);

        displayAllHistoryTable();
        Assert.assertTrue(ji1.getBeganRunningDate().compareTo(ji2.getEndDate()) <= 0);
    }

    @Test
    public void testTakingMultipleResources() throws Exception
    {
        // Single thread available.
        Long qId = Queue.create(cnx, "testqueue", " ", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 2, 1, qId); // 2 slots

        Map<String, String> prms = new HashMap<>(1);
        prms.put("com.enioka.jqm.rm.quantity.thread.consumption", "2"); // using fully qualified RM with RM name 'thread' - not the
                                                                        // generic
                                                                        // key
        CreationTools.createJobDef(null, true, "pyl.Wait", prms, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", qId, 42,
                "jqm-test-wait-dual", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.Wait", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", qId, 42,
                "jqm-test-wait-single", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        long i1 = jqmClient.newJobRequest("jqm-test-wait-single", "test").setPriority(null).enqueue();
        long i2 = jqmClient.newJobRequest("jqm-test-wait-dual", "test").setPriority(null).enqueue();

        addAndStartEngine();

        TestHelpers.waitForRunning(1, 60000, cnx);
        sleep(1); // Time to start another if case of bug...

        // Check only one of the two JI has started (asking for total 3 slots, only 2 available)
        Assert.assertEquals(0, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertEquals(1, TestHelpers.getQueueRunningCount(cnx));
        Assert.assertEquals(2, TestHelpers.getQueueAllCount(cnx));

        // Cleanup
        jqmClient.killJob(i1);
        jqmClient.killJob(i2);
        TestHelpers.waitFor(2, 1000, cnx);
    }

    // Test if RM parameters are invisible to running JI.
    @Test
    public void testRmParameterCleanup() throws Exception
    {
        // Single thread available.
        long qId = Queue.create(cnx, "testqueue", " ", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 2, 1, qId); // 2 slots

        Map<String, String> prms = new HashMap<>(1);
        prms.put("com.enioka.jqm.rm.quantity.thread.consumption", "2");
        prms.put("whatever", "value");
        CreationTools.createJobDef(null, true, "pyl.MessagePerParameter", prms, "jqm-tests/jqm-test-pyl/target/test.jar", qId, 42,
                "jqm-test-kill", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        long i1 = jqmClient.newJobRequest("jqm-test-kill", "test").setPriority(null).enqueue();

        addAndStartEngine();
        TestHelpers.waitFor(1, 60000, cnx);

        // Check only one of the two JI has started (asking for total 3 slots, only 2 available)
        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getQueueRunningCount(cnx));
        Assert.assertEquals(0, TestHelpers.getQueueAllCount(cnx));

        // Check the parameter wazs removed by the RM
        JobInstance ji = jqmClient.getJob(i1);
        Assert.assertEquals(1, ji.getMessages().size());
    }

    // Test queue behaviour with a discrete RM.
    @Test
    public void testRmDiscrete() throws Exception
    {
        // Create queue
        long qId = Queue.create(cnx, "testqueue", " ", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 40, 1, qId); // 40 threads, so not the limiting factor.

        // Enable the global discrete RM.
        GlobalParameter.setParameter(cnx, "discreteRmName", "ports");
        GlobalParameter.setParameter(cnx, "discreteRmList", "port01,port02");

        Map<String, String> prms = new HashMap<>(1);
        prms.put("com.enioka.jqm.rm.discrete.consumption", "1");
        CreationTools.createJobDef(null, true, "pyl.KillMe", prms, "jqm-tests/jqm-test-pyl/target/test.jar", qId, 42, "jqm-test-kill", null,
                "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        jqmClient.newJobRequest("jqm-test-kill", "test").setPriority(null).enqueue();
        jqmClient.newJobRequest("jqm-test-kill", "test").setPriority(null).enqueue();
        jqmClient.newJobRequest("jqm-test-kill", "test").setPriority(null).enqueue();

        addAndStartEngine();
        TestHelpers.waitForRunning(2, 60000, cnx);
        sleep(1); // Time for bugs to happen.

        // Two should be running, a we have two slots
        Assert.assertEquals(2, TestHelpers.getQueueRunningCount(cnx));
        Assert.assertEquals(3, TestHelpers.getQueueAllCount(cnx));

        // Kill one, the last JI should start after the kill
        long toKill = jqmClient.newQuery().setQueryHistoryInstances(false).setQueryLiveInstances(true).addStatusFilter(State.RUNNING)
                .invoke().get(0).getId();
        jqmClient.killJob(toKill);

        TestHelpers.waitFor(1, 60000, cnx);
        TestHelpers.waitForRunning(2, 60000, cnx);

        Assert.assertEquals(2, TestHelpers.getQueueRunningCount(cnx));
        Assert.assertEquals(2, TestHelpers.getQueueAllCount(cnx));
        Assert.assertEquals(0, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(cnx));

        // Kill all to end the test.
        for (JobInstance ji : jqmClient.newQuery().setQueryHistoryInstances(false).setQueryLiveInstances(true)
                .addStatusFilter(State.RUNNING).invoke())
        {
            jqmClient.killJob(ji.getId());
        }

        TestHelpers.waitFor(3, 60000, cnx);

        // Check only one of the two JI has started (asking for total 3 slots, only 2 available)
        Assert.assertEquals(0, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(3, TestHelpers.getNonOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getQueueRunningCount(cnx));
        Assert.assertEquals(0, TestHelpers.getQueueAllCount(cnx));
    }

    @Test
    public void testStartingMultipleDefaultQueue()
    {
        // #364 : Configuration trap: cannot restart JQM after enabling two different default queues
        try
        {
            // Add another default queeue
            Queue.create(cnx, "AnotherDefaultQueue", " ", true);
            cnx.commit();
            addAndStartEngine();

            // Has only one default queue
            Assert.assertNotNull(TestHelpers.getDefaultQueueId(cnx));
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
    }
}
