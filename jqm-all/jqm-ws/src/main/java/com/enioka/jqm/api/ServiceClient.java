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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main web service class. Interface {@link JqmClient} is implemented, but it is not compulsory at all. (done for completion sake & and
 * ease of update). Not all methods are exposed - some things are better left to the caller.
 */
@Path("/client")
public class ServiceClient implements JqmClient
{
    static Logger log = LoggerFactory.getLogger(ServiceClient.class);

    // Not directly mapped: returning an integer would be weird. See enqueue_object.
    public int enqueue(JobRequest jd)
    {
        log.debug("calling WS enqueue");
        throw new NotSupportedException();
    }

    @POST
    @Path("ji")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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

    @Override
    @Path("ji/{id}")
    @POST
    public int enqueueFromHistory(@PathParam("id") int jobIdToCopy)
    {
        log.debug("calling WS enqueueFromHistory");
        return JqmClientFactory.getClient().enqueueFromHistory(jobIdToCopy);
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

    // Not exposed directly - we prefer objects to primitive types
    public int restartCrashedJob(int jobId)
    {
        log.debug("calling WS restartCrashedJob");
        return 0;
    }

    @Path("ji/crashed/{jobId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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

    // No need to expose. CLient side work.
    @Override
    public void setJobQueue(int jobId, Queue queue)
    {
        log.debug("calling WS setJobQueue");
        JqmClientFactory.getClient().setJobQueue(jobId, queue);
    }

    @Override
    @POST
    @Path("ji/{jobId}/position/{newPosition}")
    public void setJobQueuePosition(@PathParam("jobId") int jobId, @PathParam("newPosition") int newPosition)
    {
        log.debug("calling WS setJobQueuePosition");
        JqmClientFactory.getClient().setJobQueuePosition(jobId, newPosition);
    }

    @Override
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("ji/{jobId}")
    public JobInstance getJob(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS getJob");
        return JqmClientFactory.getClient().getJob(jobId);
    }

    @Override
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("ji")
    public List<JobInstance> getJobs()
    {
        log.debug("calling WS getJobs");
        return JqmClientFactory.getClient().getJobs();
    }

    @Override
    @GET
    @Path("ji/active")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<JobInstance> getActiveJobs()
    {
        log.debug("calling WS getActiveJobs");
        return JqmClientFactory.getClient().getActiveJobs();
    }

    @Override
    @Path("user/{username}/ji")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @GET
    public List<JobInstance> getUserActiveJobs(@PathParam("username") String userName)
    {
        log.debug("calling WS getUserActiveJobs");
        return JqmClientFactory.getClient().getUserActiveJobs(userName);
    }

    // Not exposed directly - the Query object passed as parameter actually contains results...
    @Override
    public List<JobInstance> getJobs(Query query)
    {
        log.debug("calling WS getJobs_Query");
        return JqmClientFactory.getClient().getJobs(query);
    }

    @Path("ji/query")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Query getJobsQuery(Query query)
    {
        log.debug("calling WS getJobsQuery");
        query.run();
        return query;
    }

    @Override
    @Path("ji/{jobId}/messages")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Deliverable> getJobDeliverables(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS getJobDeliverables");
        List<Deliverable> res = JqmClientFactory.getClient().getJobDeliverables(jobId);
        return res;
    }

    // Not exposed. Returning a list of files is a joke anyway... Loop should be client-side.
    @Override
    public List<InputStream> getJobDeliverablesContent(int jobId)
    {
        log.debug("calling WS getJobDeliverablesContent");
        throw new NotSupportedException();
    }

    @Override
    @Path("ji/files")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces("application/octet-stream")
    @POST
    public InputStream getDeliverableContent(Deliverable file)
    {
        log.debug("calling WS getDeliverableContent");
        return JqmClientFactory.getClient().getDeliverableContent(file);
    }

    @Override
    @Path("ji/files/{id}")
    @Produces("application/octet-stream")
    @GET
    public InputStream getDeliverableContent(@PathParam("id") int delId)
    {
        log.debug("calling WS getDeliverableContent");
        return JqmClientFactory.getClient().getDeliverableContent(delId);
    }

    @Override
    @Path("ji/{jobId}/stderr")
    @Produces("application/octet-stream")
    @GET
    public InputStream getJobLogStdErr(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS getJobLogStdErr");
        return JqmClientFactory.getClient().getJobLogStdErr(jobId);
    }

    @Override
    @Path("ji/{jobId}/stdout")
    @Produces("application/octet-stream")
    @GET
    public InputStream getJobLogStdOut(@PathParam("jobId") int jobId)
    {
        log.debug("calling WS getJobLogStdOut");
        return JqmClientFactory.getClient().getJobLogStdOut(jobId);
    }

    @Override
    @Path("q")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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

    @Path("jd")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Override
    public List<JobDef> getJobDefinitions()
    {
        log.debug("calling WS getJobDefinitions-no args");
        return JqmClientFactory.getClient().getJobDefinitions();
    }

    @Path("jd/{applicationName}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Override
    public List<JobDef> getJobDefinitions(@PathParam("applicationName") String application)
    {
        log.debug("calling WS getJobDefinitions-app");
        return JqmClientFactory.getClient().getJobDefinitions(application);
    }

    @Path("ji/query")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Query getEmptyQuery()
    {
        return Query.create();
    }

    @Path("jr")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JobRequest getEmptyJobRequest()
    {
        return new JobRequest("appName", "rsapi user");
    }

}
