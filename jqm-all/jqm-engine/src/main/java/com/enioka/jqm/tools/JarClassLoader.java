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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.JobInstance;

/**
 * The {@link URLClassLoader} that will load everything related to a payload (the payload jar and all its dependencies).<br>
 * It is also responsible for launching the payload (be it a Runnable, a main function, etc).
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
class JarClassLoader extends URLClassLoader
{
    private static Logger jqmlogger = Logger.getLogger(JarClassLoader.class);

    private Map<String, String> prms = new HashMap<String, String>();

    private boolean childFirstClassLoader = false;

    private ArrayList<Pattern> hiddenJavaClassesPatterns = new ArrayList<Pattern>();

    private boolean tracing = false;

    private String referenceJobDefName = null;

    private String hiddenJavaClasses = null;

    private boolean mayBeShared = false;

    private static URL[] addUrls(URL url, URL[] libs)
    {
        URL[] urls = new URL[libs.length + 1];
        urls[0] = url;
        System.arraycopy(libs, 0, urls, 1, libs.length);
        return urls;
    }

    JarClassLoader(URL url, URL[] libs, ClassLoader parent)
    {
        super(addUrls(url, libs), parent);
    }

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

    void launchJar(JobInstance job, Map<String, String> parameters, ClassloaderManager clm) throws JqmEngineException
    {
        this.prms = parameters;

        // 1 - Create the proxy.
        Object proxy = null;
        try
        {
            JobManagerHandler h = new JobManagerHandler(job, prms);
            Class injInt = this.getParent().loadClass("com.enioka.jqm.api.JobManager");
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
        String classQualifiedName = job.getJd().getJavaClassName();
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
        for (String runnerClassName : clm.getJobRunnerClasses())
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
                        "could not load a runner: check you global parameters, or that the plugin for this runner is actually present "
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
                    // run(Class<? extends Object> toRun, Map<String, String> metaParameters, Map<String, String> jobParameters, Object
                    // handlerProxy)
                    run = runnerClass.getMethod("run", Class.class, Map.class, Map.class, Object.class);
                }
                catch (Exception e)
                {
                    throw new JqmEngineException("could not find run method for runner plugin " + runnerClassName, e);
                }

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

        throw new JqmEngineException("This type of class cannot be launched by JQM. Please consult the documentation for more details.");
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
}
