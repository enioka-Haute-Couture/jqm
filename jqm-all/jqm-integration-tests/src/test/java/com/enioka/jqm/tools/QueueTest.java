package com.enioka.jqm.tools;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.JqmInvalidRequestException;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

/**
 * All tests directly concerning the QueuePoller.
 */
public class QueueTest extends JqmBaseTest
{
    @Test
    public void testQueueWidth() throws Exception
    {
        // Only 3 threads
        int qId = Queue.create(cnx, "testqueue", "", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 3, 1, qId);

        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", qId, 42, "jqm-test-kill", null,
                "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        int i1 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i2 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i3 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i4 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i5 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");

        addAndStartEngine();

        // Scenario is: 5 jobs in queue. 3 should run. 2 are then killed - 3 should still run.
        Thread.sleep(3000);

        jqmlogger.debug("COUNT RUNNING " + cnx.runSelectSingle("ji_select_count_running", Integer.class));
        jqmlogger.debug("COUNT ALL     " + cnx.runSelectSingle("ji_select_count_all", Integer.class));
        Assert.assertEquals(3, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.RUNNING).run().size());
        Assert.assertEquals(2, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.SUBMITTED).run().size());

        JqmClientFactory.getClient().killJob(i1);
        JqmClientFactory.getClient().killJob(i2);

        Thread.sleep(2000);

        Assert.assertEquals(3, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.RUNNING).run().size());
        Assert.assertEquals(0, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.SUBMITTED).run().size());

        JqmClientFactory.getClient().killJob(i3);
        JqmClientFactory.getClient().killJob(i4);
        JqmClientFactory.getClient().killJob(i5);

        TestHelpers.waitFor(5, 10000, cnx);
    }

    // Does the poller take multiple JI on each loop?
    @Test
    public void testQueuePollWidth() throws Exception
    {
        // Only 3 threads, one poll every hour
        int qId = Queue.create(cnx, "testqueue", "", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 3, 3600000, qId);

        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", qId, 42, "jqm-test-kill", null,
                "Franquin", "ModuleMachin", "other", "other", false, cnx);

        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");

        // Scenario is: 4 jobs in queue with 3 slots. 3 jobs should run. 1 is then killed - 3 should still run.
        // (bc: poller loops on job end).

        // Note: this tests only with 1 "cannot run" job instance (used to be 2). That's because after a kill, the engine may only take one
        // JI (and not more as expected) due to the heavy load failsafe - but one is guaranteed.

        addAndStartEngine();
        TestHelpers.waitForRunning(3, 3000, cnx);

        // Check 3 running (max queue size).
        Assert.assertEquals(3, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.RUNNING).run().size());
        Assert.assertEquals(1, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.SUBMITTED).run().size());

        // Kill 1.
        List<JobInstance> running = Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.RUNNING).run();
        JqmClientFactory.getClient().killJob(running.get(0).getId());
        TestHelpers.waitFor(1, 10000, cnx);
        TestHelpers.waitForRunning(3, 10000, cnx);

        // Check the two waiting jobs have started.
        Assert.assertEquals(3, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.RUNNING).run().size());
        Assert.assertEquals(0, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.SUBMITTED).run().size());

        // Kill remaining
        running = Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.RUNNING).run();
        JqmClientFactory.getClient().killJob(running.get(0).getId());
        JqmClientFactory.getClient().killJob(running.get(1).getId());
        JqmClientFactory.getClient().killJob(running.get(2).getId());

        // Check all jobs are killed (and not cancelled as they would have been if not started)).
        TestHelpers.waitFor(4, 10000, cnx);
        this.displayAllHistoryTable();
        Assert.assertEquals(4, Query.create().addStatusFilter(com.enioka.jqm.api.State.CRASHED).run().size());
    }

    @Test
    public void testPriority() throws Exception
    {
        // Single thread available.
        int qId = Queue.create(cnx, "testqueue", "", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 1, 1, qId);

        CreationTools.createJobDef(null, true, "pyl.Wait", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", qId, 42, "jqm-test-wait",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        // Enqueue a low priority first, then a higher priority one. The higher priority should run first.
        int i1 = JobRequest.create("jqm-test-wait", "test").addParameter("ms", "100").setPriority(1).submit();
        int i2 = JobRequest.create("jqm-test-wait", "test").addParameter("ms", "100").setPriority(6).submit();

        addAndStartEngine();

        TestHelpers.waitFor(2, 60000, cnx);

        Assert.assertEquals(2, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getQueueRunningCount(cnx));

        JobInstance ji1 = Query.create().setJobInstanceId(i1).run().get(0);
        JobInstance ji2 = Query.create().setJobInstanceId(i2).run().get(0);

        Assert.assertEquals(1, (int) ji1.getPriority());
        Assert.assertEquals(6, (int) ji2.getPriority());

        Assert.assertTrue(ji1.getBeganRunningDate().compareTo(ji2.getEndDate()) >= 0);
    }

    @Test(expected = JqmInvalidRequestException.class)
    public void testPriorityLimits()
    {
        int qId = Queue.create(cnx, "testqueue", "", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 1, 1, qId);

        CreationTools.createJobDef(null, true, "pyl.Wait", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", qId, 42, "jqm-test-wait",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        // No priority = FIFO queue.
        JobRequest.create("jqm-test-wait", "test").addParameter("ms", "100").setPriority(Integer.MAX_VALUE).submit();
    }

    @Test
    public void testFifo() throws Exception
    {
        // Single thread available.
        int qId = Queue.create(cnx, "testqueue", "", false);
        DeploymentParameter.create(cnx, TestHelpers.node.getId(), 1, 1, qId);

        CreationTools.createJobDef(null, true, "pyl.Wait", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", qId, 42, "jqm-test-wait",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        // No priority = FIFO queue.
        int i1 = JobRequest.create("jqm-test-wait", "test").addParameter("ms", "100").setPriority(null).submit();
        int i2 = JobRequest.create("jqm-test-wait", "test").addParameter("ms", "100").setPriority(null).submit();

        addAndStartEngine();

        TestHelpers.waitFor(2, 60000, cnx);

        Assert.assertEquals(2, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getQueueRunningCount(cnx));

        JobInstance ji1 = Query.create().setJobInstanceId(i1).run().get(0);
        JobInstance ji2 = Query.create().setJobInstanceId(i2).run().get(0);

        Assert.assertTrue(ji1.getBeganRunningDate().compareTo(ji2.getEndDate()) <= 0);
    }

}
