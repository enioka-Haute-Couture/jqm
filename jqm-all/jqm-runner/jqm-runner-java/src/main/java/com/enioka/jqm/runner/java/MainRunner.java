package com.enioka.jqm.runner.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.api.JavaJobRunner;
import com.enioka.jqm.api.JobRunnerException;

import org.osgi.service.component.annotations.Component;

/**
 * The most simple of all runners: this launches a static main method.
 */
@Component(service = JavaJobRunner.class, property = { "Plugin-Type=JavaJobRunner", "JavaJobRunner-Type=main" })
public class MainRunner implements JavaJobRunner
{
    public MainRunner()
    {
        // No special initialization.
    }

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
        Method m = getMainMethod(toRun);
        return m != null && Modifier.isStatic(m.getModifiers());
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
        String prm = metaParameters.get("mayBeShared") == null ? "false" : metaParameters.get("mayBeShared");
        Common.inject(toRun, null, handlerProxy, Boolean.parseBoolean(prm));

        // Parameters
        String[] params = new String[jobParameters.size()];
        List<String> keys = new ArrayList<>(jobParameters.keySet());
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
