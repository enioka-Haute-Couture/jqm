package com.enioka.jqm.tools;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class EngineCLIsolationTest extends JqmBaseTest
{

    /**
     * Create JobDef corresponding to TestCLIsolation.TestSet and submit it to queue
     */
    void createSubmitSetJob(String specificIsolationContext)
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.TestCLIsolation.TestSet", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-cl-isolation/target/test.jar", TestHelpers.qVip, -1, "TestSet", null, null, null, null, null, false, em,
                specificIsolationContext);
        JobRequest.create("TestSet", null).submit();
    }

    /**
     * Create JobDef corresponding to TestCLIsolation.TestGet and submit it to queue
     */
    void createSubmitGetJob(String specificIsolationContext)
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.TestCLIsolation.TestGet", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-cl-isolation/target/test.jar", TestHelpers.qVip, -1, "TestGet", null, null, null, null, null, false, em,
                specificIsolationContext);
        JobRequest.create("TestGet", null).submit();
    }

    /**
     * Run test without any change in the default configuration (i.e. jobs are isolated).
     * 
     * Expected : isolation
     */
    @Test
    public void testDefault() throws Exception
    {
        addAndStartEngine();

        createSubmitSetJob(null);
        TestHelpers.waitFor(1, 10000, em);
        createSubmitGetJob(null);
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(2, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    /**
     * Run test setting global parameter launch_isolation_default to Isolated.
     * 
     * Expected : Isolation
     */
    @Test
    public void testGlobalIsolated() throws Exception
    {
        CreationTools.createGlobalParameter("launch_isolation_default", "Isolated", em);
        addAndStartEngine();

        createSubmitSetJob(null);
        TestHelpers.waitFor(1, 10000, em);
        createSubmitGetJob(null);
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(2, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    /**
     * Run test setting global parameter launch_isolation_default to SharedJar with two jobs inside the same jar.
     * 
     * Expected : shared CL
     */
    @Test
    public void testGlobalSharedJarSame() throws Exception
    {
        CreationTools.createGlobalParameter("launch_isolation_default", "SharedJar", em);
        addAndStartEngine();

        createSubmitSetJob(null);
        TestHelpers.waitFor(1, 10000, em);
        createSubmitGetJob(null);
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(em));
    }

    /**
     * Run test setting global parameter launch_isolation_default to SharedJar with two jobs in different jars.
     * 
     * Expected : isolation
     */
    @Test
    public void testGlobalSharedJarDifferent() throws Exception
    {
        CreationTools.createGlobalParameter("launch_isolation_default", "SharedJar", em);
        addAndStartEngine();

        createSubmitSetJob(null);
        TestHelpers.waitFor(1, 10000, em);
        // Use get job from test-pyl jar
        CreationTools.createJobDef(null, true, "pyl.EngineCLIsolationGet", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, -1, "EngineCLIsolationGet", null, null, null, null, null, false,
                em);
        JobRequest.create("EngineCLIsolationGet", null).submit();
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(2, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    /**
     * Run test setting global parameter launch_isolation_default to Shared with two jobs in the same jar.
     * 
     * Expected : shared CL
     */
    @Test
    public void testGlobalSharedSame() throws Exception
    {
        CreationTools.createGlobalParameter("launch_isolation_default", "Shared", em);
        addAndStartEngine();

        createSubmitSetJob(null);
        TestHelpers.waitFor(1, 10000, em);
        createSubmitGetJob(null);
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(em));
    }

    /**
     * Run test setting global parameter launch_isolation_default to Shared with two jobs in the different jars.
     * 
     * Expected : shared CL
     */
    @Test
    public void testGlobalSharedDifferent() throws Exception
    {
        CreationTools.createGlobalParameter("launch_isolation_default", "Shared", em);
        addAndStartEngine();

        createSubmitSetJob(null);
        TestHelpers.waitFor(1, 10000, em);
        // Use get job from test-pyl jar
        CreationTools.createJobDef(null, true, "pyl.EngineCLIsolationGet", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, -1, "EngineCLIsolationGet", null, null, null, null, null, false,
                em);
        JobRequest.create("EngineCLIsolationGet", null).submit();
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(em));
    }

    /**
     * Run test using JobDef parameter specific_isolation_context with two jobs with same specific_isolation_context values using default
     * engine parameters.
     * 
     * Expected : shared CL
     */
    @Test
    public void testJobDefSpecificSame() throws Exception
    {
        addAndStartEngine();

        createSubmitSetJob("test");
        TestHelpers.waitFor(1, 10000, em);
        createSubmitGetJob("test");
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(em));
    }

    /**
     * Run test using JobDef parameter specific_isolation_context with two jobs using different specific_isolation_context values using
     * default engine parameters.
     * 
     * Expected : isolation
     */
    @Test
    public void testJobDefSpecificDifferentDefault() throws Exception
    {
        addAndStartEngine();

        createSubmitSetJob("test1");
        TestHelpers.waitFor(1, 10000, em);
        createSubmitGetJob("test2");
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(2, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    /**
     * Run test using JobDef parameter specific_isolation_context with two jobs using different specific_isolation_context values setting
     * launch_isolation_default to Shared.
     * 
     * Expected : isolation
     */
    @Test
    public void testJobDefSpecificDifferentShared() throws Exception
    {
        CreationTools.createGlobalParameter("launch_isolation_default", "Shared", em);
        addAndStartEngine();

        createSubmitSetJob("test1");
        TestHelpers.waitFor(1, 10000, em);
        createSubmitGetJob("test2");
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(2, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    /**
     * Tests that using a static field for the JobManager API works even with shared CL, but shows a warning.
     */
    @Test
    public void testJobDefSharedWithStaticJobManagerField() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiStaticInjection", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, -1, "TestSet", null, null, null, null, null, false, em,
                "mycontext");

        JobRequest.create("TestSet", null).submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }
}
