package com.enioka.jqm.runner.java;

import java.util.Map;

import org.kohsuke.MetaInfServices;

import com.enioka.jqm.api.JavaJobRunner;
import com.enioka.jqm.api.JobRunnerException;

/**
 * A runner for classes which implement Runnable with a no-args constructor.
 */
@MetaInfServices(JavaJobRunner.class)
public class RunnableRunner implements JavaJobRunner
{
    public RunnableRunner()
    {
        // No special initialization.
    }

    @Override
    public boolean canRun(Class<? extends Object> toRun)
    {
        return Runnable.class.isAssignableFrom(toRun);
    }

    @Override
    public void run(Class<? extends Object> toRun, Map<String, String> metaParameters, Map<String, String> jobParameters,
            Object handlerProxy)
    {
        @SuppressWarnings("unchecked")
        Class<Runnable> c = (Class<Runnable>) toRun;
        Runnable o = null;
        try
        {
            o = c.newInstance();
        }
        catch (Exception e)
        {
            throw new JobRunnerException("Could not instanciate runnable payload. Does it have a nullary constructor?", e);
        }

        // Injection stuff (if needed)
        Common.inject(o.getClass(), o, handlerProxy, false);

        // Go
        o.run();
    }

}
