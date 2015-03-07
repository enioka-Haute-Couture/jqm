package com.enioka.jqm.tools;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.test.helpers.TestHelpers;

public class RefreshTest extends JqmBaseTest
{
    @Test
    public void testPauseResume() throws Exception
    {
        em.getTransaction().begin();
        TestHelpers.node.setEnabled(false);
        em.getTransaction().commit();

        // Submit request => nothing should happen
        JqmSimpleTest.create(em, "pyl.PckMain", "jqm-test-pyl-nodep").addWaitTime(3000).expectNonOk(0).expectOk(0).run(this);

        // Resume => JI should be run and end OK
        this.engines.get("localhost").resume();
        TestHelpers.waitFor(1, 5000, em);
        Assert.assertEquals(1, TestHelpers.getOkCount(em));
    }
}
