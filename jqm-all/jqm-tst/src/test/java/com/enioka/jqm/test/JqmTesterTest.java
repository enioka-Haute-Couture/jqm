package com.enioka.jqm.test;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.State;

public class JqmTesterTest
{
    // Simple run
    @Test
    public void testOne()
    {
        JobInstance res = JqmTester.create("com.enioka.jqm.test.Payload1").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }

    // Arguments
    @Test
    public void testTwo()
    {
        JobInstance res = JqmTester.create("com.enioka.jqm.test.Payload2").addParameter("arg1", "testvalue").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }

    // JNDI JDBC datasource
    @Test
    public void testThree()
    {
        JobInstance res = JqmTester.create("com.enioka.jqm.test.Payload3").addParameter("arg1", "testvalue").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }
}
