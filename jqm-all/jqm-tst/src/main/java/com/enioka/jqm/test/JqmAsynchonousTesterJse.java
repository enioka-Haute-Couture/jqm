package com.enioka.jqm.test;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.enioka.jqm.client.api.Deliverable;
import com.enioka.jqm.tester.api.JqmAsynchronousTester;
import com.enioka.jqm.tester.api.TestJobDefinition;

/**
 * An implementation of {@link JqmAsynchronousTester} that does the bridge between the OSGi and standard worlds.
 */
public class JqmAsynchonousTesterJse implements JqmAsynchronousTester
{
    private JqmTesterOsgiInternal osgiServer;
    private JqmAsynchronousTester tester;

    ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTION
    ///////////////////////////////////////////////////////////////////////////

    public JqmAsynchonousTesterJse()
    {
        System.setProperty("com.enioka.jqm.cl.allow_system_cl", "true"); // Resources should come from test CP.

        osgiServer = new JqmTesterOsgiInternal();
        osgiServer.start();

        tester = osgiServer.getSystemApi(JqmAsynchronousTester.class);
        tester.resetAllData();
    }

    /**
     * Equivalent to simply calling the constructor. Present for consistency.
     */
    public static JqmAsynchronousTester create()
    {
        return new JqmAsynchonousTesterJse();
    }

    /**
     * A helper method which creates a preset environment with a single node called 'node1' and a single queue named 'queue1' being polled
     * every 100ms by the node with at most 10 parallel running job instances..
     */
    public static JqmAsynchronousTester createSingleNodeOneQueue()
    {
        return new JqmAsynchonousTesterJse().addNode("node1").addQueue("queue1").deployQueueToNode("queue1", 10, 100, "node1");
    }

    ///////////////////////////////////////////////////////////////////////////
    // TEST PREPARATION
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public JqmAsynchronousTester addNode(String nodeName)
    {
        return tester.addNode(nodeName);
    }

    @Override
    public JqmAsynchronousTester setNodesLogLevel(String level)
    {
        return tester.setNodesLogLevel(level);
    }

    @Override
    public JqmAsynchronousTester addQueue(String name)
    {
        return tester.addQueue(name);
    }

    @Override
    public JqmAsynchronousTester deployQueueToNode(String queueName, int maxJobsRunning, int pollingIntervallMs, String... nodeName)
    {
        return tester.deployQueueToNode(queueName, maxJobsRunning, pollingIntervallMs, nodeName);
    }

    @Override
    public JqmAsynchronousTester addGlobalParameter(String key, String value)
    {
        return tester.addGlobalParameter(key, value);
    }

    @Override
    public JqmAsynchronousTester addSimpleJobDefinitionFromClasspath(Class<? extends Object> classToRun)
    {
        return tester.addSimpleJobDefinitionFromClasspath(classToRun);
    }

    @Override
    public TestJobDefinition createJobDefinitionFromClassPath(Class<? extends Object> classToRun)
    {
        return tester.createJobDefinitionFromClassPath(classToRun);
    }

    @Override
    public JqmAsynchronousTester addSimpleJobDefinitionFromLibrary(String name, String className, String jarPath)
    {
        return tester.addSimpleJobDefinitionFromLibrary(name, className, jarPath);
    }

    @Override
    public TestJobDefinition createJobDefinitionFromLibrary(String name, String className, String jarPath)
    {
        return tester.createJobDefinitionFromLibrary(name, className, jarPath);
    }

    ///////////////////////////////////////////////////////////////////////////
    // DURING TEST
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public JqmAsynchronousTester start()
    {
        return tester.start();
    }

    @Override
    public Long enqueue(String name)
    {
        return tester.enqueue(name);
    }

    @Override
    public void waitForResults(int nbResult, int timeoutMs, int waitAdditionalMs)
    {
        tester.waitForResults(nbResult, timeoutMs, waitAdditionalMs);
    }

    @Override
    public void waitForResults(int nbResult, int timeoutMs)
    {
        waitForResults(nbResult, timeoutMs, 0);
    }

    @Override
    public void stop()
    {
        tester.stop();
    }

    ///////////////////////////////////////////////////////////////////////////
    // CLEANUP
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void cleanupOperationalDbData()
    {
        tester.cleanupOperationalDbData();
    }

    @Override
    public void cleanupAllJobDefinitions()
    {
        tester.cleanupAllJobDefinitions();
    }

    @Override
    public void resetAllData()
    {
        tester.resetAllData();
    }

    ///////////////////////////////////////////////////////////////////////////
    // TEST RESULT ANALYSIS HELPERS
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int getHistoryAllCount()
    {
        return tester.getHistoryAllCount();
    }

    @Override
    public int getQueueAllCount()
    {
        return tester.getQueueAllCount();
    }

    @Override
    public int getOkCount()
    {
        return tester.getOkCount();
    }

    @Override
    public int getNonOkCount()
    {
        return tester.getNonOkCount();
    }

    @Override
    public boolean testOkCount(long expectedOkCount)
    {
        return tester.testOkCount(expectedOkCount);
    }

    @Override
    public boolean testKoCount(long expectedKoCount)
    {
        return tester.testKoCount(expectedKoCount);
    }

    @Override
    public boolean testCounts(long expectedOkCount, long expectedKoCount)
    {
        return testCounts(expectedOkCount, expectedKoCount);
    }

    @Override
    public InputStream getDeliverableContent(Deliverable file) throws FileNotFoundException
    {
        return tester.getDeliverableContent(file);
    }
}
