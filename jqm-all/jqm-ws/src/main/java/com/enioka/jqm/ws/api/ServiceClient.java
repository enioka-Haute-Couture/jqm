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
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.client.api.Deliverable;
import com.enioka.jqm.client.api.JobDef;
import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.JobRequest;
import com.enioka.jqm.client.api.JqmClient;
import com.enioka.jqm.client.api.Query;
import com.enioka.jqm.client.api.Queue;
import com.enioka.jqm.client.api.QueueStatus;
import com.enioka.jqm.client.api.State;
import com.enioka.jqm.client.jdbc.api.JqmClientFactory;
import com.enioka.jqm.client.shared.JobRequestBaseImpl;
import com.enioka.jqm.client.shared.QueryBaseImpl;
import com.enioka.jqm.client.shared.SelfDestructFileStream;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.ws.plumbing.HttpCache;

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

import static com.enioka.jqm.shared.misc.StandaloneHelpers.ipFromId;

/**
 * The main web service class for doing operations on JobInstances.
 */
@Path("/client")
public class ServiceClient
{
    static Logger log = LoggerFactory.getLogger(ServiceClient.class);

    private boolean standaloneMode;
    private String localIp;

    @Activate
    public void onServiceActivation(Map<String, Object> properties)
    {
        log.info("\tStarting ServiceClient");
        standaloneMode = Boolean.parseBoolean(
            GlobalParameter.getParameter(DbManager.getDb().getConn(), "wsStandaloneMode", "false"));

        if (standaloneMode) {
            try {
                localIp = Inet4Address.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw new IllegalStateException(e);
            }
        }
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
        JobRequest target = Helpers.getClient().newJobRequest(jd.getApplicationName(), jd.getUser());

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

        long i = target.enqueue();

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
    public Long enqueueFromHistory(@PathParam("id") long jobIdToCopy)
    {
        return Helpers.getClient().enqueueFromHistory(jobIdToCopy);
    }

    @Path("ji/cancelled/{jobId}")
    @POST
    public void cancelJob(@PathParam("jobId") long jobId)
    {
        Helpers.getClient().cancelJob(jobId);
    }

    @Path("ji/waiting/{jobId}")
    @DELETE
    public void deleteJob(@PathParam("jobId") long jobId)
    {
        Helpers.getClient().deleteJob(jobId);
    }

    @Path("ji/killed/{jobId}")
    @POST
    public void killJob(@PathParam("jobId") long jobId)
    {
        Helpers.getClient().killJob(jobId);
    }

    @Path("schedule/{scheduleId}")
    @DELETE
    public void removeRecurrence(@PathParam("scheduleId") long scheduleId)
    {
        Helpers.getClient().removeRecurrence(scheduleId);
    }

    @Path("ji/paused/{jobId}")
    @POST
    public void pauseQueuedJob(@PathParam("jobId") long jobId)
    {
        Helpers.getClient().pauseQueuedJob(jobId);
    }

    @Path("ji/paused/{jobId}")
    @DELETE
    public void resumeQueuedJob(@PathParam("jobId") long jobId)
    {
        Helpers.getClient().resumeQueuedJob(jobId);
    }

    public void resumeJob(@PathParam("jobId") long jobId)
    {
        resumeQueuedJob(jobId);
    }

    @Path("ji/running/paused/{jobId}")
    @POST
    public void pauseRunningJob(@PathParam("jobId") long jobId)
    {
        Helpers.getClient().pauseRunningJob(jobId);
    }

    @Path("ji/running/paused/{jobId}")
    @DELETE
    public void resumeRunningJob(@PathParam("jobId") long jobId)
    {
        Helpers.getClient().resumeRunningJob(jobId);
    }

    // Not exposed directly - we prefer objects to primitive types
    public int restartCrashedJob(int jobId)
    {
        return 0;
    }

    @Path("ji/crashed/{jobId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @DELETE
    public JobInstance restartCrashedJobObject(@PathParam("jobId") long jobId)
    {
        long i = Helpers.getClient().restartCrashedJob(jobId);
        return getJob(i);
    }

    @Path("q/{queueId: [0-9]+}/{jobId: [0-9]+}")
    @POST
    public void setJobQueue(@PathParam("jobId") long jobId, @PathParam("queueId") long queueId)
    {
        Helpers.getClient().setJobQueue(jobId, queueId);
    }

    // No need to expose. Client side work.

    public void setJobQueue(Long jobId, Queue queue)
    {
        Helpers.getClient().setJobQueue(jobId, queue);
    }

    @POST
    @Path("ji/{jobId}/position/{newPosition}")
    public void setJobQueuePosition(@PathParam("jobId") long jobId, @PathParam("newPosition") int newPosition)
    {
        Helpers.getClient().setJobQueuePosition(jobId, newPosition);
    }

    @POST
    @Path("ji/{jobId}/priority/{priority}")
    public void setJobPriority(@PathParam("jobId") long jobId, @PathParam("priority") int priority)
    {
        Helpers.getClient().setJobPriority(jobId, priority);
    }

    public void setJobRunAfter(@PathParam("jobId") long jobId, @PathParam("whenToRun") Calendar whenToRun)
    {
        Helpers.getClient().setJobRunAfter(jobId, whenToRun);
    }

    @POST
    @Path("ji/{jobId}/delay/{whenToRun}")
    public void setJobRunAfter(@PathParam("jobId") long jobId, @PathParam("whenToRun") long whenToRun)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(whenToRun);
        Helpers.getClient().setJobRunAfter(jobId, c);
    }

    @POST
    @Path("schedule/{scheduleId}/queue/{queueId}")
    public void setScheduleQueue(@PathParam("scheduleId") long scheduleId, @PathParam("queueId") long queueId)
    {
        Helpers.getClient().setScheduleQueue(scheduleId, queueId);
    }

    @POST
    @Path("schedule/{scheduleId}/cron/{cronExpression}")
    public void setScheduleRecurrence(@PathParam("scheduleId") long scheduleId, @PathParam("cronExpression") String cronExpression)
    {
        Helpers.getClient().setScheduleRecurrence(scheduleId, cronExpression);
    }

    @POST
    @Path("schedule/{scheduleId}/priority/{priority}")
    public void setSchedulePriority(@PathParam("scheduleId") long scheduleId, @PathParam("priority") int priority)
    {
        Helpers.getClient().setSchedulePriority(scheduleId, priority);
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("ji/{jobId}")
    @HttpCache("public, max-age=60")
    public JobInstance getJob(@PathParam("jobId") long jobId)
    {
        JqmClient client;
        if (standaloneMode && !ipFromId(jobId).equals(localIp)) {
            // Redirect to distant node with Jersey client
            final var p = new Properties();
            p.setProperty("com.enioka.jqm.ws.url", "http://" + ipFromId(jobId) + ":1789/ws/client");
            client = com.enioka.jqm.client.jersey.api.JqmClientFactory.getClient(null, p, false);
        } else {
            // Use local node with JDBC client
            client = com.enioka.jqm.client.jdbc.api.JqmClientFactory.getClient();
        }
        return client.getJob(jobId);
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("ji")
    @HttpCache("public, max-age=60")
    public List<JobInstance> getJobs()
    {
        return Helpers.getClient().getJobs();
    }

    @GET
    @Path("ji/active")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<JobInstance> getActiveJobs()
    {
        return Helpers.getClient().getActiveJobs();
    }

    @Path("user/{username}/ji")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @GET
    @HttpCache("public, max-age=60")
    public List<JobInstance> getUserActiveJobs(@PathParam("username") String userName)
    {
        return Helpers.getClient().getUserActiveJobs(userName);
    }

    @Path("ji/query")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Query getJobsQuery(QueryBaseImpl query)
    {
        Query target = Helpers.getClient().newQuery();

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
    public List<String> getJobMessages(@PathParam("jobId") long jobId)
    {
        return Helpers.getClient().getJobMessages(jobId);
    }

    // Not exposed. Use getJob => progress

    public int getJobProgress(long jobId)
    {
        throw new NotSupportedException();
    }

    @Path("ji/{jobId}/files")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<Deliverable> getJobDeliverables(@PathParam("jobId") long jobId)
    {
        return Helpers.getClient().getJobDeliverables(jobId);
    }

    // Not exposed. Returning a list of files is a joke anyway... Loop should be
    // client-side.

    public List<InputStream> getJobDeliverablesContent(long jobId)
    {
        throw new NotSupportedException();
    }

    @Path("ji/files")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces("application/octet-stream")
    @POST
    public InputStream getDeliverableContent(Deliverable file, @Context HttpServletResponse res)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) Helpers.getClient().getDeliverableContent(file);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Path("ji/files/{id}")
    @Produces("application/octet-stream")
    @GET
    public InputStream getDeliverableContent(@PathParam("id") long delId, @Context HttpServletResponse res)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) Helpers.getClient().getDeliverableContent(delId);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Path("ji/{jobId}/stderr")
    @Produces("application/octet-stream")
    @GET
    public InputStream getJobLogStdErr(@PathParam("jobId") long jobId, @Context HttpServletResponse res)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) Helpers.getClient().getJobLogStdErr(jobId);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Path("ji/{jobId}/stdout")
    @Produces("application/octet-stream")
    @GET
    public InputStream getJobLogStdOut(@PathParam("jobId") long jobId, @Context HttpServletResponse res)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) Helpers.getClient().getJobLogStdOut(jobId);
        res.setHeader("Content-Disposition", "attachment; filename=" + fs.nameHint);
        return fs;
    }

    @Path("q")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public List<Queue> getQueues()
    {
        return Helpers.getClient().getQueues();
    }

    public void pauseQueue(Queue q)
    {
        Helpers.getClient().pauseQueue(q);
    }

    @Path("q/{qId}/pause")
    @POST
    public void pauseQueue(@PathParam("qId") long qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        Helpers.getClient().pauseQueue(q);
    }

    public void resumeQueue(Queue q)
    {
        resumeQueue(q.getId());
    }

    @Path("q/{qId}/pause")
    @DELETE
    public void resumeQueue(@PathParam("qId") long qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        Helpers.getClient().resumeQueue(q);
    }

    public void clearQueue(Queue q)
    {
        clearQueue(q.getId());
    }

    @Path("q/{qId}/clear")
    @POST
    public void clearQueue(@PathParam("qId") long qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        Helpers.getClient().clearQueue(q);
    }

    @Path("q/{qId}/status")
    @GET
    public QueueStatus getQueueStatus(@PathParam("qId") long qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        return getQueueStatus(q);
    }

    public QueueStatus getQueueStatus(Queue q)
    {
        return Helpers.getClient().getQueueStatus(q);
    }

    @Path("q/{qId}/enabled-capacity")
    @GET
    public int getQueueEnabledCapacity(@PathParam("qId") long qId)
    {
        Queue q = new Queue();
        q.setId(qId);
        return getQueueEnabledCapacity(q);
    }

    public int getQueueEnabledCapacity(Queue q)
    {
        return Helpers.getClient().getQueueEnabledCapacity(q);
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
        return Helpers.getClient().getJobDefinitions();
    }

    @Path("jd/{applicationName}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })

    @HttpCache("public, max-age=60")
    public List<JobDef> getJobDefinitions(@PathParam("applicationName") String application)
    {
        return Helpers.getClient().getJobDefinitions(application);
    }

    @Path("jd/name/{name}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=60")
    public JobDef getJobDefinition(@PathParam("name") String name)
    {
        return Helpers.getClient().getJobDefinition(name);
    }

    @Path("ji/query")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=3600")
    public Query getEmptyQuery()
    {
        return Helpers.getClient().newQuery();
    }

    @Path("jr")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @HttpCache("public, max-age=3600")
    public JobRequest getEmptyJobRequest()
    {
        return Helpers.getClient().newJobRequest("appName", "rsapi user");
    }

}
