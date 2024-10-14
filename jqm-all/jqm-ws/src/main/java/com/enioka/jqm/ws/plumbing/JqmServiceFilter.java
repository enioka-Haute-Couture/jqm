package com.enioka.jqm.ws.plumbing;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.ws.api.ServiceAdmin;
import com.enioka.jqm.ws.api.ServiceClient;
import com.enioka.jqm.ws.api.ServiceSimple;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * A JAX-RS filter that checks if a service is enabled. This has replaced explicit app configuration on startup, as this prevented
 * annotation scanning and therefore made configuration more complicated than it should be.
 */
@Provider
public class JqmServiceFilter implements ContainerRequestFilter
{
    static Logger log = LoggerFactory.getLogger(JqmServiceFilter.class);

    @Context
    public ServletContext context;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException
    {
        var enabled = false;
        if (this.context.getInitParameter("startSimple") == null)
        {
            log.trace("No init parameters - services are enabled by default");
            return;
        }

        var clazz = resourceInfo.getResourceClass();
        if (clazz.isAssignableFrom(ServiceSimple.class))
        {
            enabled = Boolean.parseBoolean(this.context.getInitParameter("startSimple"));
        }
        else if (clazz.isAssignableFrom(ServiceClient.class))
        {
            enabled = Boolean.parseBoolean(this.context.getInitParameter("startClient"));
        }
        else if (clazz.isAssignableFrom(ServiceAdmin.class))
        {
            enabled = Boolean.parseBoolean(this.context.getInitParameter("startAdmin"));
        }
        else
        {
            log.warn("Unknown service {} - request is denied", clazz.getName());
        }

        if (!enabled)
        {
            log.trace("Service {} is not enabled", clazz.getName());
            requestContext.abortWith(Response.status(Response.Status.NOT_FOUND).build());
        }
    }
}
