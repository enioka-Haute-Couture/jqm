package com.enioka.jqm.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.Deliverable;
import com.enioka.jqm.api.JqmClientFactory;

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
        JqmAsyncTester tester = JqmAsyncTester.createSingleNodeOneQueue().addSimpleJobDefinitionFromClasspath(Payload1.class).start();

        tester.enqueue("Payload1");
        tester.waitForResults(1, 10000, 0);

        Assert.assertEquals(1, tester.getOkCount());
        Assert.assertEquals(1, tester.getHistoryAllCount());

        tester.stop();
    }

    // Test multiple enqueues
    @Test
    public void testTwo()
    {
        JqmAsyncTester tester = JqmAsyncTester.createSingleNodeOneQueue().addSimpleJobDefinitionFromClasspath(Payload1.class).start();

        tester.enqueue("Payload1");
        tester.enqueue("Payload1");
        tester.enqueue("Payload1");
        tester.enqueue("Payload1");
        tester.waitForResults(4, 10000, 0);

        Assert.assertEquals(4, tester.getOkCount());
        Assert.assertEquals(4, tester.getHistoryAllCount());

        tester.stop();
    }

    // Jar on path, not inside local classpath
    @Test
    public void testThree()
    {
        JqmAsyncTester tester = JqmAsyncTester.createSingleNodeOneQueue()
                .addSimpleJobDefinitionFromLibrary("payload1", "App", "../jqm-tests/jqm-test-datetimemaven/target/test.jar").start();

        tester.enqueue("payload1");
        tester.waitForResults(1, 10000, 0);

        Assert.assertEquals(0, tester.getNonOkCount());
        Assert.assertEquals(1, tester.getOkCount());
        Assert.assertEquals(1, tester.getHistoryAllCount());

        tester.stop();
    }

    // Deliverable creation
    @Test
    public void testFour() throws IOException
    {
        JqmAsyncTester tester = JqmAsyncTester.createSingleNodeOneQueue()
                .addSimpleJobDefinitionFromLibrary("payload1", "pyl.EngineApiSendDeliverable", "../jqm-tests/jqm-test-pyl/target/test.jar")
                .start();

        int jobId = tester.enqueue("payload1");
        tester.waitForResults(1, 10000, 0);

        Assert.assertEquals(0, tester.getNonOkCount());
        Assert.assertEquals(1, tester.getOkCount());
        Assert.assertEquals(1, tester.getHistoryAllCount());

        List<Deliverable> files = JqmClientFactory.getClient().getJobDeliverables(jobId);
        Assert.assertEquals(1, files.size());

        Deliverable file = files.get(0);
        InputStream is = tester.getDeliverableContent(file);
        String nl = System.getProperty("line.separator");
        Assert.assertEquals("Hello World!" + nl, IOUtils.toString(is));

        tester.stop();
    }

    // JNDI resource test
    @Test
    public void testFive()
    {
        JqmAsyncTester tester = JqmAsyncTester.createSingleNodeOneQueue().addSimpleJobDefinitionFromClasspath(Payload3.class).start();
        tester.enqueue("Payload3");

        tester.waitForResults(1, 10000);
        Assert.assertTrue(tester.testCounts(1, 0));

        tester.stop();
    }

}
