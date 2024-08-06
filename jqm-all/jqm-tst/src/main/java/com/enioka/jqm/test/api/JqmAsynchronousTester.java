package com.enioka.jqm.test.api;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.enioka.jqm.client.api.Deliverable;
import com.enioka.jqm.client.api.JqmClient;

/**
 * An asynchronous tester for JQM payloads. It allows to configure and start one or more embedded JQM engines and run payloads against them.
 * It is most suited for integration tests.<br>
 * <br>
 * It starts full JQM nodes running on an in-memory embedded database. They are started with all web API disabled.<br>
 * The user should handle interactions with the nodes through the normal client APIs. See {@link JqmClient} and {@link JqmClientFactory}. As
 * the web services are not loaded, the file retrieval methods of these APIs will not work, so the tester provides a
 * {@link #getDeliverableContent(Deliverable)} method to compensate. The tester also provides a few helper methods (accelerators) that
 * encapsulate the client API.<br>
 *
 * If using resources (JNDI), they must be put inside a resource.xml file at the root of class loader search.<br>
 * Note that tester instances are not thread safe.
 */
public interface JqmAsynchronousTester extends AutoCloseable
{
    ///////////////////////////////////////////////////////////////////////////
    // BEFORE TEST - JQM NETWORK
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a new node. It is not started by this method.<br>
     * This must be called before starting the tester.
     *
     * @param nodeName
     *            the name of the node. Must be unique.
     */
    public JqmAsynchronousTester addNode(String nodeName);

    /**
     * Changes the log level of existing and future nodes.
     *
     * @param level
     *            TRACE, DEBUG, INFO, WARNING, ERROR (or anything, which is interpreted as INFO)
     */
    public JqmAsynchronousTester setNodesLogLevel(String level);

    /**
     * Set or update a global parameter.
     */
    public JqmAsynchronousTester addGlobalParameter(String key, String value);

