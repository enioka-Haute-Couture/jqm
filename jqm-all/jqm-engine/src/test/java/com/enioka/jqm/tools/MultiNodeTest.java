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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.Message;
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
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip, 42,
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

        TestHelpers.waitFor(40, 30000, em);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(40, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());
        Assert.assertEquals(40, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        // Ran on both nodes?
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost48'", Long.class).getSingleResult() == 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost4'", Long.class).getSingleResult() > 0L);
    }

    @Test
    public void testOneQueueThreeNodes() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip, 42,
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
        TestHelpers.waitFor(40, 10000, em);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(40, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());
        Assert.assertEquals(40, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        // Ran on all nodes?
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost48'", Long.class).getSingleResult() == 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost4'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost5'", Long.class).getSingleResult() > 0L);
    }

    @Test
    public void testTwoNodesTwoQueues() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip2, 42,
                "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
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
        TestHelpers.waitFor(80, 10000, em);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(80, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());
        Assert.assertEquals(80, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        // Ran on all nodes?
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost48'", Long.class).getSingleResult() == 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost2'", Long.class).getSingleResult() > 0L);
    }

    @Test
    public void testThreeNodesThreeQueues() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qNormal, 42,
                "AppliNode1-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qSlow, 42,
                "AppliNode1-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip2, 42,
                "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qNormal2, 42,
                "AppliNode2-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qSlow2, 42,
                "AppliNode2-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip3, 42,
                "AppliNode3-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qNormal3, 42,
                "AppliNode3-2", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qSlow3, 42,
                "AppliNode3-3", null, "Franquin", "ModuleMachin", "other", "other", false, em);

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
        TestHelpers.waitFor(72, 10000, em);
        Thread.sleep(2000); // to ensure there are no additional runs

        Assert.assertEquals(72, em.createQuery("SELECT j FROM Message j", Message.class).getResultList().size());
        Assert.assertEquals(72, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        // Ran on all nodes?
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost48'", Long.class).getSingleResult() == 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost2'", Long.class).getSingleResult() > 0L);
        Assert.assertTrue(em.createQuery("SELECT count(j) FROM History j WHERE j.node.name='localhost3'", Long.class).getSingleResult() > 0L);
    }

    @Test
    public void testStopNicely() throws Exception
    {
        Helpers.setSingleParam("internalPollingPeriodMs", "10", em);

        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qVip, 42,
                "AppliNode1-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimesendmsg/target/test.jar", TestHelpers.qNormal, 42,
                "AppliNode2-1", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j11 = new JobRequest("AppliNode1-1", "TestUser");
        JobRequest j21 = new JobRequest("AppliNode2-1", "TestUser");

        for (int i = 0; i < 10; i++)
        {
            JqmClientFactory.getClient().enqueue(j11);
            JqmClientFactory.getClient().enqueue(j21);
        }
        addAndStartEngine("localhost");
        addAndStartEngine("localhost4");
        TestHelpers.waitFor(20, 5000, em);

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
}
