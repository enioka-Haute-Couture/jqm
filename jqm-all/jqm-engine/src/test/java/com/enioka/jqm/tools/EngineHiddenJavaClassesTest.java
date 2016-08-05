package com.enioka.jqm.tools;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class EngineHiddenJavaClassesTest extends JqmBaseTest
{
    
    /**
     * Hide java.math.BigInteger from parent class loader causing job to fail
     */
    @Test
    public void testHiddenBigInteger() throws Exception
    {
        addAndStartEngine();

        CreationTools.createJobDef(null, true, "pyl.EngineHiddenJavaClasses", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, -1, "EngineHiddenJavaClasses", null, null, null, null, null, false, em,
                null, false, "java.lang.String,java.math.*"); 
        JobRequest.create("EngineHiddenJavaClasses", null).submit();

        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(0, TestHelpers.getOkCount(em));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(em));
    }

    /**
     * Hide java.net.* but not java.math.BigInteger, job exits successfully 
     */
    @Test
    public void testNoMatches() throws Exception
    {
        addAndStartEngine();

        CreationTools.createJobDef(null, true, "pyl.EngineHiddenJavaClasses", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, -1, "EngineHiddenJavaClasses", null, null, null, null, null, false, em,
                null, false, "java.net.*"); 
        JobRequest.create("EngineHiddenJavaClasses", null).submit();

        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }
    
}
