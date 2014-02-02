package com.enioka.jqm.api.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.enioka.jqm.api.JqmClientFactory;

public class BasicTest
{
    private static Logger jqmlogger = Logger.getLogger(BasicTest.class);

    @Test
    public void testChain()
    {
        // No exception allowed!
        JqmClientFactory.getClient().getQueues();
        jqmlogger.info("q1");
        JqmClientFactory.getClient().getQueues();
        jqmlogger.info("q2");
    }
}
