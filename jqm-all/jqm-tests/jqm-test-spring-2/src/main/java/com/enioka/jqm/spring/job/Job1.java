package com.enioka.jqm.spring.job;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.enioka.jqm.handler.JobManagerProvider;
import com.enioka.jqm.spring.service.Service1;

@Component
public class Job1 implements Runnable
{
    @Resource(name = "runtimeParameters")
    private Map<String, String> parameters;

    @Resource
    private Service1 s1;

    @Resource
    private JobManagerProvider jm;

    @Override
    public void run()
    {
        System.out.println("Spring job 1");
        if (parameters.size() == 0)
        {
            throw new RuntimeException("parameters were not set");
        }

        if (s1 == null)
        {
            throw new RuntimeException("services were not set");
        }

        if (!s1.getBeanName().equals("com.enioka.jqm.spring.service.Service1"))
        {
            throw new RuntimeException("wrong service bean name - custom bean name generator not in used. Name found: " + s1.getBeanName());
        }

        s1.getInt();

        if (jm == null)
        {
            throw new RuntimeException("JobManager was not set");
        }
        System.out.println("Job instance ID is " + jm.getObject().jobInstanceID());
    }
}
