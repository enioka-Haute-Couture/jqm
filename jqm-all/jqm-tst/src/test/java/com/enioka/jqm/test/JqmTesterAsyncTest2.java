package com.enioka.jqm.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * Testing the tester... The main demo test using before and after is in the other file.
 *
 */
public class JqmTesterAsyncTest2
{
    // Simple no-nonsense test
    @Test
    public void testOne()
    {
        JqmAsyncTester tester = JqmAsyncTester.createSingleNodeOneQueue()
                .addJobDefinition(TestJobDefinition.createFromClassPath("payload1", "description", Payload1.class)).start();

        tester.enqueue("payload1");
        tester.waitForResults(1, 10000, 0);

        Assert.assertEquals(1, tester.getOkCount());
        Assert.assertEquals(1, tester.getHistoryAllCount());

        tester.stop();
    }

    // Test multiple enqueues
    @Test
    public void testTwo()
    {
        JqmAsyncTester tester = JqmAsyncTester.createSingleNodeOneQueue()
                .addJobDefinition(TestJobDefinition.createFromClassPath("payload1", "description", Payload1.class)).start();

        tester.enqueue("payload1");
        tester.enqueue("payload1");
        tester.enqueue("payload1");
        tester.enqueue("payload1");
        tester.waitForResults(4, 10000, 0);

        Assert.assertEquals(4, tester.getOkCount());
        Assert.assertEquals(4, tester.getHistoryAllCount());

        tester.stop();
    }

    // Jar on path, not inside local classpath
    @Test
    public void testThree()
    {
        System.out.println(new File(".").getAbsolutePath());
        JqmAsyncTester tester = JqmAsyncTester.createSingleNodeOneQueue()
                .addJobDefinition(
                        TestJobDefinition.createFromJar("payload1", "payload1", "App", "../jqm-tests/jqm-test-datetimemaven/target/test.jar"))
                .start();

        tester.enqueue("payload1");
        tester.waitForResults(1, 10000, 0);

        Assert.assertEquals(0, tester.getNonOkCount());
        Assert.assertEquals(1, tester.getOkCount());
        Assert.assertEquals(1, tester.getHistoryAllCount());

        tester.stop();
    }

}
