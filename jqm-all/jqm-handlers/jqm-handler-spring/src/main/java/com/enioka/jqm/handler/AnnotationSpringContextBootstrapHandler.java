package com.enioka.jqm.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
    /**
     * The main goal of this handler is to share this Spring context. As it is loaded inside the payload CL, there is one per execution
     * context.
     */
    private static AnnotationConfigApplicationContext ctx;
    private static volatile Boolean refreshed = false;

    private final static String THREAD_SCOPE_NAME = "thread";

    final static ThreadLocal<JobManager> localJm = new ThreadLocal<>();
    final static ThreadScope threadScope;

    static
    {
        // Implementation choice: we use annotations.
        ctx = new AnnotationConfigApplicationContext();

        // There is no "request" scope in an AnnotationConfigApplicationContext, as we are not inside a web container.
        // For JQM, we register a "thread" scope as one job instance = one thread, so it can be most useful.
        threadScope = new ThreadScope();
        ctx.getBeanFactory().registerScope(THREAD_SCOPE_NAME, threadScope);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Collaboration methods with the runner
    ///////////////////////////////////////////////////////////////////////////

    public static void setJm(JobManager jm)
    {
        localJm.set(jm);
    }

    public static void cleanThread()
    {
        localJm.remove();
        threadScope.closeThread();
    }

    public static Object getBean(String beanName)
    {
        return ctx.getBean(beanName);
    }

    public static Object getBean(Class<? extends Object> clazz)
    {
        return ctx.getBean(clazz);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Main
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    @Override
    public void run(Class<? extends Object> toRun, JobManager handler, Map<String, String> handlerParameters)
    {
        // initialize the spring context with all requested classes on first boot -
        // and only on first boot: the Spring context cannot be refreshed multiple times.
        if (!refreshed)
        {
            synchronized (AnnotationSpringContextBootstrapHandler.class)
            {
                if (!refreshed)
                {
                    // Add the different elements to the context path
                    if (handlerParameters.containsKey("beanNameGenerator"))
                    {
                        Class<? extends BeanNameGenerator> clazz;
                        try
                        {
                            clazz = (Class<? extends BeanNameGenerator>) AnnotationSpringContextBootstrapHandler.class.getClassLoader()
                                    .loadClass(handlerParameters.get("beanNameGenerator"));
                        }
                        catch (ClassNotFoundException e)
                        {
                            throw new RuntimeException("The handler beanNameGenerator class [" + handlerParameters.get("beanNameGenerator")
                                    + "] cannot be found.", e);
                        }

                        BeanNameGenerator generator;
                        try
                        {
                            generator = clazz.newInstance();
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException("The handler beanNameGenerator class [" + handlerParameters.get("beanNameGenerator")
                                    + "] was found but cannot be instantiated. Check it has a no-arguments constructor.", e);
                        }

                        ctx.setBeanNameGenerator(generator);
                    }

                    if (handlerParameters.containsKey("contextDisplayName"))
                    {
                        ctx.setDisplayName(handlerParameters.get("contextDisplayName"));
                    }

                    if (handlerParameters.containsKey("contextId"))
                    {
                        ctx.setId(handlerParameters.get("contextId"));
                    }

                    if (handlerParameters.containsKey("allowCircularReferences"))
                    {
                        boolean allow = Boolean.parseBoolean(handlerParameters.get("allowCircularReferences"));
                        ctx.setAllowCircularReferences(allow);
                    }

                    if (handlerParameters.containsKey("additionalScan"))
                    {
                        ctx.scan(handlerParameters.get("additionalScan").split(","));
                    }
                    else
                    {
                        ctx.register(toRun);
                    }

                    // Create a holder for the parameters.
                    BeanDefinition def = new RootBeanDefinition(HashMap.class); // TODO: remove this in v3
                    def.setScope(THREAD_SCOPE_NAME);
                    ctx.registerBeanDefinition("runtimeParameters", def);

                    def = new RootBeanDefinition(ParametersProvider.class);
                    def.setScope(BeanDefinition.SCOPE_SINGLETON);
                    ctx.registerBeanDefinition("runtimeParametersProvider", def);

                    // This is a factory to retrieve the JobManager. We cannot just inject the JM, as it is created at runtime, long after
                    // the context has been created!
                    def = new RootBeanDefinition(JobManagerProvider.class);
                    def.setScope(BeanDefinition.SCOPE_SINGLETON);
                    ctx.registerBeanDefinition("jobManagerProvider", def);

                    // It would however be possible to use a lazy bean injection. But this would require the user to put @Lazy everywhere,
                    // or else the result is null... Too fragile. The provider/factory pattern above does the same thing and forces the user
                    // to do a lazy call.
                    // def = new RootBeanDefinition(JobManager.class);
                    // def.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
                    // def.setLazyInit(true);
                    // def.setFactoryBeanName("jobManagerProvider");
                    // def.setFactoryMethodName("getObject");
                    // ctx.registerBeanDefinition("jobManager", def);

                    // Go: this initializes the context
                    ctx.refresh();
                    refreshed = true;
                }
            }
        }
    }
}
