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
import org.junit.Assume;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.RRole;
import com.enioka.jqm.model.RUser;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class MiscTest extends JqmBaseTest
{
    @Test
    public void testEmail() throws Exception
    {
        // Do not run in Eclipse, as it does not support the SMTP Maven plugin.
        Assume.assumeTrue(!System.getProperty("java.class.path").contains("eclipse"));

        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        JobRequest.create("MarsuApplication", "TestUser").setEmail("test@jqm.com").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx); // Need time for async mail sending.

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));

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
                "ModuleMachin", "other", "other", true, cnx);

        JobRequest j = new JobRequest("Marsu-Application-nnnnnnnn-nnnnnn-nnnnnnnnnn-nnNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNnn-nnnnnnnnn",
                "TestUser");
        JqmClientFactory.getClient().enqueue(j);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testJobWithSystemExit() throws Exception
    {
        JqmSimpleTest.create(cnx, "pyl.SecExit", "jqm-test-pyl-nodep").expectOk(0).expectNonOk(1).run(this);
    }

    @Test
    public void testJobWithPersistenceUnit() throws Exception
    {
        // The PU test expects an HSQLDB database which does not exist when running the tests on other databases
        Assume.assumeTrue(JqmBaseTest.s != null);

        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", cnx,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        CreationTools.createDatabaseProp("jdbc/jqm2", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/testdbengine", "SA", "", cnx,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);

        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-em/target/test.jar", TestHelpers.qVip, 42, "jqm-test-em",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        JobRequest.create("jqm-test-em", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(2, 10000, cnx);

        Assert.assertEquals(2, TestHelpers.getOkCount(cnx));
    }

    @Test
    public void testJobWithPersistenceUnitAndEngineApi() throws Exception
    {
        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", cnx,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        JqmSimpleTest.create(cnx, "pyl.CompatHibApi", "jqm-test-pyl-hibapi").expectOk(2).run(this);
    }

    @Test
    public void testJobWithPersistenceUnitAndEngineApiAndXmlParams() throws Exception
    {
        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", cnx,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        CreationTools.createDatabaseProp("jdbc/jqm2", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/testdbengine", "SA", "", cnx,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        cnx.commit();

        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmlstop.xml" });

        JobRequest j = new JobRequest("CompatHibApi", "TestUser");
        JqmClientFactory.getClient().enqueue(j);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
    }

    @Test
    public void testRemoteStop() throws Exception
    {
        Helpers.setSingleParam("internalPollingPeriodMs", "10", cnx);

        addAndStartEngine();

        cnx.runUpdate("node_update_stop_by_id", TestHelpers.node.getId());
        cnx.commit();

        TestHelpers.waitFor(2, 3000, cnx);
        Assert.assertFalse(engines.get("localhost").isAllPollersPolling());
        engines.clear();
    }

    @Test
    public void testNoDoubleStart() throws Exception
    {
        Helpers.setSingleParam("internalPollingPeriodMs", "500", cnx);
        Helpers.setSingleParam("disableVerboseStartup", "false", cnx);

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
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-em/target/test.jar", TestHelpers.qVip, 42, "jqm-test-em",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        /// Create a running job that should be cleaned at startup
        int i1 = JqmClientFactory.getClient().enqueue("jqm-test-em", "test");
        cnx.runUpdate("ji_update_poll", TestHelpers.node.getId(), TestHelpers.qVip, 10);
        cnx.runUpdate("jj_update_run_by_id", i1);
        cnx.commit();

        addAndStartEngine();

        Assert.assertEquals(0, TestHelpers.getQueueAllCount(cnx));
        Assert.assertEquals(1, TestHelpers.getHistoryAllCount(cnx));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testStartupCleanupAttr() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-em/target/test.jar", TestHelpers.qVip, 42, "jqm-test-em",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        // Create a running job that should be cleaned at startup
        JqmClientFactory.getClient().enqueue("jqm-test-em", "test");
        cnx.runUpdate("ji_update_poll", TestHelpers.node.getId(), TestHelpers.qVip, 10);
        cnx.commit();

        addAndStartEngine();

        Assert.assertEquals(0, TestHelpers.getQueueAllCount(cnx));
        Assert.assertEquals(1, TestHelpers.getHistoryAllCount(cnx));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testQuery() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal, 42,
                "jqm-test-kill", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();

        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");
        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");

        jqmlogger.debug("COUNT RUNNING " + cnx.runSelectSingle("ji_select_count_running", Integer.class));
        jqmlogger.debug("COUNT ALL     " + cnx.runSelectSingle("ji_select_count_all", Integer.class));
        Assert.assertEquals(0, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.RUNNING).addStatusFilter(com.enioka.jqm.api.State.ENDED).run().size());
        Assert.assertEquals(5, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.SUBMITTED).run().size());
    }

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
        JqmClientFactory.getClient().enqueue("jqm-test-kill", "test");

        // Scenario is: 5 jobs in queue with 3 slots. 3 jobs should run. 2 are then killed - 3 should still run.
        // (bc: poller loops on job end).
        addAndStartEngine();
        TestHelpers.waitForRunning(3, 3000, cnx);

        // Check 3 running (max queue size).
        Assert.assertEquals(3, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.RUNNING).run().size());
        Assert.assertEquals(2, Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.SUBMITTED).run().size());

        // Kill 2.
        List<JobInstance> running = Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false)
                .addStatusFilter(com.enioka.jqm.api.State.RUNNING).run();
        JqmClientFactory.getClient().killJob(running.get(0).getId());
        JqmClientFactory.getClient().killJob(running.get(1).getId());
        TestHelpers.waitFor(2, 10000, cnx);
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
        TestHelpers.waitFor(5, 10000, cnx);
        this.displayAllHistoryTable();
        Assert.assertEquals(5, Query.create().addStatusFilter(com.enioka.jqm.api.State.KILLED).run().size());
    }

    @Test
    public void testMultiLog() throws Exception
    {
        PrintStream out_ini = System.out;
        PrintStream err_ini = System.err;

        Helpers.setSingleParam("logFilePerLaunch", "true", cnx);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        String fileName = StringUtils.leftPad("" + i, 10, "0") + ".stdout.log";
        File f = new File(FilenameUtils.concat(((MultiplexPrintStream) System.out).rootLogDir, fileName));

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertTrue(f.exists());

        System.setErr(err_ini);
        System.setOut(out_ini);
    }

    @Test
    public void testSingleLauncher() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        cnx.runUpdate("ji_update_poll", TestHelpers.node.getId(), TestHelpers.qVip, 10);
        cnx.runUpdate("debug_jj_update_node_by_id", TestHelpers.node.getId(), i);
        cnx.commit();

        Main.main(new String[] { "-s", String.valueOf(i) });

        // This is not really a one shot JVM, so let's reset log4j
        LogManager.resetConfiguration();
        PropertyConfigurator.configure("target/classes/log4j.properties");

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testExternalLaunch() throws Exception
    {
        int jdId = CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip,
                42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        JobDef.setExternal(cnx, jdId);
        cnx.commit();
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testExternalKill() throws Exception
    {
        Helpers.setSingleParam("internalPollingPeriodMs", "100", cnx);
        int i = JqmSimpleTest.create(cnx, "pyl.KillMeNot").setExternal().addWaitTime(3000).expectNonOk(0).expectOk(0).run(this);

        JqmClientFactory.getClient().killJob(i);
        TestHelpers.waitFor(1, 20000, cnx);
        Assert.assertEquals(0, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testCliChangeUser()
    {
        Helpers.updateConfiguration(cnx);
        Main.main(new String[] { "-U", "myuser", "mypassword", "administrator", "client" });

        RUser u = RUser.selectlogin(cnx, "myuser");

        Assert.assertEquals(2, u.getRoles(cnx).size());
        boolean admin = false, client = false;
        for (RRole r : u.getRoles(cnx))
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
        Assert.assertEquals(1, u.getRoles(cnx).size());

        Main.main(new String[] { "-U", "myuser,mypassword,administrator,config admin" });
        Assert.assertEquals(2, u.getRoles(cnx).size());
    }

    @Test
    public void testMavenArtifact()
    {
        CreationTools.createJobDef(null, true, "pyl.Nothing", null, "com.enioka.jqm:jqm-test-pyl-nodep:1.3.2", TestHelpers.qVip, 42,
                "jqm-test-maven", null, "Franquin", "ModuleMachin", "other", "other", false, cnx, null, false, null, false, PathType.MAVEN);
        JobRequest.create("jqm-test-maven", null).submit();
        addAndStartEngine();

        TestHelpers.waitFor(1, 10000, cnx);
        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }
}
