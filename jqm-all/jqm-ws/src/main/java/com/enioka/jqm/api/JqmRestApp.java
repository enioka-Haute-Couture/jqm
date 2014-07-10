package com.enioka.jqm.api;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.glassfish.jersey.server.ResourceConfig;

import com.enioka.jqm.jpamodel.Node;

@ApplicationPath("/ws/*")
public class JqmRestApp extends ResourceConfig
{
    public JqmRestApp(@Context ServletContext context)
    {
        // These two properties ensure lists are properly named in JSON objects
        this.property(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        this.property(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);

        // Determine which of the three APIs should be loaded
        EntityManager em = Helpers.getEm();
        boolean loadApiSimple;
        boolean loadApiClient;
        boolean loadApiAdmin;

        if (context.getInitParameter("jqmnodeid") != null)
        {
            Node n = em.find(Node.class, Integer.parseInt(context.getInitParameter("jqmnodeid")));
            if (n == null)
            {
                throw new RuntimeException("invalid configuration: no node of ID " + context.getInitParameter("jqmnodeid"));
            }
            loadApiSimple = !Boolean.parseBoolean(Helpers.getParameter("disableApiSimple", "false", em));
            loadApiClient = !Boolean.parseBoolean(Helpers.getParameter("disableApiClient", "false", em));
            loadApiAdmin = !Boolean.parseBoolean(Helpers.getParameter("disableApiAdmin", "false", em));

            loadApiAdmin = loadApiAdmin && (n.getLoadApiAdmin() == null ? false : n.getLoadApiAdmin());
            loadApiClient = loadApiClient && (n.getLoadApiClient() == null ? false : n.getLoadApiClient());
            loadApiSimple = loadApiSimple && (n.getLoapApiSimple() == null ? true : n.getLoapApiSimple());
        }
        else
        {
            // Never load the simple API when not running on JQM's own server. This API relies on files that are local to the JQM server.
            loadApiSimple = false;
            // Always load the two others
            loadApiAdmin = true;
            loadApiClient = true;
        }
        Helpers.closeQuietly(em);

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
        this.register(JqmExceptionMapper.class);
        this.register(JqmInternalExceptionMapper.class);
        this.register(ErrorHandler.class);
    }

}
