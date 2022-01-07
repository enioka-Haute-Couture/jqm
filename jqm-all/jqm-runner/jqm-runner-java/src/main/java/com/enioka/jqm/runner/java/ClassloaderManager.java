package com.enioka.jqm.runner.java;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.Cl;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.runner.api.JobRunnerCallback;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds all the {@link PayloadClassLoader} and is the only place to create one. There should be one instance per engine.<br>
 * We use a specific object rather than static objects in the {@link JavaJobInstanceTracker} class to allow multiple engine instantiations.
 * It also allows to centralise all CL creation methods and have cleaner code in Loader and in JCL.
 */
public class ClassloaderManager
{
    private Logger jqmlogger = LoggerFactory.getLogger(ClassloaderManager.class);

    /**
     * The CL corresponding to "one CL to rule them all" mode.
     */
    private PayloadClassLoader sharedClassLoader = null;

    /**
     * The CL for loading plugins (for the engine)
     */
    private ClassLoader pluginClassLoader = null;
    private boolean hasPlugins = true;

    /**
     * The CLs corresponding to "one CL per jar" mode.
     */
    private Map<String, PayloadClassLoader> sharedJarClassLoader = new HashMap<>();

    /**
     * The CLs corresponding to specific keys (specified inside {@link JobDef#getSpecificIsolationContext()}). Key is Cl object ID.
     */
    private Map<Integer, PayloadClassLoader> persistentClassLoaders = new HashMap<>();

    /**
     * The different runners which may be involved inside the class loaders. Simple class names.
     */
    private List<String> runnerClasses = new ArrayList<>();

    /**
     * The default CL mode. Values can be: null, Shared, SharedJar.
     */
    private String launchIsolationDefault = null;

    private final LibraryResolverFS fsResolver;
    private final LibraryResolverMaven mavenResolver;

    public ClassloaderManager(DbConn cnx)
    {
        this.mavenResolver = new LibraryResolverMaven(cnx);
        this.fsResolver = new LibraryResolverFS(this.mavenResolver);

        setIsolationDefault(cnx);
    }

    private void setIsolationDefault(DbConn cnx)
    {
        this.launchIsolationDefault = GlobalParameter.getParameter(cnx, "launch_isolation_default", "Isolated");
        String rns = GlobalParameter.getParameter(cnx, "job_runners",
                "com.enioka.jqm.runner.java.LegacyRunner,com.enioka.jqm.runner.java.MainRunner,com.enioka.jqm.runner.java.RunnableRunner");
        for (String s : rns.split(","))
        {
            runnerClasses.add(s);
            jqmlogger.info("Detected a job instance runner named " + s);
        }
    }

