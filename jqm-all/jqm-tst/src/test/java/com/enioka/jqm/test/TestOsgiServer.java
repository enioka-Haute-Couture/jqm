package com.enioka.jqm.test;

import org.junit.Test;

public class TestOsgiServer
{
    @Test
    public void testResolverSimple()
    {
        JqmTesterOsgiInternal tester = new JqmTesterOsgiInternal();
        tester.start();
    }
}
