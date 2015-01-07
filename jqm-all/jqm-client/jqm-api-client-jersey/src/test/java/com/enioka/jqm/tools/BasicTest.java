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
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.Deliverable;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.Queue;
import com.enioka.jqm.api.State;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class BasicTest
{
    public static Logger jqmlogger = Logger.getLogger(BasicTest.class);

    JqmEngine engine1;
    EntityManager em;
    public static org.hsqldb.Server s;

    @BeforeClass
    public static void testInit() throws Exception
    {
        JndiContext.createJndiContext();
        s = new org.hsqldb.Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();
    }

    @AfterClass
    public static void stop() throws NamingException
    {
        JqmClientFactory.resetClient(null);
        Helpers.resetEmf();
        ((JndiContext) NamingManager.getInitialContext(null)).resetSingletons();
        s.shutdown();
        s.stop();
    }

    @Before
    public void before() throws Exception
    {
        jqmlogger.debug("********* TEST INIT");

        em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createTestData(em);
        Helpers.setSingleParam("disableWsApi", "false", em);
        Helpers.setSingleParam("enableWsApiAuth", "true", em);
        Helpers.setSingleParam("enableWsApiSsl", "false", em);
        File jar = FileUtils.listFiles(new File("../../jqm-ws/target/"), new String[] { "war" }, false).iterator().next();
        FileUtils.copyFile(jar, new File("./webapp/jqm-ws.war"));

        em.getTransaction().begin();
        Node n = em.find(Node.class, TestHelpers.node.getId());
        em.createQuery("UPDATE GlobalParameter gp set gp.value='true' WHERE gp.key = 'logFilePerLaunch'").executeUpdate();
        n.setRepo("./../..");

        TestHelpers.node.setLoadApiAdmin(true);
        TestHelpers.node.setLoadApiClient(true);
        TestHelpers.node.setLoapApiSimple(true);
        em.getTransaction().commit();

        engine1 = new JqmEngine();
        engine1.start("localhost");

        // Test user
        RRole r = em.createQuery("SELECT rr from RRole rr WHERE rr.name = :r", RRole.class).setParameter("r", "client power user")
                .getSingleResult();
        CreationTools.createUser(em, "test", "test", r);

        Properties p = new Properties();
        em.refresh(n);
        System.out.println(n.getPort());
        p.put("com.enioka.jqm.ws.url", "http://" + n.getDns() + ":" + n.getPort() + "/ws/client");
        p.put("com.enioka.jqm.ws.login", "test");
        p.put("com.enioka.jqm.ws.password", "test");
        JqmClientFactory.setProperties(p);
    }

    @After
    public void after()
    {
        jqmlogger.debug("********* TEST CLEANUP");
        engine1.stop();
        em.close();
        JqmClientFactory.resetClient();

        System.runFinalization();
        System.gc();
        System.runFinalization();
        System.gc();
        System.gc();
    }

    @Test
    public void testStartServerWithWS() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testStartServerWithWS");
    }

    @Test
    public void testWsGetFiles() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testWsGetDeliverable");

        CreationTools.createJobDef("super app", true, "pyl.EngineApiInjectThread", null, "jqm-tests/jqm-test-pyl/target/test.jar",
                TestHelpers.qVip, 42, "jqm-test-runnable-inject", "testapp", "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest j = new JobRequest("jqm-test-runnable-inject", "MAG");
        int i = JqmClientFactory.getClient().enqueue(j);
        TestHelpers.waitFor(3, 10000, em);

        List<Deliverable> dels = JqmClientFactory.getClient().getJobDeliverables(i);

        Assert.assertEquals(1, dels.size());

        InputStream is = JqmClientFactory.getClient().getDeliverableContent(dels.get(0));
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF8");
        String deliverable = writer.toString();
        Assert.assertTrue(deliverable.startsWith("The first line"));

        is = JqmClientFactory.getClient().getJobLogStdErr(i);
        Assert.assertEquals(-1, is.read());
        is.close();

        is = JqmClientFactory.getClient().getJobLogStdOut(i);
        writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF8");
        String stdout = writer.toString();
        Assert.assertTrue(stdout.contains("A loader/runner thread has just started for Job Instance"));
    }

    @Test
    public void testWsMisc() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testWsMisc");

        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        JobRequest j = new JobRequest("MarsuApplication", "MAG");

        // Enqueue & getJobs & getActiveJobs & getUserActiveJobs
        int i = JqmClientFactory.getClient().enqueue(j);
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertTrue(JqmClientFactory.getClient().getJob(i).getState().equals(State.ENDED));

        Assert.assertEquals(0, JqmClientFactory.getClient().getActiveJobs().size());
        Assert.assertEquals(1, JqmClientFactory.getClient().getJobs().size());
        Assert.assertEquals(1, JqmClientFactory.getClient().getUserActiveJobs("MAG").size());

        // Kill test
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "KillMe", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        j = new JobRequest("KillMe", "MAG");
        i = JqmClientFactory.getClient().enqueue(j);
        Assert.assertEquals(2, JqmClientFactory.getClient().getUserActiveJobs("MAG").size());
        Assert.assertTrue(!JqmClientFactory.getClient().getJob(i).getState().equals(State.ENDED));

        JqmClientFactory.getClient().killJob(i);
        Assert.assertTrue(JqmClientFactory.getClient().getJob(i).getState().equals(State.KILLED));

        // Change position and cancel test
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal3, 42,
                "KillMe2", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        j = new JobRequest("KillMe2", "TEST2");
        i = JqmClientFactory.getClient().enqueue(j);
        Thread.sleep(1000);
        Assert.assertEquals(1, JqmClientFactory.getClient().getUserActiveJobs("TEST2").size());
        Assert.assertEquals(State.SUBMITTED, JqmClientFactory.getClient().getJob(i).getState());

        JqmClientFactory.getClient().setJobQueuePosition(i, 12);
        JqmClientFactory.getClient().cancelJob(i);
        Assert.assertTrue(JqmClientFactory.getClient().getJob(i).getState().equals(State.CANCELLED));

        // Change queue
        i = JqmClientFactory.getClient().enqueue(j);
        List<Queue> queues = JqmClientFactory.getClient().getQueues();
        Queue newQueue = null;
        for (Queue q : queues)
        {
            if (q.getName().equals("VIPQueue"))
            {
                newQueue = q;
                break;
            }
        }
        JqmClientFactory.getClient().setJobQueue(i, newQueue);
        Thread.sleep(500);
        Assert.assertEquals(State.RUNNING, JqmClientFactory.getClient().getJob(i).getState());
        JqmClientFactory.getClient().killJob(i);
        Assert.assertTrue(JqmClientFactory.getClient().getJob(i).getState().equals(State.KILLED));
        Thread.sleep(500);

        // Get messages too
        Assert.assertEquals(1, JqmClientFactory.getClient().getJobMessages(i).size());

        // Finally, a query
        Assert.assertEquals(1, Query.create().setApplicationName("MarsuApplication").run().size());
    }
    // enqueue, list, cancel

}
