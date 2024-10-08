package com.enioka.jqm.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ServiceLoader;

import com.enioka.jqm.client.api.Deliverable;
import com.enioka.jqm.client.api.JqmClientFactory;
import com.enioka.jqm.test.api.JqmAsynchronousTester;

import org.apache.commons.io.IOUtils;
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
        try (var tester = ServiceLoader.load(JqmAsynchronousTester.class).findFirst().get().createSingleNodeOneQueue()
                .addSimpleJobDefinitionFromClasspath(Payload1.class).start())
        {

            tester.enqueue("Payload1");
            tester.waitForResults(1, 10000, 0);

            Assert.assertEquals(1, tester.getOkCount());
            Assert.assertEquals(1, tester.getHistoryAllCount());
        }
    }

    // Test multiple enqueues
    @Test
    public void testTwo()
    {
        JqmAsynchronousTester tester = DefaultJqmAsynchronousTester.create().createSingleNodeOneQueue()
                .addSimpleJobDefinitionFromClasspath(Payload1.class).start();

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
        JqmAsynchronousTester tester = DefaultJqmAsynchronousTester.create().createSingleNodeOneQueue()
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
        JqmAsynchronousTester tester = DefaultJqmAsynchronousTester.create().createSingleNodeOneQueue().setNodesLogLevel("TRACE")
                .addSimpleJobDefinitionFromLibrary("payload1", "pyl.EngineApiSendDeliverable", "../jqm-tests/jqm-test-pyl/target/test.jar")
                .start();

        long jobId = JqmClientFactory.getClient().newJobRequest("payload1", "tesuser").addParameter("fileName", "marsu.txt")
                .addParameter("filepath", "./").enqueue();
        tester.waitForResults(1, 10000, 0);

        Assert.assertEquals(0, tester.getNonOkCount());
        Assert.assertEquals(1, tester.getOkCount());
        Assert.assertEquals(1, tester.getHistoryAllCount());

        List<Deliverable> files = JqmClientFactory.getClient().getJobDeliverables(jobId);
        Assert.assertEquals(1, files.size());

        Deliverable file = files.get(0);
        InputStream is = tester.getDeliverableContent(file);
        String nl = System.getProperty("line.separator");
        Assert.assertEquals("Hello World!" + nl, IOUtils.toString(is, Charset.forName("UTF8")));

        tester.stop();
    }

    // JNDI resource test
    @Test
    public void testFive()
    {
        JqmAsynchronousTester tester = DefaultJqmAsynchronousTester.create().createSingleNodeOneQueue()
                .addSimpleJobDefinitionFromClasspath(Payload3.class).start();
        tester.enqueue("Payload3");

        tester.waitForResults(1, 10000);
        Assert.assertTrue(tester.testCounts(1, 0));

        tester.stop();
    }

}
