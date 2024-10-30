/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enioka.jqm.ws.plumbing;


import com.enioka.jqm.ws.api.ErrorDto;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;;

public class JqmExceptionMapper<T extends Exception> implements ExceptionMapper<T>
{
    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(T exception)
    {
        ErrorDto d = new ErrorDto(exception.getMessage(), 10, exception, Status.BAD_REQUEST);
        MediaType type = headers.getMediaType();
        if (type != MediaType.APPLICATION_JSON_TYPE && type != MediaType.APPLICATION_XML_TYPE)
        {
            type = MediaType.APPLICATION_JSON_TYPE;
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(d).type(type).build();
    }
}
