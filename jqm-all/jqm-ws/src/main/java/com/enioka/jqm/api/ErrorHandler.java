package com.enioka.jqm.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ErrorHandler implements ExceptionMapper<ErrorDto>
{
    @Override
    public Response toResponse(ErrorDto e)
    {
        return Response.status(e.httpStatus).entity(e).type(MediaType.APPLICATION_JSON).build();
    }
}