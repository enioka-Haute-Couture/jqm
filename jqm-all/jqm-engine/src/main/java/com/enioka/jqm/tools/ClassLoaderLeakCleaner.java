package com.enioka.jqm.tools;

import java.beans.Introspector;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

class ClassLoaderLeakCleaner
{
    private static Logger jqmlogger = Logger.getLogger(ClassLoaderLeakCleaner.class);

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
                if (domain.startsWith("java.") || domain.startsWith("com.sun.") || domain.startsWith("JMI")
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
}
