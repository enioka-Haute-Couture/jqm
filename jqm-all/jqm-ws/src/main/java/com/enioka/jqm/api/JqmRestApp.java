package com.enioka.jqm.api;

import javax.ws.rs.ApplicationPath;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/ws/*")
public class JqmRestApp extends ResourceConfig
{
    public JqmRestApp()
    {
        this.property(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        this.property(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);

        this.register(ServiceAdmin.class);
        this.register(ServiceClient.class);
        this.register(JqmExceptionMapper.class);
        this.register(JqmInternalExceptionMapper.class);
        this.register(ErrorHandler.class);

    }

}
