package com.enioka.jqm.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JqmInternalExceptionMapper implements ExceptionMapper<JqmClientException>
{
    @Override
    public Response toResponse(JqmClientException exception)
    {
        ErrorDto d = new ErrorDto(exception.getMessage(), 500, exception, Status.INTERNAL_SERVER_ERROR);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(d).type(MediaType.APPLICATION_JSON).build();
    }
}
