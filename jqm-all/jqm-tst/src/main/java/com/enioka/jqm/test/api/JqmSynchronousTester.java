package com.enioka.jqm.test.api;

/**
 * This interface allows to start a stripped-down version of the JQM engine and run a payload synchronously inside it.<br>
 * It is the only supported way to unit test a payload (be it with JUnit or a simple main() method). <br>
 * <br>
 * Limitations:
 * <ul>
 * <li>The job will actually run inside the current class loader (in the full engine, each job instance has its own class loader)</li>
 * <li>This for testing one job only. Only one job instance will run! If the test itself enqueues new launch request, they will be ignored.
 * For testing interactions between job instances, integration tests on an embedded JQM engine are required.</li>
 * <li>If using resources (JNDI), they must be put inside a resource.xml file at the root of classloader search.</li>
 * <li>Resource providers and corresponding drivers must be put inside testing class path (for example by putting them inside pom.xml with a
 * <code>test</code> scope)</li>
 * <li>To ease tests, the launch is synchronous. Obviously, real life instances are asynchronous. To test asynchronous launches, use an
 * embedded engine (integration test) with the much more complicated {@link JqmAsynchronousTester}.</li>
 * <li>If files are created by the payload, they are stored inside a temporary directory that is not removed at the end of the run.</li>
 * </ul>
 * <br>
 * For example, a simple JUnit test could be:
 *
 * <pre>
 * {@code public void testOne()
 * {
 *     JobInstance res = JqmAsynchronousTesterJse.create("com.enioka.jqm.test.Payload1").run();
 *     Assert.assertEquals(State.ENDED, res.getState());
 * }
 * }
 * </pre>
 */
public interface JqmSynchronousTester extends AutoCloseable
{
    /**
     * Add a parameter to the job definition.
     *
     * @param key
     * @param value
     */
    public JqmSynchronousTester addParameter(String key, String value);

    /**
     * Set the job class to run
     *
     * @param clazz
     */
    public JqmSynchronousTester setJobClass(Class<?> clazz);

    /**
     * Set the job class to run
     *
     * @param className
     */
    public JqmSynchronousTester setJobClass(String className);

    /**
     * Synchronously start the job inside the embedded JQM engine.
     */
    public com.enioka.jqm.client.api.JobInstance run();

    // Remove exception from AutoCloseable
    @Override
    void close();
}
