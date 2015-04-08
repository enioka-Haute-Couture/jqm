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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This class implements a basic JNDI context
 * 
 */
class JndiContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory, NameParser
{
    private static Logger jqmlogger = Logger.getLogger(JndiContext.class);

    private Map<String, Object> singletons = new HashMap<String, Object>();
    private List<ObjectName> jmxNames = new ArrayList<ObjectName>();
    private Registry r = null;
    private ClassLoader extResources;

    /**
     * Will create a JNDI Context and register it as the initial context factory builder
     * 
     * @return the context
     * @throws NamingException
     *             on any issue during initial context factory builder registration
     */
    static JndiContext createJndiContext() throws NamingException
    {
        try
        {
            if (!NamingManager.hasInitialContextFactoryBuilder())
            {
                JndiContext ctx = new JndiContext();
                NamingManager.setInitialContextFactoryBuilder(ctx);
                return ctx;
            }
            else
            {
                return (JndiContext) NamingManager.getInitialContext(null);
            }
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not create JNDI context: " + e.getMessage());
            NamingException ex = new NamingException("Could not initialize JNDI Context");
            ex.setRootCause(e);
            throw ex;
        }
    }

    /**
     * Create a new Context
     * 
     * @throws NamingException
     */
    private JndiContext() throws NamingException
    {
        super();

        // List all jars inside ext directory
        File extDir = new File("ext/");
        List<URL> urls = new ArrayList<URL>();
        if (extDir.isDirectory())
        {
            for (File f : extDir.listFiles())
            {
                if (!f.canRead())
                {
                    throw new NamingException("can't access file " + f.getAbsolutePath());
                }
                try
                {
                    urls.add(f.toURI().toURL());
                }
                catch (MalformedURLException e)
                {
                    jqmlogger.error("Error when parsing the content of ext directory. File will be ignored", e);
                }
            }

            // Create classloader
            final URL[] aUrls = urls.toArray(new URL[0]);
            for (URL u : aUrls)
            {
                jqmlogger.trace(u.toString());
            }
            extResources = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>()
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
            throw new NamingException("JQM_ROOT/ext directory does not exist or cannot be read");
        }
    }

