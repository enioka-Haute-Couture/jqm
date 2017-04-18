package com.enioka.jqm.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.api.JobRunner;

/**
 * The most simple of all runners: this launches a static main method.
 */
public class MainRunner implements JobRunner
{
    public MainRunner()
    {}

    private Method getMainMethod(Class<? extends Object> toRun)
    {
        try
        {
            return toRun.getMethod("main");
        }
        catch (NoSuchMethodException e)
        {
            // Nothing - let's try with arguments
        }
        try
        {
            return toRun.getMethod("main", String[].class);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    @Override
    public boolean canRun(Class<? extends Object> toRun)
    {
        return getMainMethod(toRun) != null && Modifier.isStatic(getMainMethod(toRun).getModifiers());
    }

    @Override
    public void run(Class<? extends Object> toRun, Map<String, String> metaParameters, Map<String, String> jobParameters,
            Object handlerProxy)
    {
        Method start = getMainMethod(toRun);
        if (start == null)
        {
            throw new RuntimeException("invalid call - no static main method here.");
        }
        if (!Modifier.isStatic(start.getModifiers()))
        {
            throw new RuntimeException("The main type payload has a main function but it is not static");
        }

        // Injection
        Common.inject(toRun, null, handlerProxy, Boolean.parseBoolean(metaParameters.getOrDefault("mayBeShared", "false")));

        // Parameters
        String[] params = new String[jobParameters.size()];
        List<String> keys = new ArrayList<String>(jobParameters.keySet());
        Collections.sort(keys, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });
        int i = 0;
        for (String p : keys)
        {
            params[i] = jobParameters.get(p);
            i++;
        }

        // Start
        try
        {
            start.invoke(null, (Object) params);
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
        catch (Exception e)
        {
            throw new JobRunnerException("Payload launch failed for " + toRun.getCanonicalName() + ".", e);
        }
    }

}
