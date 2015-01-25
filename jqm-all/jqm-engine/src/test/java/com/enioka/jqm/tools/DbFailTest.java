package com.enioka.jqm.tools;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.test.helpers.TestHelpers;

public class DbFailTest extends JqmBaseTest
{
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
        em.getTransaction().begin();
        TestHelpers.dpVip.setPollingInterval(3000);
        em.getTransaction().commit();

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
        JqmSimpleTest.create(em, "pyl.Wait", "jqm-test-pyl-nodep").addRuntimeParameter("p1", "4000").expectOk(0).run(this);
        this.sleep(2);

        jqmlogger.info("Stopping db");
        s.stop();
        this.waitDbStop();
        this.sleep(5);

        jqmlogger.info("Restarting DB");
        s.start();
        TestHelpers.waitFor(1, 10000, this.getNewEm());

        Assert.assertEquals(1, TestHelpers.getOkCount(this.getNewEm()));
    }

    // Job ends KO during db failure.
    @Test
    public void testDbFailureWithRunningJobKo() throws Exception
    {
        JqmSimpleTest.create(em, "pyl.KillMe").expectOk(0).run(this);
        this.sleep(2);

        jqmlogger.info("Stopping db");
        s.stop();
        this.waitDbStop();
        this.sleep(5);

        jqmlogger.info("Restarting DB");
        s.start();
        TestHelpers.waitFor(1, 10000, this.getNewEm());

        Assert.assertEquals(1, TestHelpers.getNonOkCount(this.getNewEm()));
    }
}
