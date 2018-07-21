/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.model.ClHandler;
import com.enioka.jqm.model.JobInstance;

/**
 * The {@link URLClassLoader} that will load everything related to a payload (the payload jar and all its dependencies).<br>
 * It is also responsible for launching the payload (be it a Runnable, a main function, etc).
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
class JarClassLoader extends URLClassLoader
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JarClassLoader.class);

    private boolean childFirstClassLoader = false;

    private ArrayList<Pattern> hiddenJavaClassesPatterns = new ArrayList<Pattern>();

    private boolean tracing = false;

    private String referenceJobDefName = null;

    private String hiddenJavaClasses = null;

    private boolean mayBeShared = false;

    JarClassLoader(ClassLoader parent)
    {
        super(new URL[0], parent);
    }

    void extendUrls(URL jarUrl, URL[] libs)
    {
        super.addURL(jarUrl);

        if (libs != null)
        {
            for (URL url : libs)
            {
                super.addURL(url);
            }
        }
    }

    /**
     * Everything here can run without the database.
     * 
     * @param job
     *            the JI to run.
     * @param parameters
     *            already resolved runtime parameters
     * @param clm
     *            the CLM having created this CL.
     * @param h
     *            given as parameter because its constructor needs the database.
     * @throws JqmEngineException
     */
    void launchJar(JobInstance job, Map<String, String> parameters, ClassloaderManager clm, JobManagerHandler h) throws JqmEngineException
    {
        // 1 - Create the proxy.
        Object proxy = null;
        Class injInt;
        try
        {
            injInt = this.loadClass("com.enioka.jqm.api.JobManager");
            proxy = Proxy.newProxyInstance(this, new Class[] { injInt }, h);
        }
        catch (Exception e)
        {
            throw new JqmEngineException("could not create proxy API object", e);
        }

        // 2 - Meta data used by runners
        Map<String, String> metaprms = new HashMap<String, String>();
        metaprms.put("mayBeShared", "" + this.mayBeShared);

        // 3 - Load the target class inside the context class loader
        String classQualifiedName = job.getJD().getJavaClassName();
        jqmlogger.debug("Will now load class: " + classQualifiedName);

        Class c = null;
        try
        {
            // using payload CL, i.e. this very object
            c = loadClass(classQualifiedName);
        }
        catch (Exception e)
        {
            throw new JqmEngineException("could not load class " + classQualifiedName, e);
        }
        jqmlogger.trace("Class " + classQualifiedName + " was correctly loaded");

        // 4 - Determine which job runner should take the job and launch!
        List<String> allowedRunners = clm.getJobRunnerClasses();
        if (job.getJD().getClassLoader() != null && job.getJD().getClassLoader().getAllowedRunners() != null
                && !job.getJD().getClassLoader().getAllowedRunners().isEmpty())
        {
            allowedRunners = Arrays.asList(job.getJD().getClassLoader().getAllowedRunners().split(","));
        }
        for (String runnerClassName : allowedRunners)
        {
            Boolean canRun = false;
            Class runnerClass = null;
            Method run;
            Object runner;
            try
            {
                // Note we load the runner class inside the engine CL (with plugins), not the payload CL.
                // NOTHING is allowed inside the payload CL which was not specifically asked for. (ext dir or lib dir)
                runnerClass = clm.getPluginClassLoader().loadClass(runnerClassName);
            }
            catch (Exception e)
            {
                throw new JqmEngineException(
                        "could not load a runner: check your global parameters, or that the plugin for this runner is actually present "
                                + runnerClassName,
                        e);
            }
            try
            {
                runner = runnerClass.newInstance();
            }
            catch (Exception e)
            {
                throw new JqmEngineException(
                        "could not create an instance of a runner: it may not have a no-args constructor. " + runnerClassName, e);
            }

            try
            {
                Method m = runnerClass.getMethod("canRun", Class.class);
                canRun = (Boolean) m.invoke(runner, c);
            }
            catch (Exception e)
            {
                throw new JqmEngineException("invocation of canRun failed on the runner plugin " + runnerClassName, e);
            }

            if (canRun)
            {
                jqmlogger.trace("Payload is of type: " + runnerClassName);

                try
                {
                    run = runnerClass.getMethod("run", Class.class, Map.class, Map.class, Object.class);
                }
                catch (Exception e)
                {
                    throw new JqmEngineException("could not find run method for runner plugin " + runnerClassName, e);
                }

                // We are ready to actually run the job instance. Time for all event handlers.
                if (job.getJD().getClassLoader() != null)
                {
                    for (ClHandler handler : job.getJD().getClassLoader().getHandlers())
                    {
                        String handlerClass = handler.getClassName();
                        Map<String, String> handlerPrms = new HashMap<String, String>();
                        for (Map.Entry<String, String> hprm : handler.getParameters().entrySet())
                        {
                            handlerPrms.put(hprm.getKey(), hprm.getValue());
                        }

                        try
                        {
                            Method handlerRun = loadClass(handlerClass).getMethod("run", Class.class, injInt, Map.class);
                            Object handlerInstance = loadClass(handlerClass).newInstance();
                            handlerRun.invoke(handlerInstance, c, proxy, handlerPrms);
                        }
                        catch (Exception e)
                        {
                            throw new JqmEngineException("event handler could not be loaded or run: " + handlerClass, e);
                        }
                    }
                }

                // Go for real.
                try
                {
                    run.invoke(runner, c, metaprms, parameters, proxy);
                    return;
                }
                catch (InvocationTargetException e)
                {
                    if (e.getCause() instanceof RuntimeException)
                    {
                        // it may be a Kill order, or whatever exception...
                        throw (RuntimeException) e.getCause();
                    }
                    else
                    {
                        throw new JqmEngineException("Payload has failed", e);
                    }
                }
                catch (Exception e)
                {
                    throw new JqmEngineException("Could not launch a job instance (engine issue, not a payload issue", e);
                }
            }
        }

        throw new JqmEngineException(
                "This type of class cannot be launched by JQM. Please consult the documentation for more details. Available runners: "
                        + allowedRunners);
    }

    private Class<?> loadFromParentCL(String name) throws ClassNotFoundException
    {
        for (Pattern pattern : hiddenJavaClassesPatterns)
        {
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches())
            {
                jqmlogger.debug("Class " + name + " will not be loaded by parent CL because it matches hiddenJavaClasses parameter");
                // Invoke findClass in order to find the class.
                return super.findClass(name);
            }
        }
        return loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        Class<?> c = null;

        if (tracing)
        {
            jqmlogger.debug("Loading : " + name);
        }

        if (childFirstClassLoader)
        {
            // Check if class was already loaded
            c = findLoadedClass(name);

            if (c == null)
            {
                // Try to find class from URLClassLoader
                try
                {
                    c = super.findClass(name);
                }
                catch (ClassNotFoundException e)
                {
                    //
                }
                // If nothing was found, try parent class loader
                if (c == null)
                {
                    // jqmlogger.trace("found in parent " + name);
                    c = loadFromParentCL(name);
                }
                else
                {
                    // jqmlogger.trace("found in child " + name);
                }
            }
            else
            {
                // jqmlogger.trace("name already loaded");
            }
        }
        else
        {
            // Default behavior
            c = loadFromParentCL(name);
        }

        return c;
    }

    public boolean isChildFirstClassLoader()
    {
        return childFirstClassLoader;
    }

    public void setChildFirstClassLoader(boolean childFirstClassLoader)
    {
        this.childFirstClassLoader = childFirstClassLoader;
    }

    public ArrayList<Pattern> gethiddenJavaClassesPatterns()
    {
        return hiddenJavaClassesPatterns;
    }

    public void setHiddenJavaClasses(String hiddenJavaClasses)
    {
        // Save String for quick comparaison
        this.hiddenJavaClasses = hiddenJavaClasses;

        if (hiddenJavaClasses == null)
        {
            return;
        }
        // Add hidden java classes regex patterns to CL
        for (String regex : hiddenJavaClasses.split(","))
        {
            jqmlogger.debug("Adding " + regex + " hiddenJavaClasses regex to CL");
            this.addHiddenJavaClassesPattern(Pattern.compile(regex));
        }
    }

    private void addHiddenJavaClassesPattern(Pattern hiddenJavaClassesPattern)
    {
        this.hiddenJavaClassesPatterns.add(hiddenJavaClassesPattern);
    }

    public boolean isTracing()
    {
        return tracing;
    }

    public void setTracing(boolean tracing)
    {
        this.tracing = tracing;
    }

    public void setReferenceJobDefName(String referenceJobDefName)
    {
        this.referenceJobDefName = referenceJobDefName;
    }

    public String getReferenceJobDefName()
    {
        return referenceJobDefName;
    }

    public String getHiddenJavaClasses()
    {
        return hiddenJavaClasses;
    }

    void mayBeShared(boolean val)
    {
        this.mayBeShared = val;
    }

    /**
     * Hack - in Java 7, CL.Close was introduced but is not present in earlier versions. Yet it is highly useful on Windows as it frees file
     * handlers.<br>
     * Shared class loaders are left open.
     */
    void tryClose()
    {
        if (!mayBeShared)
        {
            // First: free the hounds, er, the CL leak hunter
            ClassLoaderLeakCleaner.clean(this);
            ClassLoaderLeakCleaner.cleanJdbc(Thread.currentThread());

            // Then try to call CL.close()
            Method m = null;
            try
            {
                m = this.getClass().getMethod("close");
            }
            catch (NoSuchMethodException e)
            {
                jqmlogger.trace("CL cannot be closed");
                return;
            }
            catch (SecurityException e)
            {
                jqmlogger.error("Cannot access CL.close", e);
                return;
            }

            try
            {
                m.invoke(this);
            }
            catch (Exception e)
            {
                jqmlogger.error("Cannot close CL", e);
                return;
            }
            jqmlogger.debug("CL was closed");
        }
    }
}