    public PayloadClassLoader getClassloader(JobInstance ji, JobRunnerCallback cb)
            throws MalformedURLException, JqmPayloadException, RuntimeException
    {
        final PayloadClassLoader jobClassLoader;
        JobDef jd = ji.getJD();

        // Extract the jar actual path
        File jarFile = new File(FilenameUtils.concat(new File(ji.getNode().getRepo()).getAbsolutePath(), jd.getJarPath()));

        // The parent class loader is normally the CL with EXT on its CL. But if no lib load, user current one (happens for external
        // payloads)
        ClassLoader parent = getParentClassLoader(ji, cb);

        // Priority is:
        // 1 - a specific context
        // 2 - general mode (jar or global)

        Cl cldef = jd.getClassLoader();

        if (cldef != null)
        {
            ////////////////////////////////////
            // Specific CL options were given
            String clSharingKey = cldef.getName();

            // We take great care to reduce locking as much as possible, so this code may seem to be too complicated at first glance.
            if (cldef.isPersistent())
            {
                if (persistentClassLoaders.containsKey(cldef.getId()))
                {
                    jqmlogger.info("Using an existing specific isolation context : " + clSharingKey);
                    jobClassLoader = persistentClassLoaders.get(cldef.getId());
                }
                else
                {
                    synchronized (persistentClassLoaders)
                    {
                        if (persistentClassLoaders.containsKey(cldef.getId()))
                        {
                            jqmlogger.info("Using an existing specific isolation context : " + clSharingKey);
                            jobClassLoader = persistentClassLoaders.get(cldef.getId());
                        }
                        else
                        {
                            jqmlogger.info("Creating a new persistent specific isolation context: " + clSharingKey);
                            jobClassLoader = new PayloadClassLoader(parent);
                            jobClassLoader.setReferenceJobDefName(jd.getApplicationName());
                            jobClassLoader.mayBeShared(cldef.isPersistent());
                            jobClassLoader.setHiddenJavaClasses(cldef.getHiddenClasses());
                            jobClassLoader.setTracing(cldef.isTracingEnabled());
                            jobClassLoader.setChildFirstClassLoader(cldef.isChildFirst());

                            persistentClassLoaders.put(cldef.getId(), jobClassLoader);
                        }
                    }
                }
            }
            else
            {
                jqmlogger.info("Creating a new transient specific isolation context: " + clSharingKey);
                jobClassLoader = new PayloadClassLoader(parent);
                jobClassLoader.setReferenceJobDefName(jd.getApplicationName());
                jobClassLoader.mayBeShared(cldef.isPersistent());
                jobClassLoader.setHiddenJavaClasses(cldef.getHiddenClasses());
                jobClassLoader.setTracing(cldef.isTracingEnabled());
                jobClassLoader.setChildFirstClassLoader(cldef.isChildFirst());
            }
            // END SPECIFIC CL OPTIONS
            ////////////////////////////////////
        }
        else
        {
            // Use default CL options. We accept double creation on edge sync states in this case.
            if ("Shared".equals(launchIsolationDefault))
            {
                if (sharedClassLoader != null)
                {
                    jqmlogger.info("Using sharedClassLoader");
                    jobClassLoader = sharedClassLoader;
                }
                else
                {
                    jqmlogger.info("Creating sharedClassLoader");
                    jobClassLoader = new PayloadClassLoader(parent);
                    jobClassLoader.mayBeShared(true);
                    sharedClassLoader = jobClassLoader;
                }
            }
            else if ("SharedJar".equals(launchIsolationDefault))
            {
                if (sharedJarClassLoader.containsKey(jd.getJarPath()))
                {
                    // check if jarUrl has already a class loader
                    jqmlogger.info("Using shared Jar CL");
                    jobClassLoader = sharedJarClassLoader.get(jd.getJarPath());
                }
                else
                {
                    jqmlogger.info("Creating shared Jar CL");
                    jobClassLoader = new PayloadClassLoader(parent);
                    jobClassLoader.mayBeShared(true);
                    sharedJarClassLoader.put(jd.getJarPath(), jobClassLoader);
                }
            }
            else
            {
                // Standard case: all launches are independent. We create a transient CL.
                jqmlogger.debug("Using an isolated transient CL with default parameters");
                jobClassLoader = new PayloadClassLoader(parent);
                jobClassLoader.mayBeShared(false);
            }
        }

        // Resolve the libraries and add them to the classpath
        final URL[] classpath = getClasspath(ji, cb);

        // Remember to also add the jar file itself... as CL can be shared, there is no telling if it already present or not.
        jobClassLoader.extendUrls(jarFile.toURI().toURL(), classpath);

        // Some debug display
        jqmlogger.trace("CL URLs:");
        for (URL url : jobClassLoader.getURLs())
        {
            jqmlogger.trace("       - " + url.toString());
        }

        return jobClassLoader;
    }

    /**
     * Returns all the URL that should be inside the classpath. This includes the jar itself if any.
     *
     * @throws JqmPayloadException
     */
    private URL[] getClasspath(JobInstance ji, JobRunnerCallback cb) throws JqmPayloadException
    {
        switch (ji.getJD().getPathType())
        {
        case MAVEN:
            return mavenResolver.resolve(ji);
        case MEMORY:
            return new URL[0];
        case FS:
        default:
            return fsResolver.getLibraries(ji.getNode(), ji.getJD());
        }
    }

    private ClassLoader getParentClassLoader(JobInstance ji, JobRunnerCallback cb)
    {
        switch (ji.getJD().getPathType())
        {
        case MAVEN:
            return cb.getExtensionClassloader();
        case MEMORY:
            return ClassLoader.getSystemClassLoader();
        default:
        case FS:
            return cb.getExtensionClassloader();
        }
    }

    List<String> getJobRunnerClasses()
    {
        return this.runnerClasses;
    }

    ClassLoader getPluginClassLoader()
    {
        if (hasPlugins && pluginClassLoader == null)
        {
            File extDir = new File("plugins/");
            List<URL> urls = new ArrayList<>();
            if (extDir.isDirectory())
            {
                for (File f : extDir.listFiles())
                {
                    if (!f.canRead())
                    {
                        throw new RuntimeException("can't access file " + f.getAbsolutePath());
                    }
                    try
                    {
                        urls.add(f.toURI().toURL());
                    }
                    catch (MalformedURLException e)
                    {
                        jqmlogger.error("Error when parsing the content of plugin directory. File will be ignored", e);
                    }
                }

                // Create classloader
                final URL[] aUrls = urls.toArray(new URL[0]);
                for (URL u : aUrls)
                {
                    jqmlogger.trace(u.toString());
                }
                pluginClassLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>()
                {
                    @Override
                    public URLClassLoader run()
                    {
                        return new URLClassLoader(aUrls, null);
                    }
                });
            }
            else
            {
                hasPlugins = false;
                pluginClassLoader = ClassloaderManager.class.getClassLoader();
            }
        }
        return pluginClassLoader;
    }
}
