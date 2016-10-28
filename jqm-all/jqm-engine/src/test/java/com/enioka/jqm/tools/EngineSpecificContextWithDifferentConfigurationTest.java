package com.enioka.jqm.tools;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class EngineSpecificContextWithDifferentConfigurationTest extends JqmBaseTest
{

    @Test
    public void testSameSpecificContextDifferentCFCL() throws Exception
    {
        addAndStartEngine();

        CreationTools.createJobDef(null, true, "com.enioka.jqm.TestCLIsolation.TestGet", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-cl-isolation/target/test.jar", TestHelpers.qVip, -1, "TestGet", null, null, null, null, null, false, em,
                "specificContext", false, "");
        JobRequest.create("TestGet", null).submit();
        TestHelpers.waitFor(1, 10000, em);

        CreationTools.createJobDef(null, true, "com.enioka.jqm.TestCLIsolation.TestSet", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-cl-isolation/target/test.jar", TestHelpers.qVip, -1, "TestSet", null, null, null, null, null, false, em,
                "specificContext", true, "");
        JobRequest.create("TestSet", null).submit();
        TestHelpers.waitFor(2, 10000, em);


        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testSameSpecificContextDifferentHJC() throws Exception
    {
        addAndStartEngine();

        CreationTools.createJobDef(null, true, "com.enioka.jqm.TestCLIsolation.TestGet", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-cl-isolation/target/test.jar", TestHelpers.qVip, -1, "TestGet", null, null, null, null, null, false, em,
                "specificContext", true, "HIDDEN");
        JobRequest.create("TestGet", null).submit();
        TestHelpers.waitFor(1, 10000, em);

        CreationTools.createJobDef(null, true, "com.enioka.jqm.TestCLIsolation.TestSet", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-cl-isolation/target/test.jar", TestHelpers.qVip, -1, "TestSet", null, null, null, null, null, false, em,
                "specificContext", true, "hidden");
        JobRequest.create("TestSet", null).submit();
        TestHelpers.waitFor(2, 10000, em);


        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(em));
    }
}
