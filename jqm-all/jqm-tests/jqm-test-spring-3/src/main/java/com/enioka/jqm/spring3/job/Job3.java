package com.enioka.jqm.spring3.job;

import org.springframework.stereotype.Component;

@Component
public class Job3 implements Runnable
{
    @Override
    public void run()
    {
        if (getClass().getClassLoader().getResource("extra-classpath-test.txt") == null)
        {
            throw new RuntimeException("Resource 'extra-classpath-test.txt' was not found in classpath");
        }
        System.out.println("Resource 'extra-classpath-test.txt' was found in classpath");
    }
}
