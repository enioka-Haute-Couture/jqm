package org.apache.shiro.jqm;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
//
import org.apache.shiro.web.env.WebEnvironment;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardListener;

@Component(service = ServletContextListener.class, property = {
        org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=" + Integer.MAX_VALUE,
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=MAIN_HTTP_CTX)" })
@HttpWhiteboardListener
public class ShiroListener extends org.apache.shiro.web.env.EnvironmentLoaderListener
{
    // Empty - this class only exists in order to register EnvironmentLoaderListener in an OSGi context. (we could also create the service
    // XML by hand or with bnd but we try not to in JQM)

    @Override
    public WebEnvironment initEnvironment(ServletContext servletContext) throws IllegalStateException
    {
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ShiroListener.class.getClassLoader());
        WebEnvironment res = super.initEnvironment(servletContext);
        // Thread.currentThread().setContextClassLoader(l);
        return res;
    }
}
