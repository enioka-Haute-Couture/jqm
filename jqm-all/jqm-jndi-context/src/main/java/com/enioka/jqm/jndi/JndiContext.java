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

package com.enioka.jqm.jndi;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.registry.Registry;
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

import com.enioka.jqm.cl.ExtClassLoader;
import com.enioka.jqm.runner.java.api.jndi.JavaPayloadClassLoader;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a basic JNDI context, using a class loader seeing only JQM_ROOT/ext or the payload classloader (which includes
 * /ext)
 *
 */
class JndiContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory, NameParser
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JndiContext.class);

    private Map<String, Object> singletons = new HashMap<>();
    private List<ObjectName> jmxNames = new ArrayList<>();
    private Registry r = null;
    private final ClassLoader extResources = ExtClassLoader.classLoaderInstance;
    private final ModuleLayer extLayer = ExtClassLoader.moduleLayerInstance;
    private String serverName = null;

    /**
     * Create a new Context
     *
     * @throws NamingException
     */
    JndiContext() throws NamingException
    {
        super();
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
        // TODO: find a non-hackish way to do give latest server started. (service offered by engine?)
        if (name.endsWith("serverName"))
        {
            return serverName;
        }
        if (name.startsWith("serverName://"))
        {
            serverName = name.split("//")[1];
            return null;
        }
        if (name.startsWith("internal://reset")) // For easier tests, as this is a global singleton hard to wire in plugins...
        {
            resetSingletons();
            return null;
        }
        if (name.equals("cl://ext")) // special case needed for tests, as the ext CL will always be the same (shared between all CLs)
        {
            return this.extResources;
        }
        if (name.equals("layer://ext")) // special case neeed for tests, to retrieve the module layer from the job instance
        {
            return this.extLayer;
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
                    ResourceFactory rf = new ResourceFactory(Thread.currentThread().getContextClassLoader() != null
                            && Thread.currentThread().getContextClassLoader() instanceof JavaPayloadClassLoader
                                    ? Thread.currentThread().getContextClassLoader()
                                    : extResources);
                    res = rf.getObjectInstance(d, null, this, new Hashtable<String, Object>());
                }
                catch (Exception e)
                {
                    jqmlogger.warn("Could not instanciate singleton JNDI object resource " + name, e);
                    NamingException ex = new NamingException(e.getMessage());
                    ex.initCause(e);
                    throw ex;
                }

                // Cache result (if loaded by ext CL or below)
                if (!isLoadedByExtClassloader(res))
                {
                    jqmlogger.warn(
                            "A JNDI resource was defined as singleton but was loaded by a payload class loader - it won't be cached to avoid class loader leaks");
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
                            mbs.registerMBean(res.getClass().getMethod("getPool").invoke(res).getClass().getMethod("getJmxPool")
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
            ResourceFactory rf = new ResourceFactory(Thread.currentThread().getContextClassLoader() != null
                    && Thread.currentThread().getContextClassLoader() instanceof JavaPayloadClassLoader
                            ? Thread.currentThread().getContextClassLoader()
                            : extResources);
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

    public void resetSingletons()
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
        this.jmxNames = new ArrayList<>();
        this.singletons = new HashMap<>();
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

        if (name.equals("rmi://") && obj instanceof Registry)
        {
            jqmlogger.debug("Binding JMX registry inside JNDI directory");
            this.r = (Registry) obj;
            return;
        }

        if (r != null && name.startsWith("rmi://"))
        {
            try
            {
                jqmlogger.debug("Binding [" + name.split("/")[3] + "] to a [" + obj.getClass().getCanonicalName() + "]");
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

    /**
     * A helper - in Java 9, the extension CL was renamed to platform CL and hosts all the JDK classes. Before 9, it was useless and we used
     * bootstrap CL instead.
     *
     * @return the base CL to use.
     */
    private static ClassLoader getParentCl()
    {
        try
        {
            Method m = ClassLoader.class.getMethod("getPlatformClassLoader");
            return (ClassLoader) m.invoke(null);
        }
        catch (NoSuchMethodException e)
        {
            // Java < 9, just use the bootstrap CL.
            return null;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not fetch Platform Class Loader", e);
        }
    }

    private boolean isLoadedByExtClassloader(Object o)
    {
        ClassLoader cl = extResources;
        do
        {
            if (o.getClass().getClassLoader() == cl)
            {
                return true;
            }
            cl = cl.getParent();
        } while (cl != null);
        return o.getClass().getClassLoader() == null; // special case, as null is in many JVMs the root CL.
    }
}
