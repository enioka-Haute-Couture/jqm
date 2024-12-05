package com.enioka.jqm.integration.tests;

import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

import org.junit.Assert;
import org.junit.Test;

public class EngineJpmsTest extends JqmBaseTest {
    @Test
    public void testJpmsModuleStartsWithApi()
    {
        addAndStartEngine();

        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.jpms/com.enioka.jqm.tests.jpms.SimpleJpmsPayload", null,
            "jqm-tests/jqm-test-jpms/target/test.jar", TestHelpers.qVip, -1, "SimpleJpmsPayload", null, null, null, null, null, false,
            cnx, null, false);
        jqmClient.newJobRequest("SimpleJpmsPayload", null).enqueue();

        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testNonJpmsStartInClassicMode()
    {
        addAndStartEngine();

        CreationTools.createJobDef(null, true, "pyl.Nothing", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", TestHelpers.qVip, -1,
            "SimpleNonJpmsPayload", null, null, null, null, null, false, cnx, null, false);
        jqmClient.newJobRequest("SimpleNonJpmsPayload", null).enqueue();

        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testNonJpmsStartInJpmsMode()
    {
        addAndStartEngine();

        // Note we are using an automatic module name here.
        CreationTools.createJobDef(null, true, "test/pyl.Nothing", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", TestHelpers.qVip,
            -1, "SimpleNonJpmsPayload", null, null, null, null, null, false, cnx, null, false);
        jqmClient.newJobRequest("SimpleNonJpmsPayload", null).enqueue();

        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testJpmsWithJndiCall()
    {
        // This tests checks the JNDI provider located inside the ext layer is accessible to the payload.
        CreationTools.createJndiFile(cnx, "fs/test", "test resource", "/tmp");

        JqmSimpleTest.create(cnx, "test/pyl.JndiFile", "jqm-test-pyl-nodep").run(this);
    }

    @Test
    public void testNonJpmsWithJndiCall()
    {
        // Sanity check.
        CreationTools.createJndiFile(cnx, "fs/test", "test resource", "/tmp");

        JqmSimpleTest.create(cnx, "pyl.JndiFile", "jqm-test-pyl-nodep").run(this);
    }

    @Test
    public void testJpmsModuleStartsWithServiceLoaderCall()
    {
        addAndStartEngine();

        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.jpms/com.enioka.jqm.tests.jpms.JpmsPayloadWithService", null,
            "jqm-tests/jqm-test-jpms/target/test.jar", TestHelpers.qVip, -1, "SimpleJpmsPayload", null, null, null, null, null, false,
            cnx, null, false);
        jqmClient.newJobRequest("SimpleJpmsPayload", null).enqueue();

        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }
}
