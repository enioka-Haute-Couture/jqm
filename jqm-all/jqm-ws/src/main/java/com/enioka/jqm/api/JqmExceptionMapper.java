package com.enioka.jqm.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.enioka.jqm.api.JqmInvalidRequestException;

@Provider
public class JqmExceptionMapper implements ExceptionMapper<JqmInvalidRequestException>
{
    @Override
    public Response toResponse(JqmInvalidRequestException exception)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
    }

}
