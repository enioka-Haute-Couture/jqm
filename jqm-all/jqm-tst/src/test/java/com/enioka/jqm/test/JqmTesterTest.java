package com.enioka.jqm.test;

import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.State;

import org.junit.Assert;
import org.junit.Test;

public class JqmTesterTest
{
    // Simple run
    @Test
    public void testOne()
    {
        JobInstance res = JqmSynchronousTesterJse.create("com.enioka.jqm.test.Payload1").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }

    // Arguments
    @Test
    public void testTwo()
    {
        JobInstance res = JqmSynchronousTesterJse.create("com.enioka.jqm.test.Payload2").addParameter("arg1", "testvalue").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }

    // JNDI JDBC datasource
    @Test
    public void testThree()
    {
        JobInstance res = JqmSynchronousTesterJse.create("com.enioka.jqm.test.Payload3").addParameter("arg1", "testvalue").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }

    // Payload as a class, not a string.
    @Test
    public void testFour()
    {
        JobInstance res = JqmSynchronousTesterJse.create(Payload3.class).addParameter("arg1", "testvalue").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }
}
