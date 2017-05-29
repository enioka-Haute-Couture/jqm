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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class MultiNodeTest extends JqmBaseTest
{
    @Before
    public void b()
    {
        TestHelpers.setNodesLogLevel("INFO", cnx);
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
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
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

        TestHelpers.waitFor(40, 60000, cnx);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(40, (int) cnx.runSelectSingle("message_select_count_all", Integer.class));
        Assert.assertEquals(40, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));

        // Ran on both nodes?
        Assert.assertTrue(Query.create().setNodeName("localhost48").run().size() == 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost").run().size() > 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost4").run().size() > 0L);
    }

    @Test
    public void testOneQueueThreeNodes() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
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
        TestHelpers.waitFor(40, 60000, cnx);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(40, (int) cnx.runSelectSingle("message_select_count_all", Integer.class));
        Assert.assertEquals(40, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));

        // Ran on all nodes?
        Assert.assertTrue(Query.create().setNodeName("localhost48").run().size() == 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost").run().size() > 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost4").run().size() > 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost5").run().size() > 0L);
    }

    @Test
    public void testTwoNodesTwoQueues() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip2,
                42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
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
        TestHelpers.waitFor(80, 60000, cnx);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(80, (int) cnx.runSelectSingle("message_select_count_all", Integer.class));
        Assert.assertEquals(80, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));

        // Ran on all nodes?
        Assert.assertTrue(Query.create().setNodeName("localhost48").run().size() == 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost").run().size() > 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost2").run().size() > 0L);
    }

    @Test
    public void testThreeNodesThreeQueues() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal,
                42, "AppliNode1-2", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qSlow,
                42, "AppliNode1-3", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip2,
                42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal2,
                42, "AppliNode2-2", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qSlow2,
                42, "AppliNode2-3", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip3,
                42, "AppliNode3-1", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal3,
                42, "AppliNode3-2", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qSlow3,
                42, "AppliNode3-3", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

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
        TestHelpers.waitFor(72, 60000, cnx);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(72, (int) cnx.runSelectSingle("message_select_count_all", Integer.class));
        Assert.assertEquals(72, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));

        // Ran on all nodes?
        Assert.assertTrue(Query.create().setNodeName("localhost48").run().size() == 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost").run().size() > 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost2").run().size() > 0L);
        Assert.assertTrue(Query.create().setNodeName("localhost3").run().size() > 0L);
    }

    @Test
    public void testStopNicely() throws Exception
    {
        Helpers.setSingleParam("internalPollingPeriodMs", "10", cnx);

        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qNormal,
                42, "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        JobRequest j11 = new JobRequest("AppliNode1-1", "TestUser");
        JobRequest j21 = new JobRequest("AppliNode2-1", "TestUser");

        for (int i = 0; i < 10; i++)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j21);
        }
        addAndStartEngine("localhost");
        addAndStartEngine("localhost4");
        TestHelpers.waitFor(20, 60000, cnx);

        // Stop one node
        cnx.runUpdate("node_update_stop_by_id", TestHelpers.node.getId());
        cnx.commit();
        Thread.sleep(1000);

        // Enqueue other requests.. they should run on node 2
        for (int i = 0; i < 10; i++)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j21);
        }
        TestHelpers.waitFor(30, 5000, cnx);
        Thread.sleep(2000); // to ensure there are no additional runs

        // Only stop node2... node1 should be already dead.
        engines.get("localhost4").stop();
        engines.clear();

        // Check 30 have ended (10 should be blocked in queue)
        Assert.assertEquals(30, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertEquals(10, TestHelpers.getQueueAllCount(cnx));
    }

    @Test
    public void testMassMulti()
    {
        long size = 1000;

        int qId = Queue.create(cnx, "testqueue", "super test queue", false);

        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "jqm-tests/jqm-test-pyl/target/test.jar", qId, 42, "appliname",
                null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        Node n0 = Node.create(cnx, "n0", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");
        Node n1 = Node.create(cnx, "n1", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");
        Node n2 = Node.create(cnx, "n2", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");
        Node n3 = Node.create(cnx, "n3", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");
        Node n4 = Node.create(cnx, "n4", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");
        Node n5 = Node.create(cnx, "n5", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");
        Node n6 = Node.create(cnx, "n6", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");
        Node n7 = Node.create(cnx, "n7", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");
        Node n8 = Node.create(cnx, "n8", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");
        Node n9 = Node.create(cnx, "n9", 0, "./target/outputfiles/", "./../", "./target/tmp", "localhost", "INFO");

        TestHelpers.setNodesLogLevel("INFO", cnx);

        DeploymentParameter.create(cnx, n0.getId(), 5, 1, qId);
        DeploymentParameter.create(cnx, n1.getId(), 5, 1, qId);
        DeploymentParameter.create(cnx, n2.getId(), 5, 1, qId);
        DeploymentParameter.create(cnx, n3.getId(), 5, 1, qId);
        DeploymentParameter.create(cnx, n4.getId(), 5, 1, qId);
        DeploymentParameter.create(cnx, n5.getId(), 5, 1, qId);
        DeploymentParameter.create(cnx, n6.getId(), 5, 1, qId);
        DeploymentParameter.create(cnx, n7.getId(), 5, 1, qId);
        DeploymentParameter.create(cnx, n8.getId(), 5, 1, qId);
        DeploymentParameter.create(cnx, n9.getId(), 5, 1, qId);

        cnx.commit();

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

        for (int i = 0; i < size; i++)
        {
            JobRequest.create("appliname", "user").submit();
        }

        TestHelpers.waitFor(size * 2, 120000, cnx);

        long msgs = cnx.runSelectSingle("message_select_count_all", Long.class);
        Assert.assertEquals(size * 2, msgs);

        // Every node must have run at least a few jobs...
        Map<String, Boolean> hasRunSomething = new HashMap<String, Boolean>(10);
        for (int i = 0; i <= 9; i++)
        {
            hasRunSomething.put("n" + i, false);
        }
        for (JobInstance ji : Query.create().setPageSize(1000).run())
        {
            hasRunSomething.put(ji.getNodeName(), true);
        }
        for (Map.Entry<String, Boolean> e : hasRunSomething.entrySet())
        {
            Assert.assertTrue(e.getValue());
        }

        jqmlogger.info("" + (Calendar.getInstance().getTimeInMillis() - c.getTimeInMillis()) / 1000);
    }
}
