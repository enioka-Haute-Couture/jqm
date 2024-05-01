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
package com.enioka.jqm.ws.api;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import com.enioka.jqm.client.api.Deliverable;
import com.enioka.jqm.client.api.JobDef;
import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.JobRequest;
import com.enioka.jqm.client.api.Query;
import com.enioka.jqm.client.api.Queue;
import com.enioka.jqm.client.api.QueueStatus;
import com.enioka.jqm.client.api.State;
import com.enioka.jqm.client.jdbc.api.JqmClientFactory;
import com.enioka.jqm.client.shared.JobRequestBaseImpl;
import com.enioka.jqm.client.shared.QueryBaseImpl;
import com.enioka.jqm.client.shared.SelfDestructFileStream;
import com.enioka.jqm.ws.plumbing.HttpCache;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main web service class for doing operations on JobInstances.
 */

@Component(service = ServiceClient.class, configurationPolicy = ConfigurationPolicy.REQUIRE, scope = ServiceScope.SINGLETON)
@JakartarsResource
@Path("/client")
public class ServiceClient
{
    static Logger log = LoggerFactory.getLogger(ServiceClient.class);

    @Activate
    public void onServiceActivation(Map<String, Object> properties)
    {
        log.info("\tStarting ServiceClient");
    }

    // Not directly mapped: returning an integer would be weird. See enqueue_object.
    public int enqueue(JobRequest jd)
    {
        throw new NotSupportedException();
    }

    @POST
    @Path("ji")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JobInstance enqueueObject(JobRequestBaseImpl jd)
    {
        JobRequest target = JqmClientFactory.getClient().newJobRequest(jd.getApplicationName(), jd.getUser());

        target.setApplication(jd.getApplication());
        target.setEmail(jd.getEmail());
        target.setKeyword1(jd.getKeyword1());
        target.setKeyword2(jd.getKeyword2());
        target.setKeyword3(jd.getKeyword3());
        target.setModule(jd.getModule());
        target.setParameters(jd.getParameters());
        target.setParentID(jd.getParentID());
        target.setPriority(jd.getPriority());
        target.setQueueName(jd.getQueueName());
        target.setRecurrence(jd.getRecurrence());
        target.setRunAfter(jd.getRunAfter());
        target.setScheduleId(jd.getScheduleId());
        target.setSessionID(jd.getSessionID());
        if (jd.getStartState() == State.HOLDED)
        {
            target.startHeld();
        }

        int i = target.enqueue();

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

    // Not exposed. Client side work.

    public int enqueue(String applicationName, String userName)
    {
        throw new NotSupportedException();
    }

    @Path("ji/{id}")
    @POST
    public int enqueueFromHistory(@PathParam("id") int jobIdToCopy)
    {
        return JqmClientFactory.getClient().enqueueFromHistory(jobIdToCopy);
    }

    @Path("ji/cancelled/{jobId}")
    @POST
    public void cancelJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().cancelJob(jobId);
    }

