package com.enioka.jqm.cl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

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
    {
    }

    private static ClassLoader initClassLoader()
    {
        ClassLoader extClassLoader;

        // Java 8 ?
        boolean tmpBool = false;
        try
        {
            ClassLoader.class.getMethod("getPlatformClassLoader");
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

        // List all jars inside ext directory
        File extDir = new File("ext/");
        if (extDir.isDirectory())
        {
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
                    return new URLClassLoader(aUrls, java8 ? null : ClassLoader.getPlatformClassLoader());
                }
            });
        }
        else
        {
            // No /ext directory means nothing to load.
            return null;
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

}
