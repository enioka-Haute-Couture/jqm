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

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This class implements a basic JNDI context
 * 
 */
public class JndiContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory, NameParser
{
    private static Logger jqmlogger = Logger.getLogger(JndiContext.class);
    private ClassLoader cl = null;
    private Map<String, Object> singletons = new HashMap<String, Object>();

    /**
     * Create a new Context
     * 
     * @param cl
     *            the classloader with access to Hibernate & JQM persistence.xml
     * @throws NamingException
     */
    public JndiContext(ClassLoader cl) throws NamingException
    {
        super();
        this.cl = cl;
    }

    @Override
    public Object lookup(String name) throws NamingException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name cannot be null");
        }
        jqmlogger.debug("Looking up a JNDI element named " + name);

        // If in cache...
        if (singletons.containsKey(name))
        {
            return singletons.get(name);
        }

        // Retrieve the resource description from the database or the XML file
        JndiResourceDescriptor d = ResourceParser.getDescriptor(name);

        // Create the resource
        try
        {
            ResourceFactory rf = new ResourceFactory(d.isSingleton() ? cl : Thread.currentThread().getContextClassLoader());
            Object res = rf.getObjectInstance(d, null, this, new Hashtable<String, Object>());
            if (d.isSingleton())
            {
                singletons.put(name, res);
            }
            return res;
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
}
