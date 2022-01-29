package com.enioka.jqm.test;

import org.junit.Test;

public class TestOsgiServer
{
    @Test
    public void testResolverSimple()
    {
        System.setProperty("com.enioka.jqm.cl.allow_system_cl", "true"); // Needed to avoid cleaning JqmTesterOsgiInternal.framework
        JqmTesterOsgiInternal tester = new JqmTesterOsgiInternal();
        tester.start();
    }
}
