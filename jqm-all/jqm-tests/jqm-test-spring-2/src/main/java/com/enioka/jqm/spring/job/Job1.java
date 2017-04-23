package com.enioka.jqm.spring.job;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.enioka.jqm.spring.service.Service1;

@Component
public class Job1 implements Runnable
{
    @Resource(name = "runtimeParameters")
    private Map<String, String> parameters;

    @Resource
    private Service1 s1;

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
        s1.getInt();
    }
}
