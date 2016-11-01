/**
 * Copyright © 2013 enioka. All rights reserved
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

import java.io.File;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDef.PathType;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.jpamodel.RUser;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class MiscTest extends JqmBaseTest
{
    @Test
    public void testEmail() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        JobRequest.create("MarsuApplication", "TestUser").setEmail("test@jqm.com").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, em); // Need time for async mail sending.

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        int nbMail = 0;
        try
        {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("localhost", 10143, "testlogin", "testpassword");
            Folder inbox = store.getFolder("INBOX");
            nbMail = inbox.getMessageCount();
        }
        catch (Exception mex)
        {
            mex.printStackTrace();
        }
        Assert.assertEquals(1, nbMail);
    }

    @Test
    public void testLongName() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "Marsu-Application-nnnnnnnn-nnnnnn-nnnnnnnnnn-nnNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNnn-nnnnnnnnn", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest("Marsu-Application-nnnnnnnn-nnnnnn-nnnnnnnnnn-nnNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNnn-nnnnnnnnn",
                "TestUser");
        JqmClientFactory.getClient().enqueue(j);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testJobWithSystemExit() throws Exception
    {
        JqmSimpleTest.create(em, "pyl.SecExit", "jqm-test-pyl-nodep").expectOk(0).expectNonOk(1).run(this);
    }

    @Test
    public void testJobWithPersistenceUnit() throws Exception
    {
        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", em,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        CreationTools.createDatabaseProp("jdbc/jqm2", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/testdbengine", "SA", "", em,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);

        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-em/target/test.jar", TestHelpers.qVip, 42, "jqm-test-em",
                null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("jqm-test-em", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(2, 10000, em);

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j order by id asc", History.class).getResultList();
        Assert.assertEquals(2, ji.size());
        Assert.assertEquals(State.ENDED, ji.get(0).getState());
        Assert.assertEquals(State.ENDED, ji.get(1).getState());
    }

    @Test
    public void testJobWithPersistenceUnitAndEngineApi() throws Exception
    {
        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", em,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        JqmSimpleTest.create(em, "pyl.CompatHibApi", "jqm-test-pyl-hibapi").expectOk(2).run(this);
    }

    @Test
    public void testJobWithPersistenceUnitAndEngineApiAndXmlParams() throws Exception
    {
        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", em,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        CreationTools.createDatabaseProp("jdbc/jqm2", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/testdbengine", "SA", "", em,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);

        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmlstop.xml" });

        JobRequest j = new JobRequest("CompatHibApi", "TestUser");
        JqmClientFactory.getClient().enqueue(j);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        List<History> ji = Helpers.getNewEm()
                .createQuery("SELECT j FROM History j WHERE j.jd.applicationName = :myId order by id asc", History.class)
                .setParameter("myId", "CompatHibApi").getResultList();
        Assert.assertEquals(1, ji.size());
        Assert.assertEquals(State.ENDED, ji.get(0).getState());
    }

    @Test
    public void testRemoteStop() throws Exception
    {
        Helpers.setSingleParam("internalPollingPeriodMs", "10", em);

        addAndStartEngine();

        em.getTransaction().begin();
        TestHelpers.node.setStop(true);
        em.getTransaction().commit();

        TestHelpers.waitFor(2, 3000, em);
        Assert.assertFalse(engines.get("localhost").isAllPollersPolling());
        engines.clear();
    }

    @Test
    public void testNoDoubleStart() throws Exception
    {
        Helpers.setSingleParam("internalPollingPeriodMs", "500", em);
        Helpers.setSingleParam("disableVerboseStartup", "false", em);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

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
    public void testStartupCleanupRunning() throws Exception
    {
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

        addAndStartEngine();

        Assert.assertEquals(0, em.createQuery("SELECT ji FROM JobInstance ji", JobInstance.class).getResultList().size());
        Assert.assertEquals(1, em.createQuery("SELECT ji FROM History ji", History.class).getResultList().size());
        Assert.assertEquals(State.CRASHED, em.createQuery("SELECT ji FROM History ji", History.class).getResultList().get(0).getState());
    }

    @Test
    public void testStartupCleanupAttr() throws Exception
    {
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
        ji.setState(State.ATTRIBUTED);
        em.persist(ji);
        em.getTransaction().commit();

        addAndStartEngine();

        Assert.assertEquals(0, em.createQuery("SELECT ji FROM JobInstance ji", JobInstance.class).getResultList().size());
        Assert.assertEquals(1, em.createQuery("SELECT ji FROM History ji", History.class).getResultList().size());
        Assert.assertEquals(State.CRASHED, em.createQuery("SELECT ji FROM History ji", History.class).getResultList().get(0).getState());
    }

    @Test
    public void testQueueWidth() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "jqm-test-kill", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        // Only 3 threads
        em.getTransaction().begin();
        TestHelpers.dpVip.setNbThread(3);
        TestHelpers.dpVip.setPollingInterval(1);
        em.getTransaction().commit();

        int i1 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i2 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i3 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i4 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i5 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");

        addAndStartEngine();

        // Scenario is: 5 jobs in queue. 3 should run. 2 are then killed - 3 should still run.
        Thread.sleep(3000);

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

        TestHelpers.waitFor(5, 10000, em);
        Assert.assertEquals(5, Query.create().addStatusFilter(com.enioka.jqm.api.State.KILLED).run().size());
    }

    // Does the poller take multiple JI on each loop?
    @Test
    public void testQueuePollWidth() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "jqm-test-kill", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        // Only 3 threads, one poll every hour
        em.getTransaction().begin();
        TestHelpers.dpVip.setNbThread(3);
        TestHelpers.dpVip.setPollingInterval(3600000);
        em.getTransaction().commit();

        int i1 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i2 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i3 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i4 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        int i5 = JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");

        addAndStartEngine();

        // Scenario is: 5 jobs in queue. 3 should run. 2 are then killed - 3 should still run.
        Thread.sleep(3000);

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

        TestHelpers.waitFor(5, 10000, em);
        Assert.assertEquals(5, Query.create().addStatusFilter(com.enioka.jqm.api.State.KILLED).run().size());
    }

    @Test
    public void testMultiLog() throws Exception
    {
        PrintStream out_ini = System.out;
        PrintStream err_ini = System.err;

        Helpers.setSingleParam("logFilePerLaunch", "true", em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, em);

        String fileName = StringUtils.leftPad("" + i, 10, "0") + ".stdout.log";
        File f = new File(FilenameUtils.concat(((MultiplexPrintStream) System.out).rootLogDir, fileName));

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
        Assert.assertTrue(f.exists());

        System.setErr(err_ini);
        System.setOut(out_ini);
    }

    @Test
    public void testSingleLauncher() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        em.getTransaction().begin();
        em.createQuery("UPDATE JobInstance ji set ji.node = :n").setParameter("n", TestHelpers.node).executeUpdate();
        em.getTransaction().commit();

        Main.main(new String[] { "-s", String.valueOf(i) });

        // This is not really a one shot JVM, so let's reset log4j
        LogManager.resetConfiguration();
        PropertyConfigurator.configure("target/classes/log4j.properties");

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testExternalLaunch() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        em.getTransaction().begin();
        em.createQuery("UPDATE JobDef ji set ji.external = true").executeUpdate();
        em.getTransaction().commit();
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testExternalKill() throws Exception
    {
        Helpers.setSingleParam("internalPollingPeriodMs", "100", em);
        int i = JqmSimpleTest.create(em, "pyl.KillMeNot").setExternal().addWaitTime(3000).expectNonOk(0).expectOk(0).run(this);

        JqmClientFactory.getClient().killJob(i);
        TestHelpers.waitFor(1, 20000, em);
        Assert.assertEquals(0, TestHelpers.getOkCount(em));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testCliChangeUser()
    {
        Helpers.updateConfiguration(em);
        Main.main(new String[] { "-U", "myuser", "mypassword", "administrator", "client" });

        RUser u = em.createQuery("SELECT u FROM RUser u WHERE u.login = :l", RUser.class).setParameter("l", "myuser").getSingleResult();

        Assert.assertEquals(2, u.getRoles().size());
        boolean admin = false, client = false;
        for (RRole r : u.getRoles())
        {
            if (r.getName().equals("administrator"))
            {
                admin = true;
            }
            if (r.getName().equals("client"))
            {
                client = true;
            }
        }
        Assert.assertTrue(client && admin);

        Main.main(new String[] { "-U", "myuser", "mypassword", "administrator" });
        em.refresh(u);
        Assert.assertEquals(1, u.getRoles().size());

        Main.main(new String[] { "-U", "myuser,mypassword,administrator,config admin" });
        em.refresh(u);
        Assert.assertEquals(2, u.getRoles().size());
    }

    @Test
    public void testMavenArtifact()
    {
        CreationTools.createJobDef(null, true, "pyl.Nothing", null, "com.enioka.jqm:jqm-test-pyl-nodep:1.3.2", TestHelpers.qVip, 42,
                "jqm-test-maven", null, "Franquin", "ModuleMachin", "other", "other", false, em, null, false, null, false, PathType.MAVEN);
        JobRequest.create("jqm-test-maven", null).submit();
        addAndStartEngine();

        TestHelpers.waitFor(1, 10000, em);
        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }
}
