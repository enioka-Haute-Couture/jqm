package com.enioka.jqm.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.enioka.jqm.api.JobBase;
import com.enioka.jqm.api.JobRunner;

/**
 * A runner for the deprecated "JobBase" type of jobs.
 */
@SuppressWarnings("deprecation")
public class LegacyRunner implements JobRunner
{
    public LegacyRunner()
    {
        // No special initialization.
    }

    @Override
    public boolean canRun(Class<? extends Object> toRun)
    {
        Class<? extends Object> clazz = toRun;
        while (!clazz.equals(Object.class))
        {
            if (clazz.getName().equals(JobBase.class.getName()))
            {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    @Override
    public void run(Class<? extends Object> toRun, Map<String, String> metaParameters, Map<String, String> jobParameters,
            Object handlerProxy)
    {
        Object o = null;
        try
        {
            o = toRun.newInstance();
        }
        catch (Exception e)
        {
            throw new JobRunnerException(
                    "Cannot create an instance of class " + toRun.getCanonicalName() + ". Does it have an argumentless constructor?", e);
        }

        // Injection
        String prm = metaParameters.get("mayBeShared") == null ? "false" : metaParameters.get("mayBeShared");
        Common.inject(o.getClass(), o, handlerProxy, Boolean.parseBoolean(prm));

        try
        {
            // Start method that we will have to call
            Method start = toRun.getMethod("start");
            start.invoke(o);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof RuntimeException)
            {
                // it may be a Kill order, or whatever exception...
                throw (RuntimeException) e.getCause();
            }
            else
            {
                throw new JobRunnerException("Payload has failed", e);
            }
        }
        catch (NoSuchMethodException e)
        {
            throw new JobRunnerException("Payload " + toRun.getCanonicalName() + " is incorrect - missing fields and methods.", e);
        }
        catch (Exception e)
        {
            throw new JobRunnerException("Payload launch failed for " + toRun.getCanonicalName() + ".", e);
        }
    }

}
