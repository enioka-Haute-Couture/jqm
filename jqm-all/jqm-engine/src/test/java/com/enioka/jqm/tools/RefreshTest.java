package com.enioka.jqm.tools;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.test.helpers.TestHelpers;

public class RefreshTest extends JqmBaseTest
{
    @Test
    public void testPauseResume() throws Exception
    {
        cnx.runUpdate("node_update_enabled_by_id", false, TestHelpers.node.getId());
        cnx.commit();

        // Submit request => nothing should happen
        JqmSimpleTest.create(cnx, "pyl.PckMain", "jqm-test-pyl-nodep").addWaitTime(3000).expectNonOk(0).expectOk(0).run(this);

        // Resume => JI should be run and end OK
        this.engines.get("localhost").resume();
        TestHelpers.waitFor(1, 5000, cnx);
        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
    }
}
