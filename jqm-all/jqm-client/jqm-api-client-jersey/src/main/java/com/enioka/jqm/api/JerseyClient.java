/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;

/**
 * Main JQM client API entry point.
 */
final class JerseyClient implements JqmClient
{
    private Properties p;
    private Client client;
    private WebTarget target;

    // /////////////////////////////////////////////////////////////////////
    // Construction/Connection
    // /////////////////////////////////////////////////////////////////////

    // No public constructor. MUST use factory.
    JerseyClient(Properties p)
    {
        this.p = p;
        ClientConfig cc = new ClientConfig();
        // Later on, put SSL things here.
        client = ClientBuilder.newClient(cc);
        this.target = client.target(this.p.getProperty("com.enioka.ws.url", "http://localhost:1789/ws"));
    }

    @Override
    public void dispose()
    {
        p = null;
    }

    // /////////////////////////////////////////////////////////////////////
    // Enqueue functions
    // /////////////////////////////////////////////////////////////////////

    @Override
    public int enqueue(JobRequest jd)
    {
        return client.target("ji").request().put(Entity.entity(jd, MediaType.APPLICATION_XML), Integer.class);
    }

    @Override
    public int enqueue(String applicationName, String userName)
    {
        return enqueue(new JobRequest(applicationName, userName));
    }

    @Override
    public int enqueueFromHistory(int jobIdToCopy)
    {
        // TODO
        return 0;
    }

    // /////////////////////////////////////////////////////////////////////
    // Job destruction
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void cancelJob(int idJob)
    {
        // TODO
    }

    @Override
    public void deleteJob(int idJob)
    {
        // TODO
    }

    @Override
    public void killJob(int idJob)
    {
        // TODO
    }

    // /////////////////////////////////////////////////////////////////////
    // Job Pause/restart
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void pauseQueuedJob(int idJob)
    {
        // TODO
    }

    @Override
    public void resumeJob(int idJob)
    {
        // TODO
    }

    public int restartCrashedJob(int idJob)
    {
        // TODO
        return 0;
    }

    // /////////////////////////////////////////////////////////////////////
    // Misc.
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void setJobQueue(int idJob, int idQueue)
    {
        // TODO
    }

    @Override
    public void setJobQueue(int idJob, com.enioka.jqm.api.Queue queue)
    {
        setJobQueue(idJob, queue.getId());
    }

    @Override
    public void setJobQueuePosition(int idJob, int position)
    {
        // TODO
    }

    // /////////////////////////////////////////////////////////////////////
    // Job queries
    // /////////////////////////////////////////////////////////////////////

    @Override
    public com.enioka.jqm.api.JobInstance getJob(int idJob)
    {
        // TODO
        return null;
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getJobs()
    {
        System.out.println(target.path("ji").getUri());
        return target.path("ji").request().get(new GenericType<List<JobInstance>>()
        {
        });
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getActiveJobs()
    {
        // TODO
        return null;
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getUserActiveJobs(String user)
    {
        // TODO
        return null;
    }

    // /////////////////////////////////////////////////////////////////////
    // Helpers to quickly access some job instance properties
    // /////////////////////////////////////////////////////////////////////

    @Override
    public List<String> getJobMessages(int idJob)
    {
        return getJob(idJob).getMessages();
    }

    @Override
    public int getJobProgress(int idJob)
    {
        return getJob(idJob).getProgress();
    }

    // /////////////////////////////////////////////////////////////////////
    // Deliverables retrieval
    // /////////////////////////////////////////////////////////////////////

    @Override
    public List<com.enioka.jqm.api.Deliverable> getJobDeliverables(int idJob)
    {
        // TODO
        return null;
    }

    @Override
    public List<InputStream> getJobDeliverablesContent(int idJob)
    {
        // TODO
        return null;
    }

    @Override
    public InputStream getDeliverableContent(com.enioka.jqm.api.Deliverable d)
    {
        return null;
    }

    // /////////////////////////////////////////////////////////////////////
    // Parameters retrieval
    // /////////////////////////////////////////////////////////////////////

    @Override
    public List<com.enioka.jqm.api.Queue> getQueues()
    {
        // TODO
        return null;
    }
}
