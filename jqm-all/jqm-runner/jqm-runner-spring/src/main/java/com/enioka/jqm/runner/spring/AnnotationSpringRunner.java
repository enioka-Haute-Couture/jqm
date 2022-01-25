package com.enioka.jqm.runner.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import com.enioka.jqm.api.JavaJobRunner;
import com.enioka.jqm.api.JobRunnerException;

import org.osgi.service.component.annotations.Component;

/**
 * A very simple Spring context creation and maintenance. Designed to be used in shared class loaders, but also works with standard "use
 * once" class loaders. Will accept any job class which is using a Spring annotation.
 *
 */
@Component(service = JavaJobRunner.class, property = { "Plugin-Type=JavaJobRunner", "JavaJobRunner-Type=spring" })
public class AnnotationSpringRunner implements JavaJobRunner
{
    public AnnotationSpringRunner()
    {
        // Simple no-operation.
    }

    @Override
    public boolean canRun(Class<? extends Object> toRun)
    {
        // Check at least one annotation
        boolean found = false;
        for (Annotation a : toRun.getAnnotations())
        {
            if (a.annotationType().getName().contains("org.springframework"))
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            return false;
        }

        // Check spring-context is present in payload class loader, as well as our handler.
        try
        {
            toRun.getClassLoader().loadClass("org.springframework.context.annotation.AnnotationConfigApplicationContext");
            toRun.getClassLoader().loadClass("com.enioka.jqm.handler.AnnotationSpringContextBootstrapHandler");
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }

        // OK, we can run this.
        return true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void run(Class<? extends Object> toRun, Map<String, String> metaParameters, Map<String, String> jobParameters,
            Object handlerProxy)
    {
        // Retrieve the context from event handler. It is of course inside the payload class loader.
        Runnable o;
        Method handlerCleanUpMethod;
        try
        {
            Class handlerClass = Class.forName("com.enioka.jqm.handler.AnnotationSpringContextBootstrapHandler", true,
                    toRun.getClassLoader()); // with class init. Inside target CL.

            Method contextBeanLoader = handlerClass.getMethod("getBean", Class.class);
            o = (Runnable) contextBeanLoader.invoke(null, toRun);

            // Retrieve thread local bean holding parameters
            Method contextBeanLoaderS = handlerClass.getMethod("getBean", String.class);
            Map<String, String> prms = (Map<String, String>) contextBeanLoaderS.invoke(null, "runtimeParameters");
            prms.putAll(jobParameters);

            // Set thread local JM
            Class<? extends Object> jmClass = Class.forName("com.enioka.jqm.api.JobManager", true, toRun.getClassLoader());
            Method contextBeanLoaderJ = handlerClass.getMethod("setJm", jmClass);
            contextBeanLoaderJ.invoke(null, handlerProxy);

            // Prepare cleanup
            handlerCleanUpMethod = handlerClass.getMethod("cleanThread");

        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            return;
        }

        try
        {
            o.run();
        }
        finally
        {
            try
            {
                handlerCleanUpMethod.invoke(null);
            }
            catch (Exception e)
            {
                throw new JobRunnerException(e);
            }
        }
    }
}
