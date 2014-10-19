package com.enioka.jqm.test;

import com.enioka.jqm.api.JobManager;

/**
 * Simple echo with parameters
 */
public class Payload2 implements Runnable
{
    private JobManager jm;

    @Override
    public void run()
    {
        System.out.println("I am payload two");
        if (!jm.parameters().containsKey("arg1"))
        {
            throw new RuntimeException("arg is missing!");
        }
        System.out.println(jm.parameters().get("arg1"));
    }
}
