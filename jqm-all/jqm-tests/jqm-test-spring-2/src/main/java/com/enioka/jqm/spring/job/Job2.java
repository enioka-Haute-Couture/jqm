package com.enioka.jqm.spring.job;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.handler.JobManagerProvider;
import com.enioka.jqm.spring.service.Service1;

@Component
public class Job2 implements Runnable
{
    @Resource(name = "runtimeParameters")
    private Map<String, String> parameters;

    @Resource
    private Service1 s1;

    @Resource
    private JobManagerProvider jmp;

    @Override
    public void run()
    {
        System.out.println("Spring job 2");
        if (parameters.size() == 0)
        {
            throw new RuntimeException("parameters were not set");
        }

        if (s1 == null)
        {
            throw new RuntimeException("services were not set");
        }
        s1.getInt();

        if (jmp == null)
        {
            throw new RuntimeException("JobManager was not set");
        }
        JobManager jm = jmp.getObject();
        System.out.println("Job instance ID is " + jm.jobInstanceID());
        int instanceId = jm.jobInstanceID();
        jm.enqueueSync("Job1", jm.userName(), null, null, null, jm.module(), null, null, null, jm.parameters());

        if (instanceId != jmp.getObject().jobInstanceID())
        {
            throw new RuntimeException("The job manager was not really thread local");
        }
    }
}
