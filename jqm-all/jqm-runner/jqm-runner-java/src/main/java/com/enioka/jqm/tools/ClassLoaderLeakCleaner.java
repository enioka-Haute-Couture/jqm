package com.enioka.jqm.tools;

import java.beans.Introspector;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ClassLoaderLeakCleaner
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ClassLoaderLeakCleaner.class);
    private static double javaVersion = getJavaVersion();

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

        // Some java 6 stuff
        if (javaVersion <= 1.8)
        {
            clearSunSoftCache(ObjectInputStream.class, "subclassAudits");
            clearSunSoftCache(ObjectOutputStream.class, "subclassAudits");
            clearSunSoftCache(ObjectStreamClass.class, "localDescs");
            clearSunSoftCache(ObjectStreamClass.class, "reflectors");
        }
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

    private static double getJavaVersion()
    {
        String ver = System.getProperty("java.version");
        return Double.parseDouble(ver.substring(0, ver.indexOf('.') + 2));
    }

    @SuppressWarnings("all")
    private static void clearSunSoftCache(Class clazz, String fieldName)
    {
        Map cache = null;
        try
        {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            cache = (Map) field.get(null);
        }
        catch (Throwable ignored)
        {
            // there is nothing a user could do about this anyway
        }

        if (cache != null)
        {
            synchronized (cache)
            {
                cache.clear();
            }
        }
    }
}
