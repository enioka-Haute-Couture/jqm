package com.enioka.jqm.ws;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.api.JobInstance;

@Path("ji")
public class QueueOperation
{
    static Logger log = Logger.getLogger(QueueOperation.class);

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    public int enqueue(JobDefinition jd)
    {
        log.debug("enqueue xs");
        return Dispatcher.enQueue(jd);
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public List<JobInstance> getJobInstances()
    {
        return Dispatcher.getJobs();
    }
}
