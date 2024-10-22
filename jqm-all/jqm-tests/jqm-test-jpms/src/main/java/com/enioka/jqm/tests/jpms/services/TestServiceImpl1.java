package com.enioka.jqm.tests.jpms.services;

import com.enioka.jqm.tests.jpms.api.TestService;

public class TestServiceImpl1 implements TestService
{
    @Override
    public void test()
    {
        System.out.println("TestServiceImpl1 has run!");
    }
}
