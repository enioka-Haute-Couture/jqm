package com.enioka.jqm.ws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.enioka.jqm.api.JobRequest;

/**
 * Example resource class hosted at the URI path "/test"
 */
@Path("/test")
public class Resource
{
    /**
     * Method processing HTTP GET requests, producing "text/plain" MIME media type.
     * 
     * @return String that will be send back as a response of type "text/plain".
     */
    @GET
    @Path("hello")
    @Produces("text/plain")
    public String getIt()
    {
        return "Hi there!";
    }

    @GET
    @Path("testx")
    @Produces(MediaType.APPLICATION_XML)
    public JobRequest getJobDef()
    {
        return new JobRequest("rrr", "hhh");
    }
}
