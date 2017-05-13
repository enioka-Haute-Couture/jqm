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
package com.enioka.jqm.api;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.glassfish.jersey.server.ResourceConfig;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;

@ApplicationPath("/ws/*")
public class JqmRestApp extends ResourceConfig
{
    public JqmRestApp(@Context ServletContext context)
    {
        // These two properties ensure lists are properly named in JSON objects
        this.property(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        this.property(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);

        // Determine which of the three APIs should be loaded
        DbConn cnx = Helpers.getDbSession();
        boolean loadApiSimple;
        boolean loadApiClient;
        boolean loadApiAdmin;

        if (context.getInitParameter("jqmnodeid") != null)
        {
            // The application is running hosted by a JQM node.
            Node n = null;

            try
            {
                n = Node.select_single(cnx, "node_select_by_id", Integer.parseInt(context.getInitParameter("jqmnodeid")));
            }
            catch (NoResultException e)
            {
                throw new RuntimeException("invalid configuration: no node of ID " + context.getInitParameter("jqmnodeid"));
            }
            loadApiSimple = !Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "disableWsApiSimple", "false"));
            loadApiClient = !Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "disableWsApiClient", "false"));
            loadApiAdmin = !Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "disableWsApiAdmin", "false"));

            loadApiAdmin = loadApiAdmin && (n.getLoadApiAdmin() == null ? false : n.getLoadApiAdmin());
            loadApiClient = loadApiClient && (n.getLoadApiClient() == null ? false : n.getLoadApiClient());
            loadApiSimple = loadApiSimple && (n.getLoapApiSimple() == null ? true : n.getLoapApiSimple());
        }
        else
        {
            // The application is hosted by some other server (Tomcat, JBoss... but not a JQM node)

            // Never load the simple API when not running on JQM's own server. This API relies on files that are local to the JQM server.
            loadApiSimple = false;
            // Always load the two others
            loadApiAdmin = true;
            loadApiClient = true;
        }
        Helpers.closeQuietly(cnx);

        // Load the APIs
        if (loadApiAdmin)
        {
            this.register(ServiceAdmin.class);
        }
        if (loadApiClient)
        {
            this.register(ServiceClient.class);
        }
        if (loadApiSimple)
        {
            this.register(ServiceSimple.class);
        }

        // Load the exception mappers
        this.register(ErrorHandler.class);
        this.register(JqmExceptionMapper.class);
        this.register(JqmInternalExceptionMapper.class);

        // Load the cache annotation helper
        this.register(HttpCacheImpl.class);
    }

}
