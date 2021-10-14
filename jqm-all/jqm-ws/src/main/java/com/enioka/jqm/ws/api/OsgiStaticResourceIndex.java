package com.enioka.jqm.ws.api;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;

/**
 * Configuration for serving static files through the OSGi HTTP whiteboard. Simply serves the index.html entry for our JS bundle (resulting
 * from the JS build).
 */
@Component(service = OsgiStaticResourceIndex.class, property = {
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=" + Integer.MIN_VALUE,
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=MAIN_HTTP_CTX)" })
@HttpWhiteboardResource(pattern = "/", prefix = "/dist/index.html")
public class OsgiStaticResourceIndex
{

}
