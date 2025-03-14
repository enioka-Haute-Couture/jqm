package com.enioka.jqm.runner.java;

import java.beans.Introspector;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ClassLoaderLeakCleaner
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ClassLoaderLeakCleaner.class);

    private ClassLoaderLeakCleaner()
    {
        // Helper class only.
    }

    static void clean(ClassLoader cl)
    {
        // MBeans
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        String domain = "";
        for (ObjectName mbean : mbs.queryNames(null, null))
        {
            try
            {
                domain = mbean.getDomain();
                if (domain.startsWith("java.") || domain.startsWith("com.sun.") || domain.startsWith("jdk.") || domain.startsWith("JMI")
                        || domain.startsWith("com.enioka."))
                {
                    continue;
                }

                if (mbs.getClassLoaderFor(mbean) == null || mbs.getClassLoaderFor(mbean).equals(cl))
                {
                    jqmlogger.info("A JMX Mbean will be forcefully unregistered to avoid classloader leaks: " + mbean.getCanonicalName());
                    mbs.unregisterMBean(mbean);
                }
            }
            catch (Exception e)
            {
                // Don't do anything - the failure of the failsafe is not a failure in itself.
                jqmlogger.info("Failed to clean JMX MBean " + mbean.getCanonicalName(), e);
            }
        }

        // Bean cache
        Introspector.flushCaches();

        // Runaway threads
        cleanThreads();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static void cleanJdbc(Thread t)
    {
        try
        {
            Class c = t.getContextClassLoader().loadClass("com.enioka.jqm.providers.PayloadInterceptor");
            Method m = c.getMethod("forceCleanup", Thread.class);
            int i = (Integer) m.invoke(null, t);
            if (i > 0)
            {
                jqmlogger.warn("Thread " + t.getName() + " has leaked " + i
                        + " JDBC connections! Contact the payload developer and have him close his connections correctly");
            }
        }
        catch (ClassNotFoundException e)
        {
            // This happens if the resource providers are not present. Not an issue.
            return;
        }
        catch (Exception e)
        {
            jqmlogger.warn("An error occured during JDBC connections cleanup - connections may leak", e);
        }
    }

    private static void cleanThreads()
    {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();

        synchronized (tg)
        {
            Thread[] threads = new Thread[tg.activeCount()];
            int tCount = tg.enumerate(threads);

            for (int i = 0; i < tCount; i++)
            {
                Thread t = threads[i];
                if (t == null || t == Thread.currentThread() || t.getContextClassLoader() != Thread.currentThread().getContextClassLoader())
                {
                    // Only kill threads that are not the current one and that are an issue, that is prevent GC of the classloader.
                    continue;
                }

                jqmlogger.warn("Runaway thread. Cleaner has interrupted thread {}", t.getName());
                t.setContextClassLoader(null);
                t.interrupt();
            }
        }
    }
}
