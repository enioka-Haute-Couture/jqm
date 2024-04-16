package com.enioka.jqm.runner.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a workaround for the deprecation of security managers in Java 17.<br>
 * It registers the security manager only if Java<17. Obviously this also removes the functionality in Java>=17. We should follow the JDK
 * ticket https://bugs.openjdk.java.net/browse/JDK-8199704 for an API allowing us to prevent System.exit in Java 17+.
 */
class SecurityManagerPayloadLoader
{
    private static Logger jqmlogger = LoggerFactory.getLogger(SecurityManagerPayloadLoader.class);

    private SecurityManagerPayloadLoader()
    {}

    static void registerIfPossible()
    {
        if (javaVersion() < 17)
        {
            Method getSecurityManagerMethod;
            Method setSecurityManagerMethod;
            Class<?> securityManagerPayloadClass;
            try
            {
                getSecurityManagerMethod = System.class.getMethod("getSecurityManager");
                setSecurityManagerMethod = System.class.getMethod("setSecurityManager",
                        SecurityManagerPayloadLoader.class.getClassLoader().loadClass("java.lang.SecurityManager"));
                securityManagerPayloadClass = SecurityManagerPayloadLoader.class.getClassLoader()
                        .loadClass("com.enioka.jqm.runner.java.SecurityManagerPayload");
            }
            catch (NoSuchMethodException e)
            {
                jqmlogger.warn("Could not retrieve getSecurityManager on a compatible Java version", e);
                return;
            }
            catch (SecurityException e)
            {
                jqmlogger.warn("Was prevented from retrieving getSecurityManager on a compatible Java version", e);
                return;
            }
            catch (ClassNotFoundException e)
            {
                jqmlogger.warn("Could not find SecurityManager class on a compatible Java version", e);
                return;
            }

            Object existingManager;
            try
            {
                existingManager = getSecurityManagerMethod.invoke(null);
            }
            catch (Exception e)
            {
                jqmlogger.warn("getSecurityManager failed", e);
                return;
            }

            if (existingManager == null)
            {
                try
                {
                    setSecurityManagerMethod.invoke(null, securityManagerPayloadClass.getDeclaredConstructor().newInstance());
                    jqmlogger.info("Security manager was registered");
                }
                catch (Exception e)
                {
                    jqmlogger.warn("Could not register the security manager", e);
                    return;
                }
            }
        }
    }

    static void unregisterIfPossible()
    {
        if (javaVersion() < 17)
        {
            Method getSecurityManagerMethod;
            Method setSecurityManagerMethod;
            Class<?> securityManagerPayloadClass;
            try
            {
                getSecurityManagerMethod = System.class.getMethod("getSecurityManager");
                setSecurityManagerMethod = System.class.getMethod("setSecurityManager",
                        SecurityManagerPayloadLoader.class.getClassLoader().loadClass("java.lang.SecurityManager"));
                securityManagerPayloadClass = SecurityManagerPayloadLoader.class.getClassLoader()
                        .loadClass("com.enioka.jqm.runner.java.SecurityManagerPayload");
            }
            catch (NoSuchMethodException e)
            {
                jqmlogger.warn("Could not retrieve getSecurityManager on a compatible Java version", e);
                return;
            }
            catch (SecurityException e)
            {
                jqmlogger.warn("Was prevented from retrieving getSecurityManager on a compatible Java version", e);
                return;
            }
            catch (ClassNotFoundException e)
            {
                jqmlogger.warn("Could not find SecurityManager class on a compatible Java version", e);
                return;
            }

            Object existingManager;
            try
            {
                existingManager = getSecurityManagerMethod.invoke(null);
            }
            catch (Exception e)
            {
                jqmlogger.warn("getSecurityManager failed", e);
                return;
            }

            if (existingManager == null || !securityManagerPayloadClass.isAssignableFrom(existingManager.getClass()))
            {
                return;
            }

            try
            {
                setSecurityManagerMethod.invoke(null, (Object) null);
                jqmlogger.info("Security manager was unregistered");
            }
            catch (Exception e)
            {
                jqmlogger.warn("Could not unregister the security manager", e);
                return;
            }
        }
    }

    private static double javaVersion()
    {
        String ver = System.getProperty("java.version");
        return Double.parseDouble(ver.substring(0, ver.indexOf('.') + 2));
    }

}
