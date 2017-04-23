package com.enioka.jqm.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.SimpleThreadScope;

import com.enioka.jqm.api.JobInstanceStartedHandler;
import com.enioka.jqm.api.JobManager;

/**
 * A handler which creates a Spring annotation parsing context if it does not already exists. Can take optional parameters which specify
 * additional packages to scan. <br>
 * <br>
 * As for all event handlers, it should not be instantiated directly but be referenced in a &lt;context&gt; tag in a deployment descriptor.
 */
public class AnnotationSpringContextBootstrapHandler implements JobInstanceStartedHandler
{
    private static AnnotationConfigApplicationContext ctx;
    private static volatile Boolean refreshed = false;

    private final static String THREAD_SCOPE_NAME = "thread";

    static
    {
        // Implementation choice: we use annotations.
        ctx = new AnnotationConfigApplicationContext();

        // There is no "request" scope in an AnnotationConfigApplicationContext, as we are not inside a web container.
        // For JQM, we register a "thread" scope as one job instance = one thread, so it can be most useful.
        Scope threadScope = new SimpleThreadScope();
        ctx.getBeanFactory().registerScope(THREAD_SCOPE_NAME, threadScope);
    }

    public static Object getBean(String beanName)
    {
        return ctx.getBean(beanName);
    }

    public static Object getBean(Class<? extends Object> clazz)
    {
        return ctx.getBean(clazz);
    }

    @Override
    public void run(Class<? extends Object> toRun, JobManager handler, Map<String, String> handlerParameters)
    {
        // initialize the spring context with all requested classes on first boot -
        // and only on first boot: the Spring context cannot be refreshed multiple times.
        if (!refreshed)
        {
            synchronized (refreshed)
            {
                if (!refreshed)
                {
                    // Add the different elements to the context path
                    if (handlerParameters.containsKey("additionalScan"))
                    {
                        ctx.scan(handlerParameters.get("additionalScan").split(","));
                    }
                    else
                    {
                        ctx.register(toRun);
                    }

                    // Create a holder for JQM injected items.
                    BeanDefinition def = new RootBeanDefinition(HashMap.class);
                    def.setScope(THREAD_SCOPE_NAME);
                    ctx.registerBeanDefinition("runtimeParameters", def);

                    def = new RootBeanDefinition(JobManager.class);
                    def.setScope(THREAD_SCOPE_NAME);
                    ctx.registerBeanDefinition("jobManager", def);

                    // Go: this initializes the context
                    ctx.refresh();
                    refreshed = true;
                }
            }
        }
    }
}
