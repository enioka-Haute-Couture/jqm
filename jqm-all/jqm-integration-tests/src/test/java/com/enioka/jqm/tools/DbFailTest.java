package com.enioka.jqm.tools;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class DbFailTest extends JqmBaseTest
{
    @Before
    public void before()
    {
        // These tests are HSQLDB dependent.
        Assume.assumeTrue(JqmBaseTest.s != null);

        // TODO: write some tests for PGSQL.
    }

    @Test
    public void testDbFailure() throws Exception
    {
        this.addAndStartEngine();
        jqmlogger.info("Stopping db");
        s.stop();
        this.waitDbStop();
        this.sleep(2);
        jqmlogger.info("Restarting DB");
        s.start();
        this.sleep(5);
        Assert.assertTrue(this.engines.get("localhost").areAllPollersPolling());
    }

    @Test
    public void testDbDoubleFailure() throws Exception
    {
        this.addAndStartEngine();
        jqmlogger.info("Stopping db");
        s.stop();
        this.waitDbStop();
        this.sleep(2);
        jqmlogger.info("Restarting DB");
        s.start();
        this.sleep(5);
        jqmlogger.info("Stopping db");
        s.stop();
        this.waitDbStop();
        this.sleep(2);
        jqmlogger.info("Restarting DB");
        s.start();
        this.sleep(5);
    }

    @Test
    public void testDbTranscientFailure() throws Exception
    {
        // Set a real polling interval to allow the failure to be unseen by the poller
        cnx.runUpdate("dp_update_interval_by_id", 3000, TestHelpers.dpVip.getId());
        cnx.commit();

        this.addAndStartEngine();
        this.sleep(2); // first poller loop

        jqmlogger.info("Stopping db");
        s.stop();
        this.waitDbStop();
        jqmlogger.info("Restarting DB (as soon as possible)");
        s.start();
        this.sleep(5);
    }

    // Job ends OK during db failure.
    @Test
    public void testDbFailureWithRunningJob() throws Exception
    {
        JqmSimpleTest.create(cnx, "pyl.Wait", "jqm-test-pyl-nodep").addRuntimeParameter("p1", "4000").expectOk(0).run(this);
        this.sleep(2);

        jqmlogger.info("Stopping db");
        s.stop();
        this.waitDbStop();
        this.sleep(5);

        jqmlogger.info("Restarting DB");
        s.start();
        TestHelpers.waitFor(1, 10000, this.getNewDbSession());

        Assert.assertEquals(1, TestHelpers.getOkCount(this.getNewDbSession()));
    }

    // Job ends KO during db failure.
    @Test
    public void testDbFailureWithRunningJobKo() throws Exception
    {
        JqmSimpleTest.create(cnx, "pyl.KillMe").expectOk(0).run(this);
        this.sleep(2);

        jqmlogger.info("Stopping db");
        s.stop();
        this.waitDbStop();
        this.sleep(5);

        jqmlogger.info("Restarting DB");
        s.start();
        TestHelpers.waitFor(1, 10000, this.getNewDbSession());

        Assert.assertEquals(1, TestHelpers.getNonOkCount(this.getNewDbSession()));
    }

    // Many jobs starting & running during failure
    @Test
    public void testDbFailureUnderLoad() throws Exception
    {
        // Many starting jobs simultaneously
        cnx.runUpdate("dp_update_threads_by_id", 50, TestHelpers.dpVip.getId());
        cnx.commit();
        TestHelpers.setNodesLogLevel("INFO", cnx);

        CreationTools.createJobDef(null, true, "pyl.Nothing", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", TestHelpers.qVip, -1,
                "TestJqmApplication", "appFreeName", "TestModule", "kw1", "kw2", "kw3", false, cnx);

        JobRequest j = new JobRequest("TestJqmApplication", "TestUser");
        for (int i = 0; i < 1000; i++)
        {
            JqmClientFactory.getClient().enqueue(j);
        }

        addAndStartEngine();

        this.sleep(1);
        jqmlogger.info("Stopping db");
        simulateDbFailure();

        TestHelpers.waitFor(1000, 120000, this.getNewDbSession());

        Assert.assertEquals(1000, TestHelpers.getOkCount(this.getNewDbSession()));
        // Assert.assertTrue(this.engines.get("localhost").isAllPollersPolling());
    }
}
