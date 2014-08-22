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
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class MiscTest extends JqmBaseTest
{
    @Test
    public void testEmail() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testSendEmail");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-datetimemaven/target/test.jar",
                TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest("MarsuApplication", "MAG", "jqm.noreply@gmail.com");

        @SuppressWarnings("unused")
        int i = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(2, 7000, em); // Need time for async mail sending.
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testLongName() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testLongName");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-datetimemaven/target/test.jar",
                TestHelpers.qVip, 42,
                "Marsu-Application-nnnnnnnn-nnnnnn-nnnnnnnnnn-nnNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNnn-nnnnnnnnn", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest(
                "Marsu-Application-nnnnnnnn-nnnnnn-nnnnnnnnnn-nnNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNnn-nnnnnnnnn", "MAG");

        @SuppressWarnings("unused")
        int i = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000, em);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testJobWithSystemExit() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testJobWithSystemExit");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-exit/target/test.jar", TestHelpers.qVip, 42,
                "jqm-test-exit", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("jqm-test-exit", "MAG");
        JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000, em);
        engine1.stop();

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId order by id asc", History.class)
                .setParameter("myId", jd.getId()).getResultList();

        Assert.assertEquals(1, ji.size());
        Assert.assertEquals(State.CRASHED, ji.get(0).getState());
    }

    @Test
    public void testJobWithPersistenceUnit() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testJobWithPersistenceUnit");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", em,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        CreationTools.createDatabaseProp("jdbc/jqm2", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/testdbengine", "SA", "", em,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-em/target/test.jar", TestHelpers.qVip, 42,
                "jqm-test-em", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("jqm-test-em", "MAG");
        JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(2, 10000, em);
        engine1.stop();

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId order by id asc", History.class)
                .setParameter("myId", jd.getId()).getResultList();

        Assert.assertEquals(2, ji.size());
        Assert.assertEquals(State.ENDED, ji.get(0).getState());
        Assert.assertEquals(State.ENDED, ji.get(1).getState());
    }

    @Test
    public void testRemoteStop() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testRemoteStop");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        em.getTransaction().begin();
        GlobalParameter gp = em.createQuery("SELECT n from GlobalParameter n WHERE n.key = 'internalPollingPeriodMs'",
                GlobalParameter.class).getSingleResult();
        gp.setValue("10");
        em.getTransaction().commit();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        em.getTransaction().begin();
        TestHelpers.node.setStop(true);
        em.getTransaction().commit();

        TestHelpers.waitFor(2, 3000, em);
        Assert.assertFalse(engine1.isAllPollersPolling());
    }

    @Test
    public void testNoDoubleStart() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testNoDoubleStart");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        em.getTransaction().begin();
        GlobalParameter gp = em.createQuery("SELECT n from GlobalParameter n WHERE n.key = 'internalPollingPeriodMs'",
                GlobalParameter.class).getSingleResult();
        gp.setValue("50");
        gp = em.createQuery("SELECT n from GlobalParameter n WHERE n.key = 'aliveSignalMs'", GlobalParameter.class).getSingleResult();
        gp.setValue("50");
        em.getTransaction().commit();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        Thread.sleep(200);

        JqmEngine engine2 = new JqmEngine();

        try
        {
            engine2.start("localhost");
            Assert.fail("engine should not have been able to start");
        }
        catch (JqmInitErrorTooSoon e)
        {
            jqmlogger.info(e);
        }
        finally
        {
            engine1.stop();
        }
    }

    @Test
    public void testStartupCleanup() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testStartupCleanup");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);
        JobDef jd = CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-em/target/test.jar", TestHelpers.qVip, 42,
                "jqm-test-em", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        // Create a running job that should be cleaned at startup
        em.getTransaction().begin();
        JobInstance ji = new JobInstance();
        ji.setApplication("marsu");
        ji.setAttributionDate(Calendar.getInstance());
        ji.setCreationDate(Calendar.getInstance());
        ji.setExecutionDate(Calendar.getInstance());
        ji.setInternalPosition(0);
        ji.setJd(jd);
        ji.setNode(TestHelpers.node);
        ji.setQueue(TestHelpers.qVip);
        ji.setState(State.RUNNING);
        em.persist(ji);
        em.getTransaction().commit();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        engine1.stop();

        Assert.assertEquals(0, em.createQuery("SELECT ji FROM JobInstance ji", JobInstance.class).getResultList().size());
        Assert.assertEquals(1, em.createQuery("SELECT ji FROM History ji", History.class).getResultList().size());
        Assert.assertEquals(State.CRASHED, em.createQuery("SELECT ji FROM History ji", History.class).getResultList().get(0).getState());
    }

}
