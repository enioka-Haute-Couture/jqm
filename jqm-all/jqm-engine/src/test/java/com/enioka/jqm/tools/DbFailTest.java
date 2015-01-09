package com.enioka.jqm.tools;

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

}
