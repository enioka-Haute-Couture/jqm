package com.enioka.jqm.cl;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import com.enioka.jqm.shared.exceptions.JqmRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to retrieve a class loader with access to JDK classes as well as /ext, but nothing more. (particularly : no access to engine
 * classes)
 */
public final class ExtClassLoader
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ExtClassLoader.class);

    public static ClassLoader instance = initClassLoader();

    private ExtClassLoader()
    {}

    private static ClassLoader initClassLoader()
    {
        ClassLoader extClassLoader;

        // Java 8?
        boolean tmpBool = false;
        Method getPlatformClassLoaderMethod = null;
        try
        {
            getPlatformClassLoaderMethod = ClassLoader.class.getMethod("getPlatformClassLoader");
        }
        catch (NoSuchMethodException e)
        {
            tmpBool = true;
        }
        catch (SecurityException e)
        {
            // Ignore.
        }
        final boolean java8 = tmpBool;

        // Parent for the ext CL?
        final ClassLoader parentCl;
        if (System.getProperty("com.enioka.jqm.cl.allow_system_cl", "false").equals("true"))
        {
            jqmlogger.debug("As allow_system_cl is true, some JQM classes may be visible to payloads");
            parentCl = ClassLoader.getSystemClassLoader(); // itself has the platform CL as parent.
        }
        else
        {
            try
            {
                parentCl = java8 ? null : (ClassLoader) getPlatformClassLoaderMethod.invoke(null);
            }
            catch (Exception e)
            {
                throw new JqmRuntimeException("Could not get parent classloader", e);
            }
        }

        // List all jars inside ext directory
        File extDir = new File(getRootDir(), "ext/");
        jqmlogger.info("Using {} as JQM_ROOT directory", extDir);
        if (extDir.isDirectory())
        {
            jqmlogger.debug("Using {} as ext resource directory", extDir.getAbsolutePath());

            // Create classloader
            final URL[] aUrls = getJarsInDirectoryRecursive(extDir).toArray(new URL[0]);
            for (URL u : aUrls)
            {
                jqmlogger.trace(u.toString());
            }
            extClassLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>()
            {
                @Override
                public URLClassLoader run()
                {
                    // Java 8 : parent is default system CL, also called bootstrap CL which has all JDK classes.
                    // Java 9 and above: parent is the platform CL, which holds the same classes.
                    return new URLClassLoader(aUrls, parentCl);
                }
            });
        }
        else
        {
            // No /ext directory means nothing to load.
            return parentCl;
        }

        return extClassLoader;
    }

    private static List<URL> getJarsInDirectoryRecursive(File root)
    {
        List<URL> result = new ArrayList<>(10);

        if (!root.isDirectory() || !root.canExecute())
        {
            throw new RuntimeException("cannot access directory " + root.getAbsolutePath());
        }

        for (File f : root.listFiles())
        {
            if (f.isFile() && f.canRead() && (f.getName().endsWith(".jar") || f.getName().endsWith(".war") || f.getName().endsWith(".bar")))
            {
                try
                {
                    result.add(f.toURI().toURL());
                }
                catch (MalformedURLException e)
                {
                    jqmlogger.warn("Error when parsing the content of ext directory. File will be ignored", e);
                }
            }

            if (f.isDirectory() && f.canExecute())
            {
                result.addAll(getJarsInDirectoryRecursive(f));
            }
        }

        return result;
    }

    /**
     * A helper to retrieve the installation path of JQM. Usually determined by running jar, but can change by property during tests and
     * non-standard deployments.
     *
     * @return
     */
    public static String getRootDir()
    {
        String rootPath;
        try
        {
            rootPath = System.getProperty("com.enioka.jqm.alternateJqmRoot", null);
            if (rootPath == null)
            {
                // logger class because it is always shared with the host, not ever in an OSGi CL and cannot be unloaded.
                File currentJar = new File(org.slf4j.Logger.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                rootPath = currentJar.getParentFile().getParent(); // log lib is always inside JQM_ROOT/lib.
            }
        }
        catch (Exception e)
        {
            rootPath = ".";
        }
        jqmlogger.debug("Using {} as root JQM directory", rootPath);
        return rootPath;
    }
}
