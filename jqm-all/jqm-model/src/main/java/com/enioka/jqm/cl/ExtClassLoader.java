package com.enioka.jqm.cl;

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static ModuleLayer moduleLayerInstance;
    public static ClassLoader classLoaderInstance;

    static {
        initInstances();
    }

    private ExtClassLoader()
    {}

    private static void initInstances()
    {
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
        jqmlogger.info("Using {} as JQM_ROOT/ext directory", extDir);
        if (extDir.isDirectory())
        {
            jqmlogger.debug("Using {} as ext resource directory", extDir.getAbsolutePath());

            // Find candidate jars
            final List<File> lJarFiles = getJarsInDirectoryRecursive(extDir);
            final List<URL> lUrls = new ArrayList<>();
            final List<Path> lPaths = new ArrayList<>();
            for (final File file : lJarFiles) {
                try {
                    lUrls.add(file.toURI().toURL());
                    lPaths.add(file.toPath());
                    jqmlogger.trace("\tAdded EXT path {}", file.toURI());
                } catch (MalformedURLException e) {
                    // This should be unreachable because the error was already handled in getJarsInDirectoryRecursive()
                    throw new IllegalStateException(e);
                }
            }

            // Create classloader
            final ModuleFinder moduleFinder = ModuleFinder.of(lPaths.toArray(new Path[0]));
            Set<String> moduleNames = moduleFinder.findAll().stream()
                .map(ModuleReference::descriptor)
                .map(ModuleDescriptor::name)
                .collect(Collectors.toUnmodifiableSet());
            Configuration cf = ModuleLayer.boot().configuration().resolveAndBind(ModuleFinder.of(), moduleFinder, moduleNames);
            moduleLayerInstance = ModuleLayer.boot().defineModulesWithOneLoader(cf, parentCl);
            classLoaderInstance = moduleLayerInstance.modules().isEmpty()
                ? AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>()
                    {
                        @Override
                        public URLClassLoader run()
                        {
                            // Java 8 : parent is default system CL, also called bootstrap CL which has all JDK classes.
                            // Java 9 and above: parent is the platform CL, which holds the same classes.
                            return new URLClassLoader(lUrls.toArray(new URL[0]), parentCl);
                        }
                    })
                : moduleLayerInstance.modules().iterator().next().getClassLoader();
        }
        else
        {
            // No /ext directory means nothing to load.
            classLoaderInstance = parentCl;
        }
    }

    private static List<File> getJarsInDirectoryRecursive(File root)
    {
        List<File> result = new ArrayList<>(10);

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
                    f.toURI().toURL(); // Still filter out files that cause URL problems
                    result.add(f);
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
                File currentJar = new File(ExtClassLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
