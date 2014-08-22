package com.enioka.jqm.tools;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

public class ClassLoaderLeakCleaner
{
    private static Logger jqmlogger = Logger.getLogger(ClassLoaderLeakCleaner.class);

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
    }
}