    ///////////////////////////////////////////////////////////////////////////
    // BEFORE TEST - QUEUES
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a new queue. After creation, it is not polled by any node - see {@link #deployQueueToNode(String, int, int, String...)} for
     * this.<br>
     * The first queue created is considered to be the default queue.<br>
     * This must be called before starting the engines.
     *
     * @param name
     *            must be unique.
     */
    public JqmAsynchronousTester addQueue(String name);

    /**
     * Set one or more nodes to poll a queue for new job instances.<br>
     * This must be called before starting the engines.
     */
    public JqmAsynchronousTester deployQueueToNode(String queueName, int maxJobsRunning, int pollingIntervallMs, String... nodeName);

    ///////////////////////////////////////////////////////////////////////////
    // BEFORE TEST - CREATE JOB DEFINITIONS
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A helper method to create a job definition from a class <strong>which is present inside the current class path</strong>.<br>
     * The job description and name will be the class name (simple name, not the fully qualified name).<br>
     * If you need further customisation, directly create your {@link TestJobDefinition} by calling
     * {@link #createJobDefinitionFromClassPath(String, String, Class))} instead of using this method.
     *
     * @param classToRun
     *            a class present inside the class path which should be launched by JQM.
     * @return the tester itself to allow fluid API behaviour.
     */
    public JqmAsynchronousTester addSimpleJobDefinitionFromClasspath(Class<? extends Object> classToRun);

    /**
     * Create a new Job Definition from a class <strong>which is present inside the current class path</strong>.<br>
     * The job description and name will be the class name (simple name, not the fully qualified name).<br>
     * The job definition is only be ready after {@link TestJobDefinition#addJobDefinition()} is called (fluent API).
     *
     * @param name
     * @param className
     * @param classToRun
     * @return
     */
    public TestJobDefinition createJobDefinitionFromClassPath(Class<? extends Object> classToRun);

    /**
     * A helper method to create a job definition from a class <strong>which is present inside an existing jar file</strong>.<br>
     * The job description and name will be identical<br>
     * If you need further customisation, directly create your {@link TestJobDefinition} and call
     * {@link #addJobDefinition(TestJobDefinition)} instead of using this method.
     *
     * @param name
     *            name of the new job definition (as used in the enqueue methods)
     * @param className
     *            the full canonical name of the the class to run inside the jar
     * @param jarPath
     *            path to the jar. Relative to current directory.
     * @return the tester itself to allow fluid API behaviour.
     */
    public JqmAsynchronousTester addSimpleJobDefinitionFromLibrary(String name, String className, String jarPath);

    /**
     * Create a new Job Definition from a class <strong>which is present inside an existing jar file</strong>.<br>
     * The job description and name will be identical<br>
     * The job definition is only be ready after {@link TestJobDefinition#addJobDefinition()} is called (fluent API).
     *
     * @param name
     * @param className
     * @param jarPath
     * @return
     */
    public TestJobDefinition createJobDefinitionFromLibrary(String name, String className, String jarPath);

    ///////////////////////////////////////////////////////////////////////////
    // BEFORE TEST - HELPERS
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A helper method which creates a preset environment with a single node called 'node1' and a single queue named 'queue1' being polled
     * every 100ms by the node with at most 10 parallel running job instances..
     */
    public JqmAsynchronousTester createSingleNodeOneQueue();

    ///////////////////////////////////////////////////////////////////////////
    // DURING TEST
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This actually starts the different engines configured with {@link #addNode(String)}.<br>
     * This can usually only be called once (it can actually be called again but only after calling {@link #stop()}).
     */
    public JqmAsynchronousTester start();

    /**
     * Helper method to enqueue a new launch request. Simple JqmClientFactory.getClient().enqueue wrapper.
     *
     * @return the request ID.
     */
    public Long enqueue(String name);

    /**
     * Wait for a given amount of ended job instances (OK or KO).
     *
     * @param nbResult
     *            the expected result count
     * @param timeoutMs
     *            give up after this (throws a RuntimeException)
     * @param waitAdditionalMs
     *            after reaching the expected nbResult count, wait a little more (for example to ensure there is no additonal unwanted
     *            launch). Will usually be zero.
     */
    public void waitForResults(int nbResult, int timeoutMs, int waitAdditionalMs);

    /**
     * Wait for a given amount of ended job instances (OK or KO). Shortcut for {@link #waitForResults(int, int, int)} with 0ms of additional
     * wait time.
     *
     * @param nbResult
     *            the expected result count
     * @param timeoutMs
     *            give up after this (throws a RuntimeException)
     */
    public void waitForResults(int nbResult, int timeoutMs);

    /**
     * Stops all engines. Only returns when engines are fully stopped.
     */
    public void stop();

    ///////////////////////////////////////////////////////////////////////////
    // POST TEST
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Removes all job instances from the queues and the history.
     *
     * @param em
     */
    public void cleanupOperationalDbData();

    /**
     * Deletes all job definitions. This calls {@link #cleanupOperationalDbData()}
     *
     * @param em
     */
    public void cleanupAllJobDefinitions();

    /**
     * Prepares the tester for a new test series by removing all database data and reseting default configuration.
     */
    public void resetAllData();

    /**
     * Helper query (directly uses {@link JdbcQuery}). Gives the count of all ended (KO and OK) job instances.
     */
    public int getHistoryAllCount();

    /**
     * Helper query (directly uses {@link JdbcQuery}). Gives the count of all non-ended (waiting in queue, running...) job instances.
     */
    public int getQueueAllCount();

    /**
     * Helper query (directly uses {@link JdbcQuery}). Gives the count of all OK-ended job instances.
     */
    public int getOkCount();

    /**
     * Helper query (directly uses {@link JdbcQuery}). Gives the count of all non-OK-ended job instances.
     */
    public int getNonOkCount();

    /**
     * Helper method. Tests if {@link #getOkCount()} is equal to the given parameter.
     */
    public boolean testOkCount(long expectedOkCount);

    /**
     * Helper method. Tests if {@link #getNonOkCount()} is equal to the given parameter.
     */
    public boolean testKoCount(long expectedKoCount);

    /**
     * Helper method. Tests if {@link #getOkCount()} is equal to the first parameter and if {@link #getNonOkCount()} is equal to the second
     * parameter.
     */
    public boolean testCounts(long expectedOkCount, long expectedKoCount);

    /**
     * Version of {@link JqmClient#getDeliverableContent(Deliverable)} which does not require the web service APIs to be enabled to work.
     * Also, returned files do not self-destruct on stream close.<br>
     * See the javadoc of the original method for details.
     *
     * @throws FileNotFoundException
     */
    public InputStream getDeliverableContent(Deliverable file) throws FileNotFoundException;

    // Remove exception from AutoCloseable
    @Override
    void close();
}
