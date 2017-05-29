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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
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

    private @Context HttpServletResponse res;

    // Not directly mapped: returning an integer would be weird. See enqueue_object.
    public int enqueue(JobRequest jd)
    {
        throw new NotSupportedException();
    }

    @POST
    @Path("ji")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JobInstance enqueue_object(JobRequest jd)
    {
        int i = JqmClientFactory.getClient().enqueue(jd);

        return getJi(jd, i);
    }

    // Not exposed. Client side work.
    @Override
    public int enqueue(String applicationName, String userName)
    {
        throw new NotSupportedException();
    }

    @Override
    @Path("ji/{id}")
    @POST
    public int enqueueFromHistory(@PathParam("id") int jobIdToCopy)
    {
        return JqmClientFactory.getClient().enqueueFromHistory(jobIdToCopy);
    }

    @Override
    @Path("ji/cancelled/{jobId}")
    @POST
    public void cancelJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().cancelJob(jobId);
    }

    @Override
    @Path("ji/waiting/{jobId}")
    @DELETE
    public void deleteJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().deleteJob(jobId);
    }

    @Override
    @Path("ji/killed/{jobId}")
    @POST
    public void killJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().killJob(jobId);
    }

    @Override
    @Path("schedule/{scheduleId}")
    @DELETE
    public void removeRecurrence(int scheduleId)
    {
        JqmClientFactory.getClient().removeRecurrence(scheduleId);
    }

    @Override
    @Path("ji/paused/{jobId}")
    @POST
    public void pauseQueuedJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().pauseQueuedJob(jobId);
    }

    @Override
    @Path("ji/paused/{jobId}")
    @DELETE
    public void resumeJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().resumeJob(jobId);
    }

    // Not exposed directly - we prefer objects to primitive types
    public int restartCrashedJob(int jobId)
    {
        return 0;
    }

    @Path("ji/crashed/{jobId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @DELETE
    public JobInstance restartCrashedJob_object(@PathParam("jobId") int jobId)
    {
        int i = JqmClientFactory.getClient().restartCrashedJob(jobId);
        return getJob(i);
    }

    @Override
    @Path("q/{queueId: [0-9]+}/{jobId: [0-9]+}")
    @POST
    public void setJobQueue(@PathParam("jobId") int jobId, @PathParam("queueId") int queueId)
    {
        JqmClientFactory.getClient().setJobQueue(jobId, queueId);
    }

    // No need to expose. Client side work.
    @Override
    public void setJobQueue(int jobId, Queue queue)
    {
        JqmClientFactory.getClient().setJobQueue(jobId, queue);
    }

    @Override
    @POST
    @Path("ji/{jobId}/position/{newPosition}")
    public void setJobQueuePosition(@PathParam("jobId") int jobId, @PathParam("newPosition") int newPosition)
    {
        JqmClientFactory.getClient().setJobQueuePosition(jobId, newPosition);
    }

    @Override
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("ji/{jobId}")
    @HttpCache("public, max-age=60")
    public JobInstance getJob(@PathParam("jobId") int jobId)
    {
        return JqmClientFactory.getClient().getJob(jobId);
    }

    @Override
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("ji")
    @HttpCache("public, max-age=60")
    public List<JobInstance> getJobs()
    {
        return JqmClientFactory.getClient().getJobs();
    }

    @Override
    @GET
    @Path("ji/active")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<JobInstance> getActiveJobs()
    {
        return JqmClientFactory.getClient().getActiveJobs();
    }

    @Override
    @Path("user/{username}/ji")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @GET
    @HttpCache("public, max-age=60")
    public List<JobInstance> getUserActiveJobs(@PathParam("username") String userName)
    {
        return JqmClientFactory.getClient().getUserActiveJobs(userName);
    }

    // Not exposed directly - the Query object passed as parameter actually contains results...
    @Override
    public List<JobInstance> getJobs(Query query)
    {
        return JqmClientFactory.getClient().getJobs(query);
    }

    @Path("ji/query")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Query getJobsQuery(Query query)
    {
        query.run();
        return query;
    }

    @Override
    @Path("ji/{jobId}/messages")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<String> getJobMessages(@PathParam("jobId") int jobId)
    {
        return JqmClientFactory.getClient().getJobMessages(jobId);
    }

    // Not exposed. Use getJob => progress
    @Override
    public int getJobProgress(int jobId)
    {
        throw new NotSupportedException();
    }

    @Override
    @Path("ji/{jobId}/files")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<Deliverable> getJobDeliverables(@PathParam("jobId") int jobId)
    {
        List<Deliverable> res = JqmClientFactory.getClient().getJobDeliverables(jobId);
        return res;
    }

    // Not exposed. Returning a list of files is a joke anyway... Loop should be client-side.
    @Override
    public List<InputStream> getJobDeliverablesContent(int jobId)
    {
        throw new NotSupportedException();
    }

    @Override
    @Path("ji/files")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces("application/octet-stream")
    @POST
    public InputStream getDeliverableContent(Deliverable file)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) JqmClientFactory.getClient().getDeliverableContent(file);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Override
    @Path("ji/files/{id}")
    @Produces("application/octet-stream")
    @GET
    public InputStream getDeliverableContent(@PathParam("id") int delId)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) JqmClientFactory.getClient().getDeliverableContent(delId);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Override
    @Path("ji/{jobId}/stderr")
    @Produces("application/octet-stream")
    @GET
    public InputStream getJobLogStdErr(@PathParam("jobId") int jobId)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) JqmClientFactory.getClient().getJobLogStdErr(jobId);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Override
    @Path("ji/{jobId}/stdout")
    @Produces("application/octet-stream")
    @GET
    public InputStream getJobLogStdOut(@PathParam("jobId") int jobId)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) JqmClientFactory.getClient().getJobLogStdOut(jobId);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Override
    @Path("q")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<Queue> getQueues()
    {
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
        ji.setPosition(Long.MAX_VALUE);
        ji.setApplication(jd.getApplication());

        return ji;
    }

    @Path("jd")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Override
    @HttpCache("public, max-age=60")
    public List<JobDef> getJobDefinitions()
    {
        return JqmClientFactory.getClient().getJobDefinitions();
    }

    @Path("jd/{applicationName}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Override
    @HttpCache("public, max-age=60")
    public List<JobDef> getJobDefinitions(@PathParam("applicationName") String application)
    {
        return JqmClientFactory.getClient().getJobDefinitions(application);
    }

    @Path("ji/query")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=3600")
    public Query getEmptyQuery()
    {
        return Query.create();
    }

    @Path("jr")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=3600")
    public JobRequest getEmptyJobRequest()
    {
        return new JobRequest("appName", "rsapi user");
    }

}
