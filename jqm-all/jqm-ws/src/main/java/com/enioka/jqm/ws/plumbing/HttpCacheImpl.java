package com.enioka.jqm.ws.plumbing;

import java.io.IOException;
import java.lang.annotation.Annotation;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

/**
 * The provider behind the {@link HttpCache} annotation. It is simply a {@link ContainerResponseFilter} that looks for the aforementioned
 * annotation and adds the <code>Cache-Control</code> header to the HTTP response if found.
 */
@Provider
public class HttpCacheImpl implements ContainerResponseFilter
{
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException
    {
        for (Annotation a : responseContext.getEntityAnnotations())
        {
            if (a.annotationType() == HttpCache.class)
            {
                String value = ((HttpCache) a).value();
                responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, value);
                break;
            }
        }
    }
}
