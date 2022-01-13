package com.enioka.jqm.tester.api;

/**
 * Helper class to define a new payload. This will help create an actual job definition, as if it had been imported from an XML deployment
 * descriptor.<br>
 * There are two possibilities to create a job definition: from a class inside the current class path, or from an external Jar.
 */
public interface TestJobDefinition
{
    ///////////////////////////////////////////////////////////////////////////
    // FLUID API
    ///////////////////////////////////////////////////////////////////////////

    /**
     * End of the job definition creation fluent API: save the job definition inside the tester.
     *
     * @return the builder for the tester.
     */
    public JqmAsynchronousTester addJobDefinition();

    /**
     * Name of the job definition, as used in the enqueue API. Always has a default.
     *
     * @return the builder for the tester.
     */
    public TestJobDefinition setName(String applicationName);

    /**
     * The jobs will run on this queue. If no set (or set to null) will run on the default queue.
     *
     * @param queueName
     */
    public TestJobDefinition setQueueName(String queueName);

    /**
     * This enables Highlander mode. See the documentation on Highlander jobs.
     */
    public TestJobDefinition enableHighlander();

    /**
     * Add a parameter.
     *
     * @param key
     *            must be unique
     * @param value
     */
    public TestJobDefinition addParameter(String key, String value);

    /**
     * An optional classifier.
     *
     * @param application
     */
    public TestJobDefinition setApplication(String application);

    /**
     * An optional classifier.
     *
     * @param module
     */
    public TestJobDefinition setModule(String module);

    /**
     * An optional classifier.
     *
     * @param keyword1
     */
    public TestJobDefinition setKeyword1(String keyword1);

    /**
     * An optional classifier.
     *
     * @param keyword2
     */
    public TestJobDefinition setKeyword2(String keyword2);

    /**
     * An optional classifier.
     *
     * @param keyword3
     */
    public TestJobDefinition setKeyword3(String keyword3);

    /**
     * By default jobs run inside a dedicated class loader thrown out after the run. By setting this to a non null value, this job will run
     * inside a re-used class loader. All jobs using the same specificIsolationContext share the same class loader. See documentation on
     * class loading inside JQM.
     *
     * @param specificIsolationContext
     *            name of the class loader to use or null to use default behaviour.
     */
    public TestJobDefinition setSpecificIsolationContext(String specificIsolationContext);

    /**
     * This enables child-first class loading.<br>
     * Note that if the payload is actually inside your unit test class path, this won't do much. See documentation on class loading inside
     * JQM.
     *
     * @param childFirstClassLoader
     */
    public TestJobDefinition setChildFirstClassLoader(boolean childFirstClassLoader);

    /**
     * This prevents the given class from being loaded by a parent class loader. Id es: if the class is not directly available to the
     * payload (inside the payload libraries for example) it will not be loaded.
     *
     * @param canonicalClassName
     */
    public TestJobDefinition addHiddenJavaClasses(String canonicalClassName);

    /**
     * Enable verbose class loading fot his payload.
     */
    public TestJobDefinition enableClassLoaderTracing();
}
