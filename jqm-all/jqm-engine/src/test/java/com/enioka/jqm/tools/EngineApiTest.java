package com.enioka.jqm.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class EngineApiTest extends JqmBaseTest
{
    @Test
    public void testSendMsg() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testSendMsg");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);
        boolean success = false;
        boolean success2 = false;
        boolean success3 = false;

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-sendmsg/target/test.jar",
                TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest("MarsuApplication", "MAG");

        int i = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000, em);

        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        ArrayList<Message> m = (ArrayList<Message>) em.createQuery("SELECT m FROM Message m WHERE m.ji = :i", Message.class)
                .setParameter("i", i).getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());

        for (Message msg : m)
        {
            if (msg.getTextMessage().equals("Les marsus sont nos amis, il faut les aimer aussi!"))
            {
                success = true;
            }
            if (msg.getTextMessage().equals("Les marsus sont nos amis, il faut les aimer aussi!2"))
            {
                success2 = true;
            }
            if (msg.getTextMessage().equals("Les marsus sont nos amis, il faut les aimer aussi!3"))
            {
                success3 = true;
            }
        }

        Assert.assertEquals(true, success);
        Assert.assertEquals(true, success2);
        Assert.assertEquals(true, success3);
    }

    @Test
    public void testSendProgress() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testSendProgress");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-sendprogress/target/test.jar",
                TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest("MarsuApplication", "MAG");

        @SuppressWarnings("unused")
        int i = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 20000, em);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals((Integer) 50, res.get(0).getProgress());
    }

    /**
     * To stop a job, just throw an exception
     */
    @Test
    public void testThrowable() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testThrowable");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-throwable/target/test.jar", TestHelpers.qVip,
                42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("MarsuApplication", "MAG");
        JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);
        engine1.stop();

        History ji1 = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId", History.class)
                .setParameter("myId", jd.getId()).getSingleResult();

        Assert.assertEquals(State.CRASHED, ji1.getState());
    }

    /**
     * A parent job can wait for all its children - then its end date should be after the end date of the children.
     */
    @Test
    public void testWaitChildren() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testWaitChildren");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-waitall/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("MarsuApplication", "MAG");
        JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(6, 10000, em);
        engine1.stop();

        List<History> ji = Helpers.getNewEm()
                .createQuery("SELECT j FROM History j WHERE j.status = 'ENDED' ORDER BY j.id ASC", History.class).getResultList();
        Assert.assertEquals(6, ji.size());

        Calendar parentEnd = ji.get(0).getEndDate();
        for (int i = 1; i < 6; i++)
        {
            Assert.assertTrue(parentEnd.after(ji.get(i).getEndDate()));
        }
    }

    @Test
    public void testGetChildrenStatus() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetChildrenStatus");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-apienginestatus/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("MarsuApplication", "MAG");
        JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(3, 10000, em);
        engine1.stop();

        List<History> ji = Helpers.getNewEm()
                .createQuery("SELECT j FROM History j WHERE j.status = 'ENDED' ORDER BY j.id ASC", History.class).getResultList();
        Assert.assertEquals(2, ji.size());
        ji = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.status = 'CRASHED' ORDER BY j.id ASC", History.class)
                .getResultList();
        Assert.assertEquals(1, ji.size());
    }

}
