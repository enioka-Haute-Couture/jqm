package com.enioka.jqm.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JqmInternalExceptionMapper implements ExceptionMapper<JqmClientException>
{
    @Override
    public Response toResponse(JqmClientException exception)
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
    }
}
