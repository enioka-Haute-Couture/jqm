package com.enioka.jqm.tests.jpms;

import java.util.ServiceLoader;

import com.enioka.jqm.tests.jpms.api.TestService;

public class JpmsPayloadWithService implements Runnable
{
    public void run()
    {
        ServiceLoader<TestService> sl = ServiceLoader.load(TestService.class);
        sl.forEach(TestService::test);
    }
}
