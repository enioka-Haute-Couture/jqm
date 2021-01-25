package com.enioka.jqm.ws.plumbing;

import java.net.URL;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

/**
 * OSGi can define multiple servlet contextes - we choose to create one explicitely and use it for all our servlet-related needs instead of
 * using the default context (which cannot receive init parameters)
 */
@Component(service = ServletContextHelper.class, property = { HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=MAIN_HTTP_CTX",
        HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH + "=/", HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_INIT_PARAM_PREFIX
                + "shiroConfigLocations=classpath:org/apache/shiro/jqm/shiro.ini" })
public class HttpContextSettings extends ServletContextHelper
{
    /**
     * Only serve resources from current bundle. (we could also pass a bundle context to the other ServletContextHelperconstructor to do the
     * same)
     */
    @Override
    public URL getResource(String name)
    {
        return HttpContextSettings.class.getResource(name);
    }
}
