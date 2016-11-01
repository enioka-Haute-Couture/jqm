/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class MultiNodeTest extends JqmBaseTest
{
    @Before
    public void b()
    {
        TestHelpers.setNodesLogLevel("INFO", em);
    }

    @After
    public void a()
    {
        Logger.getRootLogger().setLevel(Level.toLevel("DEBUG"));
        Logger.getLogger("com.enioka").setLevel(Level.toLevel("DEBUG"));
    }

    @Test
    public void testOneQueueTwoNodes() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest j11 = new JobRequest("AppliNode1-1", "TestUser");
        for (int i = 0; i < 10; i++)
        {
            JqmClientFactory.getClient().enqueue(j11);
        }

        addAndStartEngine("localhost");
        addAndStartEngine("localhost4");

        for (int j = 0; j < 3; j++)
        {
            for (int i = 0; i < 10; i++)
            {
                JqmClientFactory.getClient().enqueue(j11);
            }
            Thread.sleep(200);
        }

        TestHelpers.waitFor(40, 60000, em);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(40, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());
        Assert.assertEquals(40, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        // Ran on both nodes?
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost48'", Long.class).getSingleResult() == 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost4'", Long.class).getSingleResult() > 0L);
    }

    @Test
    public void testOneQueueThreeNodes() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest j11 = new JobRequest("AppliNode1-1", "TestUser");
        for (int i = 0; i < 10; i++)
        {
            JqmClientFactory.getClient().enqueue(j11);
        }

        addAndStartEngine("localhost");
        addAndStartEngine("localhost4");
        addAndStartEngine("localhost5");

        for (int j = 0; j < 3; j++)
        {
            for (int i = 0; i < 10; i++)
            {
                JqmClientFactory.getClient().enqueue(j11);
            }
            Thread.sleep(200);
        }
        TestHelpers.waitFor(40, 60000, em);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(40, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());
        Assert.assertEquals(40, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        // Ran on all nodes?
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost48'", Long.class).getSingleResult() == 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost4'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost5'", Long.class).getSingleResult() > 0L);
    }

    @Test
    public void testTwoNodesTwoQueues() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip2,
                42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest j11 = new JobRequest("AppliNode1-1", "TestUser");
        JobRequest j21 = new JobRequest("AppliNode2-1", "TestUser");

        for (int i = 0; i < 10; i++)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j21);
        }

        addAndStartEngine("localhost");
        addAndStartEngine("localhost2");

        for (int j = 0; j < 3; j++)
        {
            for (int i = 0; i < 10; i++)
            {
                JqmClientFactory.getClient().enqueue(j11);
                JqmClientFactory.getClient().enqueue(j21);
            }
            Thread.sleep(200);
        }
        TestHelpers.waitFor(80, 60000, em);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(80, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());
        Assert.assertEquals(80, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        // Ran on all nodes?
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost48'", Long.class).getSingleResult() == 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost2'", Long.class).getSingleResult() > 0L);
    }

    @Test
    public void testThreeNodesThreeQueues() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal,
                42, "AppliNode1-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qSlow,
                42, "AppliNode1-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip2,
                42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal2,
                42, "AppliNode2-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qSlow2,
                42, "AppliNode2-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip3,
                42, "AppliNode3-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal3,
                42, "AppliNode3-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qSlow3,
                42, "AppliNode3-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j11 = new JobRequest("AppliNode1-1", "TestUser");
        JobRequest j12 = new JobRequest("AppliNode1-2", "TestUser");
        JobRequest j13 = new JobRequest("AppliNode1-3", "TestUser");

        JobRequest j21 = new JobRequest("AppliNode2-1", "TestUser");
        JobRequest j22 = new JobRequest("AppliNode2-2", "TestUser");
        JobRequest j23 = new JobRequest("AppliNode2-3", "TestUser");

        JobRequest j31 = new JobRequest("AppliNode3-1", "TestUser");
        JobRequest j32 = new JobRequest("AppliNode3-2", "TestUser");
        JobRequest j33 = new JobRequest("AppliNode3-3", "TestUser");

        for (int i = 0; i < 2; i++)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j12);
            JqmClientFactory.getClient().enqueue(j13);
            JqmClientFactory.getClient().enqueue(j21);
            JqmClientFactory.getClient().enqueue(j22);
            JqmClientFactory.getClient().enqueue(j23);
            JqmClientFactory.getClient().enqueue(j31);
            JqmClientFactory.getClient().enqueue(j32);
            JqmClientFactory.getClient().enqueue(j33);
        }

        addAndStartEngine("localhost");
        addAndStartEngine("localhost2");
        addAndStartEngine("localhost3");

        for (int j = 0; j < 3; j++)
        {
            for (int i = 0; i < 2; i++)
            {
                JqmClientFactory.getClient().enqueue(j11);
                JqmClientFactory.getClient().enqueue(j12);
                JqmClientFactory.getClient().enqueue(j13);
                JqmClientFactory.getClient().enqueue(j21);
                JqmClientFactory.getClient().enqueue(j22);
                JqmClientFactory.getClient().enqueue(j23);
                JqmClientFactory.getClient().enqueue(j31);
                JqmClientFactory.getClient().enqueue(j32);
                JqmClientFactory.getClient().enqueue(j33);
            }
            Thread.sleep(200);
        }
        TestHelpers.waitFor(72, 60000, em);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(72, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());
        Assert.assertEquals(72, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        // Ran on all nodes?
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost48'", Long.class).getSingleResult() == 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost2'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(
                em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost3'", Long.class).getSingleResult() > 0L);
    }

    @Test
    public void testStopNicely() throws Exception
    {
        Helpers.setSingleParam("internalPollingPeriodMs", "10", em);

        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal,
                42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j11 = new JobRequest("AppliNode1-1", "TestUser");
        JobRequest j21 = new JobRequest("AppliNode2-1", "TestUser");

        for (int i = 0; i < 10; i++)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j21);
        }
        addAndStartEngine("localhost");
        addAndStartEngine("localhost4");
        TestHelpers.waitFor(20, 60000, em);

        // Stop one node
        em.getTransaction().begin();
        em.createQuery("UPDATE Node n SET n.stop = 'true' WHERE n.name = 'localhost'").executeUpdate();
        em.getTransaction().commit();
        Thread.sleep(1000);

        // Enqueue other requests.. they should run on node 2
        for (int i = 0; i < 10; i++)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j21);
        }
        TestHelpers.waitFor(30, 5000, em);
        Thread.sleep(2000); // to ensure there are no additional runs

        // Only stop node2... node1 should be already dead.
        engines.get("localhost4").stop();
        engines.clear();

        // Check 30 have ended (10 should be blocked in queue)
        Assert.assertEquals(30, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
        Assert.assertEquals(10, TestHelpers.getQueueAllCount(em));
    }

    @Test
    public void testMassMulti()
    {
        long size = 1000;

        Queue q = CreationTools.initQueue("testqueue", "super test queue", 42, em, false);

        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", q, 42, "appliname",
                null, "Franquin", "ModuleMachin", "other", "other", false, em);

        Node n0 = CreationTools.createNode("n0", 0, "./target/outputfiles/", "./../", "./target/tmp", em);
        Node n1 = CreationTools.createNode("n1", 0, "./target/outputfiles/", "./../", "./target/tmp", em);
        Node n2 = CreationTools.createNode("n2", 0, "./target/outputfiles/", "./../", "./target/tmp", em);
        Node n3 = CreationTools.createNode("n3", 0, "./target/outputfiles/", "./../", "./target/tmp", em);
        Node n4 = CreationTools.createNode("n4", 0, "./target/outputfiles/", "./../", "./target/tmp", em);
        Node n5 = CreationTools.createNode("n5", 0, "./target/outputfiles/", "./../", "./target/tmp", em);
        Node n6 = CreationTools.createNode("n6", 0, "./target/outputfiles/", "./../", "./target/tmp", em);
        Node n7 = CreationTools.createNode("n7", 0, "./target/outputfiles/", "./../", "./target/tmp", em);
        Node n8 = CreationTools.createNode("n8", 0, "./target/outputfiles/", "./../", "./target/tmp", em);
        Node n9 = CreationTools.createNode("n9", 0, "./target/outputfiles/", "./../", "./target/tmp", em);

        TestHelpers.setNodesLogLevel("INFO", em);

        CreationTools.createDeploymentParameter(n0, 5, 1, q, em);
        CreationTools.createDeploymentParameter(n1, 5, 1, q, em);
        CreationTools.createDeploymentParameter(n2, 5, 1, q, em);
        CreationTools.createDeploymentParameter(n3, 5, 1, q, em);
        CreationTools.createDeploymentParameter(n4, 5, 1, q, em);
        CreationTools.createDeploymentParameter(n5, 5, 1, q, em);
        CreationTools.createDeploymentParameter(n6, 5, 1, q, em);
        CreationTools.createDeploymentParameter(n7, 5, 1, q, em);
        CreationTools.createDeploymentParameter(n8, 5, 1, q, em);
        CreationTools.createDeploymentParameter(n9, 5, 1, q, em);

        for (int i = 0; i < size; i++)
        {
            JobRequest.create("appliname", "user").submit();
        }

        Calendar c = Calendar.getInstance();
        this.addAndStartEngine("n0");
        this.addAndStartEngine("n1");
        this.addAndStartEngine("n2");
        this.addAndStartEngine("n3");
        this.addAndStartEngine("n4");
        this.addAndStartEngine("n5");
        this.addAndStartEngine("n6");
        this.addAndStartEngine("n7");
        this.addAndStartEngine("n8");
        this.addAndStartEngine("n9");

        TestHelpers.waitFor(size, 120000, em);

        long msgs = em.createQuery("SELECT COUNT(m) from Message m", Long.class).getSingleResult();
        Assert.assertEquals(size, msgs);

        @SuppressWarnings("unchecked")
        List<Object[]> res = em.createQuery("SELECT h.nodeName, COUNT(h) FROM History h GROUP BY h.nodeName").getResultList();
        for (Object[] o : res)
        {
            // Every node must have run at least a few jobs...
            Assert.assertTrue(((Long) o[1]) > 10L);
            jqmlogger.info(o[0] + " - " + o[1]);
        }
        jqmlogger.info("" + (Calendar.getInstance().getTimeInMillis() - c.getTimeInMillis()) / 1000);
    }
}