    @Path("ji/waiting/{jobId}")
    @DELETE
    public void deleteJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().deleteJob(jobId);
    }

    @Path("ji/killed/{jobId}")
    @POST
    public void killJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().killJob(jobId);
    }

    @Path("schedule/{scheduleId}")
    @DELETE
    public void removeRecurrence(@PathParam("scheduleId") int scheduleId)
    {
        JqmClientFactory.getClient().removeRecurrence(scheduleId);
    }

    @Path("ji/paused/{jobId}")
    @POST
    public void pauseQueuedJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().pauseQueuedJob(jobId);
    }

    @Path("ji/paused/{jobId}")
    @DELETE
    public void resumeQueuedJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().resumeQueuedJob(jobId);
    }

    public void resumeJob(@PathParam("jobId") int jobId)
    {
        resumeQueuedJob(jobId);
    }

    @Path("ji/running/paused/{jobId}")
    @POST
    public void pauseRunningJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().pauseRunningJob(jobId);
    }

    @Path("ji/running/paused/{jobId}")
    @DELETE
    public void resumeRunningJob(@PathParam("jobId") int jobId)
    {
        JqmClientFactory.getClient().resumeRunningJob(jobId);
    }

    // Not exposed directly - we prefer objects to primitive types
    public int restartCrashedJob(int jobId)
    {
        return 0;
    }

    @Path("ji/crashed/{jobId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @DELETE
    public JobInstance restartCrashedJobObject(@PathParam("jobId") int jobId)
    {
        int i = JqmClientFactory.getClient().restartCrashedJob(jobId);
        return getJob(i);
    }

    @Path("q/{queueId: [0-9]+}/{jobId: [0-9]+}")
    @POST
    public void setJobQueue(@PathParam("jobId") int jobId, @PathParam("queueId") int queueId)
    {
        JqmClientFactory.getClient().setJobQueue(jobId, queueId);
    }

    // No need to expose. Client side work.

    public void setJobQueue(int jobId, Queue queue)
    {
        JqmClientFactory.getClient().setJobQueue(jobId, queue);
    }

    @POST
    @Path("ji/{jobId}/position/{newPosition}")
    public void setJobQueuePosition(@PathParam("jobId") int jobId, @PathParam("newPosition") int newPosition)
    {
        JqmClientFactory.getClient().setJobQueuePosition(jobId, newPosition);
    }

    @POST
    @Path("ji/{jobId}/priority/{priority}")
    public void setJobPriority(@PathParam("jobId") int jobId, @PathParam("priority") int priority)
    {
        JqmClientFactory.getClient().setJobPriority(jobId, priority);
    }

    public void setJobRunAfter(@PathParam("jobId") int jobId, @PathParam("whenToRun") Calendar whenToRun)
    {
        JqmClientFactory.getClient().setJobRunAfter(jobId, whenToRun);
    }

    @POST
    @Path("ji/{jobId}/delay/{whenToRun}")
    public void setJobRunAfter(@PathParam("jobId") int jobId, @PathParam("whenToRun") long whenToRun)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(whenToRun);
        JqmClientFactory.getClient().setJobRunAfter(jobId, c);
    }

    @POST
    @Path("schedule/{scheduleId}/queue/{queueId}")
    public void setScheduleQueue(@PathParam("scheduleId") int scheduleId, @PathParam("queueId") int queueId)
    {
        JqmClientFactory.getClient().setScheduleQueue(scheduleId, queueId);
    }

    @POST
    @Path("schedule/{scheduleId}/cron/{cronExpression}")
    public void setScheduleRecurrence(@PathParam("scheduleId") int scheduleId, @PathParam("cronExpression") String cronExpression)
    {
        JqmClientFactory.getClient().setScheduleRecurrence(scheduleId, cronExpression);
    }

    @POST
    @Path("schedule/{scheduleId}/priority/{priority}")
    public void setSchedulePriority(@PathParam("scheduleId") int scheduleId, @PathParam("priority") int priority)
    {
        JqmClientFactory.getClient().setSchedulePriority(scheduleId, priority);
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("ji/{jobId}")
    @HttpCache("public, max-age=60")
    public JobInstance getJob(@PathParam("jobId") int jobId)
    {
        return JqmClientFactory.getClient().getJob(jobId);
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("ji")
    @HttpCache("public, max-age=60")
    public List<JobInstance> getJobs()
    {
        return JqmClientFactory.getClient().getJobs();
    }

    @GET
    @Path("ji/active")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<JobInstance> getActiveJobs()
    {
        return JqmClientFactory.getClient().getActiveJobs();
    }

    @Path("user/{username}/ji")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @GET
    @HttpCache("public, max-age=60")
    public List<JobInstance> getUserActiveJobs(@PathParam("username") String userName)
    {
        return JqmClientFactory.getClient().getUserActiveJobs(userName);
    }

    @Path("ji/query")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Query getJobsQuery(QueryBaseImpl query)
    {
        Query target = JqmClientFactory.getClient().newQuery();

        target.setApplicationName(query.getApplicationName());
        target.setBeganRunningAfter(query.getBeganRunningAfter());
        target.setBeganRunningBefore(query.getBeganRunningBefore());
        target.setEndedAfter(query.getEndedAfter());
        target.setEndedBefore(query.getBeganRunningBefore());
        target.setEnqueuedAfter(query.getEndedAfter());
        target.setEnqueuedBefore(query.getEnqueuedBefore());
        target.setFirstRow(query.getFirstRow());
        target.setInstanceApplication(query.getInstanceApplication());
        target.setInstanceKeyword1(query.getInstanceKeyword1());
        target.setInstanceKeyword2(query.getInstanceKeyword2());
        target.setInstanceKeyword3(query.getInstanceKeyword3());
        target.setInstanceModule(query.getInstanceModule());
        target.setJobDefApplication(query.getJobDefApplication());
        target.setJobDefKeyword1(query.getJobDefKeyword1());
        target.setJobDefKeyword2(query.getJobDefKeyword2());
        target.setJobDefKeyword3(query.getJobDefKeyword3());
        target.setJobDefModule(query.getJobDefModule());
        target.setJobInstanceId(query.getJobInstanceId());
        target.setNodeName(query.getNodeName());
        target.setPageSize(query.getPageSize());
        target.setParentId(query.getParentId());
        target.setQueryHistoryInstances(query.isQueryHistoryInstances());
        target.setQueryLiveInstances(query.isQueryLiveInstances());
        target.setQueueId(query.getQueueId());
        target.setQueueName(query.getQueueName());
        // target.setResultSize(resultSize);
        // target.setResults(results);
        target.setSessionId(query.getSessionId());
        target.setUser(query.getUser());

        target.invoke();
        return target;
    }

    @Path("ji/{jobId}/messages")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<String> getJobMessages(@PathParam("jobId") int jobId)
    {
        return JqmClientFactory.getClient().getJobMessages(jobId);
    }

    // Not exposed. Use getJob => progress

    public int getJobProgress(int jobId)
    {
        throw new NotSupportedException();
    }

    @Path("ji/{jobId}/files")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<Deliverable> getJobDeliverables(@PathParam("jobId") int jobId)
    {
        return JqmClientFactory.getClient().getJobDeliverables(jobId);
    }

    // Not exposed. Returning a list of files is a joke anyway... Loop should be
    // client-side.

    public List<InputStream> getJobDeliverablesContent(int jobId)
    {
        throw new NotSupportedException();
    }

    @Path("ji/files")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces("application/octet-stream")
    @POST
    public InputStream getDeliverableContent(Deliverable file, @Context HttpServletResponse res)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) JqmClientFactory.getClient().getDeliverableContent(file);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Path("ji/files/{id}")
    @Produces("application/octet-stream")
    @GET
    public InputStream getDeliverableContent(@PathParam("id") int delId, @Context HttpServletResponse res)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) JqmClientFactory.getClient().getDeliverableContent(delId);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Path("ji/{jobId}/stderr")
    @Produces("application/octet-stream")
    @GET
    public InputStream getJobLogStdErr(@PathParam("jobId") int jobId, @Context HttpServletResponse res)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) JqmClientFactory.getClient().getJobLogStdErr(jobId);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Path("ji/{jobId}/stdout")
    @Produces("application/octet-stream")
    @GET
    public InputStream getJobLogStdOut(@PathParam("jobId") int jobId, @Context HttpServletResponse res)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) JqmClientFactory.getClient().getJobLogStdOut(jobId);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Path("q")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<Queue> getQueues()
    {
        return JqmClientFactory.getClient().getQueues();
    }

    public void pauseQueue(Queue q)
    {
        JqmClientFactory.getClient().pauseQueue(q);
    }

    @Path("q/{qId}/pause")
    @POST
    public void pauseQueue(@PathParam("qId") int qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        JqmClientFactory.getClient().pauseQueue(q);
    }

    public void resumeQueue(Queue q)
    {
        resumeQueue(q.getId());
    }

    @Path("q/{qId}/pause")
    @DELETE
    public void resumeQueue(@PathParam("qId") int qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        JqmClientFactory.getClient().resumeQueue(q);
    }

    public void clearQueue(Queue q)
    {
        clearQueue(q.getId());
    }

    @Path("q/{qId}/clear")
    @POST
    public void clearQueue(@PathParam("qId") int qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        JqmClientFactory.getClient().clearQueue(q);
    }

    @Path("q/{qId}/status")
    @GET
    public QueueStatus getQueueStatus(@PathParam("qId") int qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        return getQueueStatus(q);
    }

    public QueueStatus getQueueStatus(Queue q)
    {
        return JqmClientFactory.getClient().getQueueStatus(q);
    }

    @Path("q/{qId}/enabled-capacity")
    @GET
    public int getQueueEnabledCapacity(@PathParam("qId") int qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        return getQueueEnabledCapacity(q);
    }

    public int getQueueEnabledCapacity(Queue q)
    {
        return JqmClientFactory.getClient().getQueueEnabledCapacity(q);
    }

    public void dispose()
    {
        log.debug("calling WS dispose");
        // Nothing to do.
    }

    @Path("jd")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })

    @HttpCache("public, max-age=60")
    public List<JobDef> getJobDefinitions()
    {
        return JqmClientFactory.getClient().getJobDefinitions();
    }

    @Path("jd/{applicationName}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })

    @HttpCache("public, max-age=60")
    public List<JobDef> getJobDefinitions(@PathParam("applicationName") String application)
    {
        return JqmClientFactory.getClient().getJobDefinitions(application);
    }

    @Path("jd/name/{name}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public JobDef getJobDefinition(@PathParam("name") String name)
    {
        return JqmClientFactory.getClient().getJobDefinition(name);
    }

    @Path("ji/query")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=3600")
    public Query getEmptyQuery()
    {
        return JqmClientFactory.getClient().newQuery();
    }

    @Path("jr")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=3600")
    public JobRequest getEmptyJobRequest()
    {
        return JqmClientFactory.getClient().newJobRequest("appName", "rsapi user");
    }

}
