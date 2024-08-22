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

import java.util.HashMap;
import java.util.Map;
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

    private boolean alreadyRun = false;

    public JqmRestApp()
    {
        log.debug("Starting REST WS app");
    }

    @Override
    public Set<Class<?>> getClasses()
    {
        if (alreadyRun)
        {
            return null;
        }
        alreadyRun = true;

        log.debug("\tStarting REST WS app class configuration");

        // Log service status
        logServiceStatus(ServiceSimple.class, "startSimple");
        logServiceStatus(ServiceClient.class, "startClient");
        logServiceStatus(ServiceAdmin.class, "startAdmin");

        // Done
        return null;
    }

    @Override
    public Map<String, Object> getProperties()
    {
        var res = new HashMap<String, Object>();
        // We do not need the descriptor, and a compatible JAXB context is not provided in the JQM server.
        res.put("jersey.config.server.wadl.disableWadl", "true");
        return res;
    }

    private void logServiceStatus(Class<?> service, String paramName)
    {
        if (this.context != null && this.context.getInitParameter(paramName) != null)
        {
            var start = Boolean.parseBoolean(this.context.getInitParameter(paramName));
            if (start)
            {
                log.info("\t\tService {} enabled", service.getName());
            }
            else
            {
                log.info("\t\tService {} is not configured to start", service.getName());
            }
        }
    }
}
