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

// Thanks Apache Tomcat!

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

/**
 * An {@link ObjectFactory} which delegates all work to the {@link ObjectFactory} described inside a {@link JndiResourceDescriptor}.<br>
 * For all accounts, it is a proxy ObjectFactory.
 */
class ResourceFactory implements ObjectFactory
{
    private ClassLoader clResourceClasses = null;

    ResourceFactory(ClassLoader clResourcesClasses)
    {
        this.clResourceClasses = clResourcesClasses;
    }

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
    {
        // What kind of resource should we create?
        JndiResourceDescriptor resource = (JndiResourceDescriptor) obj;

        // Get class loader, we'll need it to load the factory class
        Class<?> factoryClass = null;
        ObjectFactory factory = null;

        try
        {
            factoryClass = clResourceClasses.loadClass(resource.getFactoryClassName());
        }
        catch (ClassNotFoundException e)
        {
            NamingException ex = new NamingException("Could not find resource or resource factory class in the classpath");
            ex.initCause(e);
            throw ex;
        }
        catch (Exception e)
        {
            NamingException ex = new NamingException("Could not load resource or resource factory class for an unknown reason");
            ex.initCause(e);
            throw ex;
        }

        try
        {
            factory = (ObjectFactory) factoryClass.newInstance();
        }
        catch (Exception e)
        {
            NamingException ex = new NamingException("Could not create resource factory instance");
            ex.initCause(e);
            throw ex;
        }

        Object result = null;
        try
        {
            result = factory.getObjectInstance(obj, name, nameCtx, environment);
        }
        catch (Exception e)
        {
            NamingException ex = new NamingException(
                    "Could not create object resource from resource factory. JNDI definition & parameters may be incorrect.");
            ex.initCause(e);
            throw ex;
        }
        return result;
    }

}
