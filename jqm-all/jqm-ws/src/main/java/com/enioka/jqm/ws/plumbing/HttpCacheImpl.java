package com.enioka.jqm.ws.plumbing;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsExtension;

/**
 * The provider behind the {@link HttpCache} annotation. It is simply a {@link ContainerResponseFilter} that looks for the aforementioned
 * annotation and adds the <code>Cache-Control</code> header to the HTTP response if found.
 */
@Provider
@JaxrsExtension
@Component
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
