package com.enioka.jqm.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobInstance;

/**
 * This class holds all the {@link JarClassLoader} and is the only place to create one. There should be one instance per engine.<br>
 * We use a specific object rather than static objects in the {@link Loader} class to allow multiple engine instantiations. It also allows
 * to centralise all CL creation methods and have cleaner code in Loader and in JCL.
 */
class ClassloaderManager
{
    private Logger jqmlogger = Logger.getLogger(ClassloaderManager.class);

    /**
     * The CL corresponding to "one CL to rule them all" mode.
     */
    private JarClassLoader sharedClassLoader = null;

    /**
     * The CLs corresponding to "one CL per jar" mode.
     */
    private Map<String, JarClassLoader> sharedJarClassLoader = new HashMap<String, JarClassLoader>();

    /**
     * The CLs corresponding to specific keys (specified inside {@link JobDef#getSpecificIsolationContext()}).
     */
    private Map<String, JarClassLoader> specificIsolationContextClassLoader = new HashMap<String, JarClassLoader>();

    /**
     * The default CL mode. Values can be: null, Shared, SharedJar.
     */
    private String launchIsolationDefault = null;

    private final LibraryResolverFS fsResolver;
    private final LibraryResolverMaven mavenResolver;

    ClassloaderManager()
    {
        EntityManager em = null;
        try
        {
            em = Helpers.getNewEm();
            launchIsolationDefault = Helpers.getParameter("launch_isolation_default", "Isolated", em);
        }
        catch (Exception e)
        {
            throw new JqmInitError("could not find parameter", e);
        }
        finally
        {
            Helpers.closeQuietly(em);
        }

        this.fsResolver = new LibraryResolverFS();
        this.mavenResolver = new LibraryResolverMaven();
    }

    JarClassLoader getClassloader(JobInstance ji, EntityManager em) throws MalformedURLException, JqmPayloadException, RuntimeException
    {
        final JarClassLoader jobClassLoader;
        JobDef jd = ji.getJd();

        // Extract the jar actual path
        File jarFile = new File(FilenameUtils.concat(new File(ji.getNode().getRepo()).getAbsolutePath(), jd.getJarPath()));

        // The parent class loader is normally the CL with EXT on its CL. But if no lib load, user current one (happens for external
        // pyloads)
        ClassLoader parent = getParentClassLoader(ji);

        // Priority is:
        // 1 - a specific context
        // 2 - general mode (jar or global)

        String specificIsolationContext = jd.getSpecificIsolationContext();
        if (specificIsolationContext != null)
        {
            if (specificIsolationContextClassLoader.containsKey(specificIsolationContext))
            {
                jqmlogger.info("Using specific isolation context : " + specificIsolationContext);
                jobClassLoader = specificIsolationContextClassLoader.get(specificIsolationContext);
                // Checking if the specific class loader configuration is exactly the same as this job definition configuration
                if (jobClassLoader.isChildFirstClassLoader() != jd.isChildFirstClassLoader() ||
                        (jobClassLoader.getHiddenJavaClasses() == null && jd.getHiddenJavaClasses() != null) ||
                        (jobClassLoader.getHiddenJavaClasses() != null && !jobClassLoader.getHiddenJavaClasses().equals(jd.getHiddenJavaClasses()))) {
                    throw new RuntimeException("Specific class loader: " + specificIsolationContext + " for job def ["+ jd.getApplicationName()
                            +"]have different configuration than the first one loaded ["+ jobClassLoader.getReferenceJobDefName()+"]");
                }
            }
            else
            {
                jqmlogger.info("Creating specific isolation context " + specificIsolationContext);
                jobClassLoader = new JarClassLoader(parent);
                jobClassLoader.setReferenceJobDefName(jd.getApplicationName());
                specificIsolationContextClassLoader.put(specificIsolationContext, jobClassLoader);
            }
        }
        else if ("Shared".equals(launchIsolationDefault))
        {
            if (sharedClassLoader != null)
            {
                jqmlogger.info("Using sharedClassLoader");
                jobClassLoader = sharedClassLoader;
            }
            else
            {
                jqmlogger.info("Creating sharedClassLoader");
                jobClassLoader = new JarClassLoader(parent);
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
                jobClassLoader = new JarClassLoader(parent);
                sharedJarClassLoader.put(jd.getJarPath(), jobClassLoader);
            }
        }
        else
        {
            // Standard case: all launches are independent. We create a transient CL.
            jqmlogger.debug("Using an isolated transient CL");
            jobClassLoader = new JarClassLoader(parent);
        }

        // Resolve the libraries and add them to the classpath
        final URL[] classpath = getClasspath(ji, em);

        // Remember to also add the jar file itself... as CL can be shared, there is no telling if it already present or not.
        jobClassLoader.extendUrls(jarFile.toURI().toURL(), classpath);

        // Set ignore classes
        jobClassLoader.setHiddenJavaClasses(jd.getHiddenJavaClasses());

        // Tracing option
        jobClassLoader.setTracing(jd.isClassLoaderTracing());

        // Child first option
        jobClassLoader.setChildFirstClassLoader(jd.isChildFirstClassLoader());

        // Some debug display
        jqmlogger.trace("CL URLs:");
        for (URL url : jobClassLoader.getURLs())
        {
            jqmlogger.trace("       - " + url.toString());
        }

        return jobClassLoader;
    }

    private ClassLoader getExtensionCLassloader()
    {
        ClassLoader extLoader = null;
        try
        {
            extLoader = ((JndiContext) NamingManager.getInitialContext(null)).getExtCl();
        }
        catch (NamingException e)
        {
            jqmlogger.warn("could not find ext directory class loader. No parent classloader will be used", e);
        }
        return extLoader;
    }

    /**
     * Returns all the URL that should be inside the classpath. This includes the jar itself if any.
     * 
     * @throws JqmPayloadException
     */
    private URL[] getClasspath(JobInstance ji, EntityManager em) throws JqmPayloadException
    {
        switch (ji.getJd().getPathType())
        {
        default:
        case FS:
            return fsResolver.getLibraries(ji.getNode(), ji.getJd(), em);
        case MAVEN:
            return mavenResolver.resolve(ji, em);
        case MEMORY:
            return new URL[0];
        }
    }

    private ClassLoader getParentClassLoader(JobInstance ji)
    {
        switch (ji.getJd().getPathType())
        {
        default:
        case FS:
            return getExtensionCLassloader();
        case MAVEN:
            return getExtensionCLassloader();
        case MEMORY:
            return Thread.currentThread().getContextClassLoader();
        }
    }
}
