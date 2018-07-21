package com.enioka.jqm.tools;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

/**
 * A set of configuration launches helpful for some bug inquiries.
 *
 */
public class ZHelperManualTest extends JqmBaseTest
{
    //@Test
    public void testDbFailureUnderLoadWithExternalDb() throws Exception
    {
        // Many starting jobs simultaneously
        cnx.runUpdate("dp_update_threads_by_id", 50, TestHelpers.dpVip.getId());
        cnx.commit();
        TestHelpers.setNodesLogLevel("INFO", cnx);

        CreationTools.createJobDef(null, true, "pyl.Nothing", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", TestHelpers.qVip, -1,
                "TestJqmApplication", "appFreeName", "TestModule", "kw1", "kw2", "kw3", false, cnx);

        JobRequest j = new JobRequest("TestJqmApplication", "TestUser");

        int ji = 0;
        for (int i = 0; i < 1000; i++)
        {
            ++ji;
            JqmClientFactory.getClient().enqueue(j);
        }

        addAndStartEngine();

        // TestHelpers.waitFor(ji, 120000, this.getNewDbSession());
        this.sleep(1000);
    }

    //@Test
    public void testNoDuplicateLaunchesUnderLoad() throws Exception
    {
        // Many starting jobs simultaneously
        cnx.runUpdate("dp_update_threads_by_id", 50, TestHelpers.dpVip.getId());
        cnx.commit();
        TestHelpers.setNodesLogLevel("INFO", cnx);

        CreationTools.createJobDef(null, true, "pyl.Nothing", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", TestHelpers.qVip, -1,
                "TestJqmApplication", "appFreeName", "TestModule", "kw1", "kw2", "kw3", false, cnx);

        JobRequest j = new JobRequest("TestJqmApplication", "TestUser");

        int ji = 0;
        for (int i = 0; i < 1000; i++)
        {
            ++ji;
            JqmClientFactory.getClient().enqueue(j);
        }

        addAndStartEngine();

        TestHelpers.waitFor(ji, 120000, this.getNewDbSession());
        this.sleep(3);
        Assert.assertEquals(1000, TestHelpers.getHistoryAllCount(cnx));
    }
}
