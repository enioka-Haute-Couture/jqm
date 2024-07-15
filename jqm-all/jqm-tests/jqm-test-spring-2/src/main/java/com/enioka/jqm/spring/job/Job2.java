package com.enioka.jqm.spring.job;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.spring.service.Service1;

@Component
@Scope("thread")
public class Job2 implements Runnable
{
    @Resource(name = "runtimeParameters")
    private Map<String, String> parameters;

    @Resource(name = "runtimeParametersProvider")
    private ObjectFactory<Map<String, String>> parametersProvider;

    @Resource(name = "jobManagerProvider")
    private ObjectFactory<JobManager> jmp;

    @Resource
    private Service1 s1;

    @Override
    public void run()
    {
        System.out.println("Spring job 2");

        // Service injection test: context should work.
        if (s1 == null)
        {
            throw new RuntimeException("services were not set");
        }
        s1.getInt();

        // JQM service injected?
        if (jmp == null)
        {
            throw new RuntimeException("JobManager was not set");
        }

        // Legacy parameter tests
        if (parameters == null || parameters.size() == 0)
        {
            throw new RuntimeException("parameters were not set");
        }
        System.out.println(parameters);
        System.out.println(jmp.getObject().parameters());
        if (parameters.get("key1") == null || !parameters.get("key1").equals("valueKey1FromRequestNotDefinition"))
        {
            throw new RuntimeException("parameter value is wrong: " + parameters.get("key1"));
        }
        if (parameters.get("key2") == null || !parameters.get("key2").equals("valueKey2FromDefinition"))
        {
            throw new RuntimeException("parameter value is wrong: " + parameters.get("key2"));
        }

        // Check parameters consistency between legacy parameters and JM
        if (jmp.getObject().parameters() == null)
        {
            throw new RuntimeException("JobManageris set but with null parameters!");
        }
        if (jmp.getObject().parameters().size() != parameters.size())
        {
            throw new RuntimeException("Different parameter count in JM and injected map");
        }
        for (Map.Entry<String, String> e : jmp.getObject().parameters().entrySet())
        {
            if (!e.getValue().equals(parameters.get(e.getKey())))
            {
                throw new RuntimeException("Different parameter " + e.getValue() + " value (" + e.getValue() + " in JM, but "
                        + parameters.get(e.getKey()) + " in injected map).");
            }
        }

        // New-style parameter tests
        if (parametersProvider == null || parametersProvider.getObject() == null || parametersProvider.getObject().size() == 0)
        {
            throw new RuntimeException("new-style parameters were not set");
        }
        Map<String, String> newParameters = parametersProvider.getObject();
        System.out.println(newParameters);
        System.out.println(jmp.getObject().parameters());
        if (newParameters.get("key1") == null || !newParameters.get("key1").equals("valueKey1FromRequestNotDefinition"))
        {
            throw new RuntimeException("parameter value is wrong: " + newParameters.get("key1"));
        }
        if (newParameters.get("key2") == null || !newParameters.get("key2").equals("valueKey2FromDefinition"))
        {
            throw new RuntimeException("parameter value is wrong: " + newParameters.get("key2"));
        }

        // Test calling JM methods
        JobManager jm = jmp.getObject();
        System.out.println("Job instance ID is " + jm.jobInstanceID());
        long instanceId = jm.jobInstanceID();
        jm.enqueueSync("Job1", jm.userName(), null, null, null, jm.module(), null, null, null, jm.parameters());

        if (instanceId != jmp.getObject().jobInstanceID())
        {
            throw new RuntimeException("The job manager was not really thread local");
        }
    }
}
