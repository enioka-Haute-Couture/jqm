package com.enioka.jqm.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.api.JobRunner;

public class Common
{
    static void inject(Class<? extends Object> c, Object o, Object handlerProxy, boolean mayBeShared)
    {
        List<Field> ff = new ArrayList<Field>();
        Class<? extends Object> clazz = c;
        while (!clazz.equals(Object.class))
        {
            ff.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        boolean inject = false;
        for (Field f : ff)
        {
            if (JobManager.class.getName().equals(f.getType().getName()))
            {
                if (mayBeShared && Modifier.isStatic(f.getModifiers()))
                {
                    System.out.println("Injection done on a static field with shared isolation context - this may "
                            + "create weird behaviour and crashes of the JobManager API as this field is shared between "
                            + "all job instances created from this Job Definition. There should always be one instance"
                            + " of JobManager per running job instance.");
                }
                inject = true;
                break;
            }
        }
        if (!inject)
        {
            return;
        }

        Class<? extends Object> injInt = null;
        try
        {
            injInt = Thread.currentThread().getContextClassLoader().loadClass("com.enioka.jqm.api.JobManager");
        }
        catch (Exception e)
        {
            throw new JobRunner.JobRunnerException("Could not load JQM internal interface", e);
        }
        try
        {
            for (Field f : ff)
            {
                if (f.getType().equals(injInt))
                {
                    boolean acc = f.isAccessible();
                    f.setAccessible(true);
                    f.set(o, handlerProxy);
                    f.setAccessible(acc);
                }
            }
        }
        catch (Exception e)
        {
            throw new JobRunner.JobRunnerException("Could not inject JQM interface into target payload", e);
        }
    }
}
