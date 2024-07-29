package com.enioka.jqm.ws.plumbing;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.ext.Provider;

/**
 * A simple JAX-RS event listener which simply logs every exception occurring during a JAX-RS call.
 */
@Provider
public class ExceptionLogger implements ContainerResponseFilter
{
    private static final Logger log = LoggerFactory.getLogger("RestClientException");

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException
    {
        if (responseContext.getStatusInfo().getFamily() != Family.SUCCESSFUL)
        {
            log.info("a REST call failed " + responseContext.getStatusInfo().getReasonPhrase());
        }
    }
}
