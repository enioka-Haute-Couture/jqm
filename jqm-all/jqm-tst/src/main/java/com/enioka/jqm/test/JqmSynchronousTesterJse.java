package com.enioka.jqm.test;

import com.enioka.jqm.tester.api.JqmSynchronousTester;

/**
 * This class allows to start a stripped-down version of the JQM engine and run a payload synchronously inside it.<br>
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
 * embedded engine (integration test) with the much more complicated {@link JqmAsynchonousTesterJse}.</li>
 * <li>If files are created by the payload, they are stored inside a temporary directory that is not removed at the end of the run.</li>
 * </ul>
 * <br>
 * For example, a simple JUnit test could be:
 *
 * <pre>
 * {@code public void testOne()
 * {
 *     JobInstance res = JqmSynchronousTesterJse.create("com.enioka.jqm.test.Payload1").run();
 *     Assert.assertEquals(State.ENDED, res.getState());
 * }
 * </pre>
 *
 * <br>
 * <br>
 * This implementation of the {@link JqmSynchronousTester} interface is usable in all normal Java contextes (not OSGi ones).
 */
public class JqmSynchronousTesterJse implements JqmSynchronousTester
{
    private JqmTesterOsgiInternal osgiServer;
    private JqmSynchronousTester tester;

    private JqmSynchronousTesterJse()
    {
        System.setProperty("com.enioka.jqm.cl.allow_system_cl", "true"); // Resources should come from test CP.

        osgiServer = new JqmTesterOsgiInternal();
        osgiServer.start();

        tester = osgiServer.getSystemApi(JqmSynchronousTester.class);
    }

    /**
     * Start of the fluent API to construct a test case.
     *
     * @param className
     * @return
     */
    public static JqmSynchronousTester create(String className)
    {
        return new JqmSynchronousTesterJse().setJobClass(className);
    }

    /**
     * Start of the fluent API to construct a test case.
     *
     * @param clazz
     * @return
     */
    public static JqmSynchronousTester create(Class<?> clazz)
    {
        return new JqmSynchronousTesterJse().setJobClass(clazz);
    }

    @Override
    public com.enioka.jqm.client.api.JobInstance run()
    {
        com.enioka.jqm.client.api.JobInstance res = tester.run();
        return res;
    }

    @Override
    public JqmSynchronousTester addParameter(String key, String value)
    {
        tester.addParameter(key, value);
        return this;
    }

    @Override
    public JqmSynchronousTester setJobClass(Class<?> clazz)
    {
        tester.setJobClass(clazz);
        return this;
    }

    @Override
    public JqmSynchronousTester setJobClass(String className)
    {
        tester.setJobClass(className);
        return this;
    }
}
