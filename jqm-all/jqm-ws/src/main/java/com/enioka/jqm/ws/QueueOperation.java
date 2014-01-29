package com.enioka.jqm.ws;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.enioka.jqm.api.Deliverable;
import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClient;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Queue;

/**
 * The main Web service class. Interface {@link JqmClient} is implemented for completion sake, but it is not compulsory at all.
 * 
 */
@Path("ji")
public class QueueOperation implements JqmClient
{
    static Logger log = Logger.getLogger(QueueOperation.class);

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    public int enqueue(JobRequest jd)
    {
        log.debug("enqueue xs");
        return JqmClientFactory.getClient().enqueue(jd);
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public List<JobInstance> getJobInstances()
    {
        return JqmClientFactory.getClient().getJobs();
    }

    @Override
    public int enqueue(String applicationName, String userName)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int enqueueFromHistory(int jobIdToCopy)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void cancelJob(int jobId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteJob(int jobId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void killJob(int jobId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void pauseQueuedJob(int jobId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void resumeJob(int jobId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int restartCrashedJob(int jobId)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setJobQueue(int jobId, int queueId)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setJobQueue(int jobId, Queue queue)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setJobQueuePosition(int jobId, int newPosition)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public JobInstance getJob(int jobId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<JobInstance> getJobs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<JobInstance> getActiveJobs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<JobInstance> getUserActiveJobs(String userName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getJobMessages(int jobId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getJobProgress(int jobId)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Deliverable> getJobDeliverables(int jobId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<InputStream> getJobDeliverablesContent(int jobId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getDeliverableContent(Deliverable file)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Queue> getQueues()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub

    }
}
