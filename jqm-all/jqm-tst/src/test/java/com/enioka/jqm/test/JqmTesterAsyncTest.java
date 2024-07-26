package com.enioka.jqm.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.test.api.JqmAsynchronousTester;

/**
 * Tests as they should be: with a node stared once, closed at the end.
 */
public class JqmTesterAsyncTest
{
    public static JqmAsynchronousTester tester;

    @BeforeClass
    public static void beforeClass()
    {
        tester = JqmAsynchronousTesterImpl.create().addNode("node1").addNode("node2").addQueue("queue1").addQueue("queue2")
                .addQueue("queue3").deployQueueToNode("queue1", 10, 100, "node1").deployQueueToNode("queue2", 10, 100, "node2")
                .deployQueueToNode("queue3", 10, 100, "node1", "node2").start();
    }

    @AfterClass
    public static void afterClass()
    {
        tester.close();
    }

    @Test
    public void testOne()
    {
        tester.cleanupAllJobDefinitions();
        tester.addSimpleJobDefinitionFromClasspath(Payload1.class);

        tester.enqueue("Payload1");
        tester.waitForResults(1, 10000, 0);

        Assert.assertEquals(1, tester.getOkCount());
        Assert.assertEquals(1, tester.getHistoryAllCount());
    }

    @Test
    public void testTwo()
    {
        tester.cleanupAllJobDefinitions();
        tester.createJobDefinitionFromClassPath(Payload1.class).setName("payload2").addJobDefinition();

        tester.enqueue("payload2");
        tester.enqueue("payload2");
        tester.enqueue("payload2");
        tester.enqueue("payload2");
        tester.waitForResults(4, 10000, 0);

        Assert.assertEquals(4, tester.getOkCount());
        Assert.assertEquals(4, tester.getHistoryAllCount());
    }
}
