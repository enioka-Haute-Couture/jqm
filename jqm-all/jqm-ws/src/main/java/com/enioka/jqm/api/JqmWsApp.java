package com.enioka.jqm.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.bridge.SLF4JBridgeHandler;

@ApplicationPath("/ws")
public class JqmWsApp extends ResourceConfig
{
    static
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    public JqmWsApp()
    {
        packages("com.enioka.jqm.ws");
    }
}
