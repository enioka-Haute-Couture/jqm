/**
 * Copyright © 2013 enioka. All rights reserved
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
package com.enioka.jqm.ws.plumbing;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.ws.api.ServiceAdmin;
import com.enioka.jqm.ws.api.ServiceClient;
import com.enioka.jqm.ws.api.ServiceSimple;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;

/**
 * A JAX-RS application registering all JQM JAX-RS resources.
 */
@ApplicationPath("/ws/*")
public class JqmRestApp extends Application
{
    static Logger log = LoggerFactory.getLogger(JqmRestApp.class);

    @Context
    public ServletContext context;

    @Override
    public Set<Class<?>> getClasses()
    {
        // TODO: most of these should work with simple annotation scanning.
        log.debug("\tStarting REST WS app class configuration");
        HashSet<Class<?>> res = new HashSet<>();

        // Load the exception mappers
        res.add(ErrorHandler.class);
        res.add(JqmExceptionMapper.class);
        res.add(JqmInternalExceptionMapper.class);

        // Logger
        res.add(ExceptionLogger.class);

        // Load the cache annotation helper
        res.add(HttpCacheImpl.class);

        // Load the actual services
        startService(ServiceSimple.class, "startSimple", res, false);
        startService(ServiceClient.class, "startClient", res, true);
        startService(ServiceAdmin.class, "startAdmin", res, true);

        // Done
        return res;
    }

    public JqmRestApp()
    {
        log.debug("Starting REST WS app");
    }

    private void startService(Class<?> service, String paramName, HashSet<Class<?>> res, boolean defaultStart)
    {
        if (this.context != null && this.context.getInitParameter(paramName) != null)
        {
            var start = Boolean.parseBoolean(this.context.getInitParameter(paramName));
            if (start)
            {
                log.debug("\t\tRegistering service {}", service.getName());
                res.add(service);
            }
            else
            {
                log.debug("\t\tService {} is not configured to start", service.getName());
            }
        }
        else if (defaultStart)
        {
            // No parameter = deployment in a normal servlet container.
            log.debug("\t\tRegistering service {} (standard war deployment)", service.getName());
            res.add(service);
        }
        else
        {
            log.debug("\t\tService {} is not configured to start in a standard web deployment", service.getName());
        }
    }
}