    @Override
    public Object lookup(String name) throws NamingException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name cannot be null");
        }
        jqmlogger.trace("Looking up a JNDI element named " + name);

        // Special delegated cases
        if (name.startsWith("rmi:"))
        {
            try
            {
                return this.r.lookup(name.split("/")[3]);
            }
            catch (Exception e)
            {
                NamingException e1 = new NamingException();
                e1.setRootCause(e);
                throw e1;
            }
        }
        if (name.endsWith("serverName"))
        {
            return JqmEngine.latestNodeStartedName;
        }

        // If in cache...
        if (singletons.containsKey(name))
        {
            jqmlogger.trace("JNDI element named " + name + " found in cache.");
            return singletons.get(name);
        }

        // Retrieve the resource description from the database or the XML file
        JndiResourceDescriptor d = ResourceParser.getDescriptor(name);
        jqmlogger.trace("JNDI element named " + name + " not found in cache. Will be created. Singleton status: " + d.isSingleton());

        // Singleton handling is synchronized to avoid double creation
        if (d.isSingleton())
        {
            synchronized (singletons)
            {
                if (singletons.containsKey(name))
                {
                    return singletons.get(name);
                }

                // We use the current thread loader to find the resource and resource factory class - ext is inside that CL.
                // This is done only for payload CL - engine only need ext, not its own CL (as its own CL does NOT include ext).
                Object res = null;
                try
                {
                    ResourceFactory rf = new ResourceFactory(
                            Thread.currentThread().getContextClassLoader() instanceof com.enioka.jqm.tools.JarClassLoader ? Thread
                                    .currentThread().getContextClassLoader() : extResources);
                    res = rf.getObjectInstance(d, null, this, new Hashtable<String, Object>());
                }
                catch (Exception e)
                {
                    jqmlogger.warn("Could not instanciate singleton JNDI object resource " + name, e);
                    NamingException ex = new NamingException(e.getMessage());
                    ex.initCause(e);
                    throw ex;
                }

                // Cache result
                if (res.getClass().getClassLoader() instanceof JarClassLoader)
                {
                    jqmlogger
                            .warn("A JNDI resource was defined as singleton but was loaded by a payload class loader - it won't be cached to avoid class loader leaks");
                }
                else
                {
                    singletons.put(name, res);

                    // Pool JMX registration (only if cached - avoids leaks)
                    if ("org.apache.tomcat.jdbc.pool.DataSourceFactory".equals(d.getFactoryClassName())
                            && (d.get("jmxEnabled") == null ? true : Boolean.parseBoolean((String) d.get("jmxEnabled").getContent())))
                    {
                        try
                        {
                            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                            ObjectName jmxname = new ObjectName("com.enioka.jqm:type=JdbcPool,name=" + name);
                            mbs.registerMBean(
                                    res.getClass().getMethod("getPool").invoke(res).getClass().getMethod("getJmxPool")
                                            .invoke(res.getClass().getMethod("getPool").invoke(res)), jmxname);
                            jmxNames.add(jmxname);
                        }
                        catch (Exception e)
                        {
                            jqmlogger.warn("Could not register JMX MBean for resource.", e);
                        }
                    }
                }

                // Done
                return res;
            }
        }

        // Non singleton
        try
        {
            // We use the current thread loader to find the resource and resource factory class - ext is inside that CL.
            // This is done only for payload CL - engine only need ext, not its own CL (as its own CL does NOT include ext).
            ResourceFactory rf = new ResourceFactory(
                    Thread.currentThread().getContextClassLoader() instanceof com.enioka.jqm.tools.JarClassLoader ? Thread.currentThread()
                            .getContextClassLoader() : extResources);
            return rf.getObjectInstance(d, null, this, new Hashtable<String, Object>());
        }
        catch (Exception e)
        {
            jqmlogger.warn("Could not instanciate JNDI object resource " + name, e);
            NamingException ex = new NamingException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    void resetSingletons()
    {
        jqmlogger.info("Resetting singleton JNDI resource cache");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        for (ObjectName n : this.jmxNames)
        {
            try
            {
                mbs.unregisterMBean(n);
            }
            catch (Exception e)
            {
                jqmlogger.error("could not unregister bean", e);
            }
        }
        this.jmxNames = new ArrayList<ObjectName>();
        this.singletons = new HashMap<String, Object>();
    }

    @Override
    public Object lookup(Name name) throws NamingException
    {
        return this.lookup(StringUtils.join(Collections.list(name.getAll()), "/"));
    }

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
    {
        return this;
    }

    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException
    {
        return this;
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException
    {
        return this;
    }

    @Override
    public Name parse(String name) throws NamingException
    {
        return new CompositeName(name);
    }

    @Override
    public void close() throws NamingException
    {
        // Nothing to do.
    }

    @Override
    public void bind(String name, Object obj) throws NamingException
    {
        jqmlogger.debug("binding [" + name + "] to a [" + obj.getClass().getCanonicalName() + "]");
        if (r != null && name.startsWith("rmi://"))
        {
            try
            {
                jqmlogger.debug("binding [" + name.split("/")[3] + "] to a [" + obj.getClass().getCanonicalName() + "]");
                this.r.bind(name.split("/")[3], (Remote) obj);
            }
            catch (Exception e)
            {
                NamingException e1 = new NamingException("could not bind RMI object");
                e1.setRootCause(e);
                throw e1;
            }
        }
        else
        {
            this.singletons.put(name, obj);
        }
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException
    {
        this.bind(StringUtils.join(Collections.list(name.getAll()), "/"), obj);
    }

    /**
     * Will register the given Registry as a provider for the RMI: context. If there is already a registered Registry, the call is ignored.
     * 
     * @param r
     */
    void registerRmiContext(Registry r)
    {
        if (this.r == null)
        {
            this.r = r;
        }
    }

    /**
     * @return the class loader holding the ext directory (or null if no ext directory - should never happen)
     */
    ClassLoader getExtCl()
    {
        return this.extResources;
    }

    @Override
    public void unbind(Name name) throws NamingException
    {
        this.unbind(StringUtils.join(Collections.list(name.getAll()), "/"));
    }

    @Override
    public void unbind(String name) throws NamingException
    {
        if (r != null && name.startsWith("rmi://"))
        {
            try
            {
                jqmlogger.debug("unbinding RMI name " + name);
                this.r.unbind(name.split("/")[3]);
            }
            catch (Exception e)
            {
                NamingException e1 = new NamingException("could not unbind RMI name");
                e1.setRootCause(e);
                throw e1;
            }
        }
        else
        {
            this.singletons.remove(name);
        }
    }
}