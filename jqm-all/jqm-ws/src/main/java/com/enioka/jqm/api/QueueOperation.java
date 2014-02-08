package com.enioka.jqm.api;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

/**
 * The main web service class. Interface {@link JqmClient} is implemented, but it is not compulsory at all. (done for completion sake & and
 * ease of update). Not all methods are exposed - some things are better left to the caller.
 * 
 */
@Path("/")
public class QueueOperation implements JqmClient
{
    static Logger log = Logger.getLogger(QueueOperation.class);

    // Not directly mapped: returning an integer would be weird. See enqueue_object.
    public int enqueue(JobRequest jd)
    {
        log.debug("calling WS enqueue");
        return JqmClientFactory.getClient().enqueue(jd);
    }

    @POST
    @Path("ji")
    @Consumes(MediaType.APPLICATION_XML)
    public JobInstance enqueue_object(JobRequest jd)
    {
        log.debug("calling WS enqueue_object");
        int i = JqmClientFactory.getClient().enqueue(jd);

        return getJi(jd, i);
    }

    // Not exposed. Client side work.
    @Override
    public int enqueue(String applicationName, String userName)
    {
        log.debug("calling WS enqueue");
        throw new NotSupportedException();
    }

    // Not exposed. Client side work.
    @Override
    public int enqueueFromHistory(int jobIdToCopy)
    {
        log.debug("calling WS enqueueFromHistory");
        throw new NotSupportedException();
    }

    @Override
    @Path("ji/cancelled/{jobId}")
    @POST
    public void cancelJob(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS cancelJob");
        JqmClientFactory.getClient().cancelJob(jobId);
    }

    @Override
    @Path("ji/waiting/{jobId}")
    @DELETE
    public void deleteJob(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS deleteJob");
        JqmClientFactory.getClient().deleteJob(jobId);
    }

    @Override
    @Path("ji/killed/{jobId}")
    @POST
    public void killJob(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS killJob");
        JqmClientFactory.getClient().killJob(jobId);
    }

    @Override
    @Path("ji/paused/{jobId}")
    @POST
    public void pauseQueuedJob(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS pauseQueuedJob");
        JqmClientFactory.getClient().pauseQueuedJob(jobId);
    }

    @Override
    @Path("ji/paused/{jobId}")
    @DELETE
    public void resumeJob(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS resumeJob");
        JqmClientFactory.getClient().resumeJob(jobId);
    }

    // Not exposed - we prefer objects to primitive types
    public int restartCrashedJob(int jobId)
    {
        log.debug("calling WS restartCrashedJob");
        return 0;
    }

    @Path("ji/crashed/{jobId}")
    @DELETE
    public JobInstance restartCrashedJob_object(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS restartCrashedJob_object");
        int i = JqmClientFactory.getClient().restartCrashedJob(jobId);
        return getJob(i);
    }

    @Override
    @Path("q/{queueId: [0-9]+}/{jobId: [0-9]+}")
    @POST
    public void setJobQueue(@PathParam("jobId") int jobId, @PathParam("queueId") int queueId)
    {
        log.debug("calling WS setJobQueue");
        JqmClientFactory.getClient().setJobQueue(jobId, queueId);
    }

    // No need to expose.
    @Override
    public void setJobQueue(int jobId, Queue queue)
    {
        log.debug("calling WS setJobQueue");
        JqmClientFactory.getClient().setJobQueue(jobId, queue);
    }

    // For now, not exposed. May be one day.
    @Override
    public void setJobQueuePosition(int jobId, int newPosition)
    {
        log.debug("calling WS setJobQueuePosition");
        JqmClientFactory.getClient().setJobQueuePosition(jobId, newPosition);
    }

    @Override
    @GET
    @Path("ji/{jobId}")
    public JobInstance getJob(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS getJob");
        return JqmClientFactory.getClient().getJob(jobId);
    }

    @Override
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("ji")
    public List<JobInstance> getJobs()
    {
        log.debug("calling WS getJobs");
        return JqmClientFactory.getClient().getJobs();
    }

    @Override
    @GET
    @Path("ji/active")
    @Produces(MediaType.APPLICATION_XML)
    public List<JobInstance> getActiveJobs()
    {
        log.debug("calling WS getActiveJobs");
        return JqmClientFactory.getClient().getActiveJobs();
    }

    @Override
    @Path("user/{username}/ji")
    @Produces(MediaType.APPLICATION_XML)
    @GET
    public List<JobInstance> getUserActiveJobs(@PathParam("username") String userName)
    {
        log.debug("calling WS getUserActiveJobs");
        return JqmClientFactory.getClient().getUserActiveJobs(userName);
    }

    @Override
    @Path("ji/query")
    @Consumes(MediaType.APPLICATION_XML)
    @POST
    @Produces(MediaType.APPLICATION_XML)
    public List<JobInstance> getJobs(Query query)
    {
        log.debug("calling WS getJobs_Query");
        return JqmClientFactory.getClient().getJobs(query);
    }

    // Not exposed. Use getJob => messages
    @Override
    @Path("ji/{jobId}/messages")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public List<String> getJobMessages(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS getJobMessages");
        return JqmClientFactory.getClient().getJobMessages(jobId);
    }

    // Not exposed. Use getJob => progress
    @Override
    public int getJobProgress(int jobId)
    {
        log.debug("calling WS getJobProgress");
        throw new NotSupportedException();
    }

    @Override
    @Path("ji/{jobId}/files")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public List<Deliverable> getJobDeliverables(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS getJobDeliverables");
        return JqmClientFactory.getClient().getJobDeliverables(jobId);
    }

    @Override
    public List<InputStream> getJobDeliverablesContent(int jobId)
    {
        log.debug("calling WS getJobDeliverablesContent");
        throw new NotSupportedException();
    }

    @Override
    public InputStream getDeliverableContent(Deliverable file)
    {
        log.debug("calling WS getDeliverableContent");
        throw new NotSupportedException();
    }

    @Override
    @Path("q")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public List<Queue> getQueues()
    {
        log.debug("calling WS getQueues");
        return JqmClientFactory.getClient().getQueues();
    }

    @Override
    public void dispose()
    {
        log.debug("calling WS dispose");
        // Nothing to do.
    }

    private JobInstance getJi(JobRequest jd, int i)
    {
        JobInstance ji = new JobInstance();
        ji.setId(i);
        ji.setKeyword1(jd.getKeyword1());
        ji.setKeyword2(jd.getKeyword2());
        ji.setKeyword3(jd.getKeyword3());
        ji.setParameters(jd.getParameters());
        ji.setParent(jd.getParentID());
        ji.setSessionID(jd.getSessionID());
        ji.setState(State.SUBMITTED);
        ji.setUser(jd.getUser());
        ji.setPosition(Integer.MAX_VALUE);
        ji.setApplication(jd.getApplication());

        return ji;
    }
}
