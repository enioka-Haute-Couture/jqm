package com.enioka.tests;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.State;
import com.enioka.jqm.test.JqmTester;

public class TestPayload
{
    @Test
    public void test()
    {
        JobInstance res = JqmTester.create("com.enioka.tests.App").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }
}
