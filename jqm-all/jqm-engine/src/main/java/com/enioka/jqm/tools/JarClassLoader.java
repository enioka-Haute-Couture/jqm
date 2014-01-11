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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.JobInstance;

@SuppressWarnings({ "unchecked", "rawtypes" })
class JarClassLoader extends URLClassLoader
{
    private static Logger jqmlogger = Logger.getLogger(JarClassLoader.class);

    private static URL[] addUrls(URL url, URL[] libs)
    {
        URL[] urls = new URL[libs.length + 1];
        urls[0] = url;
        for (int i = 0; i < libs.length; i++)
        {
            urls[i + 1] = libs[i];
        }
        return urls;
    }

    JarClassLoader(URL url, URL[] libs)
    {
        super(addUrls(url, libs), null);
    }

    Object launchJar(JobInstance job, String defaultConnection, ClassLoader old, EntityManager em) throws Exception
    {
        // 1st:load the class
        String classQualifiedName = job.getJd().getJavaClassName();
        jqmlogger.debug("Will now load class: " + classQualifiedName);

        Class c = null;
        try
        {
            c = loadClass(classQualifiedName);
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not load class", e);
            throw e;
        }
        jqmlogger.debug("Class " + classQualifiedName + " was correctly loaded");

        // 2nd: what type of payload is this?
        if (Runnable.class.isAssignableFrom(c))
        {
            jqmlogger.info("This payload is of type: Runnable");
            return launchRunnable(c, job, defaultConnection, em);
        }
        else if (c.getSuperclass().getName().equals("com.enioka.jqm.api.JobBase"))
        {
            jqmlogger.info("This payload is of type: explicit API implementation");
            return launchApiPayload(c, job, defaultConnection, em);
        }

        throw new JqmEngineException("This type of class cannot be launched by JQM. Please consult the documentation for more details.");
    }

    private Object launchApiPayload(Class c, JobInstance job, String defaultConnection, EntityManager em) throws JqmEngineException
    {
        Object o = null;
        try
        {
            o = c.newInstance();
        }
        catch (Exception e)
        {
            jqmlogger.error("Cannot create an instance of class " + c.getCanonicalName() + ". Does it have an argumentless constructor?");
            throw new JqmEngineException("Could not create payload instance", e);
        }

        // Injection
        inject(o, job, em);

        try
        {
            // Start method that we will have to call
            Method start = c.getMethod("start");
            start.invoke(o);
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
        catch (NoSuchMethodException e)
        {
            throw new JqmEngineException("Payload " + c.getCanonicalName() + " is incorrect - missing fields and methods.", e);
        }
        catch (Exception e)
        {
            throw new JqmEngineException("Payload launch failed for " + c.getCanonicalName() + ".", e);
        }

        return o;
    }

    private Object launchRunnable(Class<Runnable> c, JobInstance job, String defaultConnection, EntityManager em) throws JqmEngineException
    {
        Runnable o = null;
        try
        {
            o = c.newInstance();
        }
        catch (Exception e)
        {
            throw new JqmEngineException("Could not instanciate runnable payload. Does it have a nullary constructor?", e);
        }

        // Injection stuff (if needed)
        inject(o, job, em);

        // Go
        o.run();

        return o;
    }

    private void inject(Object o, JobInstance job, EntityManager em) throws JqmEngineException
    {
        Class c = o.getClass();
        List<Field> ff = new ArrayList<Field>();
        ff.addAll(Arrays.asList(c.getDeclaredFields()));
        ff.addAll(Arrays.asList(c.getSuperclass().getDeclaredFields()));
        boolean inject = false;
        for (Field f : ff)
        {
            if (f.getType().getName().equals("com.enioka.jqm.api.JobManager"))
            {
                jqmlogger.debug("The object should be injected at least on field " + f.getName());
                inject = true;
                break;
            }
        }
        if (!inject)
        {
            jqmlogger.debug("This object has no fields available for injection. No injection will take place.");
            return;
        }

        JobManagerHandler h = new JobManagerHandler(job, em);
        Class injInt = null;
        Object proxy = null;
        try
        {
            injInt = loadClass("com.enioka.jqm.api.JobManager");
            proxy = Proxy.newProxyInstance(this, new Class[] { injInt }, h);
        }
        catch (Exception e)
        {
            throw new JqmEngineException("Could not load JQM internal interface", e);
        }
        try
        {
            for (Field f : ff)
            {
                if (f.getType().equals(injInt))
                {
                    jqmlogger.debug("Injecting interface JQM into field " + f.getName());
                    boolean acc = f.isAccessible();
                    f.setAccessible(true);
                    f.set(o, proxy);
                    f.setAccessible(acc);
                }
            }
        }
        catch (Exception e)
        {
            throw new JqmEngineException("Could not inject JQM interface into taget payload", e);
        }
    }

}
