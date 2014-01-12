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

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.jpamodel.State;

/**
 * Represents the result of a job execution request - either a queued request, or a running job, or the result of said execution.
 * 
 */
public class JobInstance
{
    private Integer id;
    private JobDefinition jd;
    private Integer parent;
    private String user;
    private String sessionID;
    private State state;
    private Integer position;
    private Queue queue;
    private Map<String, String> parameters = new HashMap<String, String>();
    private Integer progress;

    /**
     * The Job instance ID. This is a key for numerous Dispatcher functions.
     * 
     * @return
     */
    public Integer getId()
    {
        return id;
    }

    void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * A JobDefinition that contains all the data that was used to create the job request.
     * 
     * @return
     */
    public JobDefinition getJd()
    {
        return jd;
    }

    void setJd(JobDefinition jd)
    {
        this.jd = jd;
    }

    /**
     * The ID of the parent job that has enqueued this job instance. Optionnal.
     * 
     * @return
     */
    public Integer getParent()
    {
        return parent;
    }

    void setParent(Integer parent)
    {
        this.parent = parent;
    }

    /**
     * The user that was given at enqueue time.
     * 
     * @return
     */
    public String getUser()
    {
        return user;
    }

    void setUser(String user)
    {
        this.user = user;
    }

    /**
     * The session ID that was given at enqueue time. Optional.
     * 
     * @return
     */
    public String getSessionID()
    {
        return sessionID;
    }

    void setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
    }

    /**
     * Status of the job. usual values: SUBMITTED, ATTRIBUTED, RUNNING, DONE, CRASHED.
     * 
     * @return
     */
    public State getState()
    {
        return state;
    }

    void setState(State state)
    {
        this.state = state;
    }

    /**
     * Position in the queue. 0 if running.
     * 
     * @return
     */
    public Integer getPosition()
    {
        return position;
    }

    void setPosition(Integer position)
    {
        this.position = position;
    }

    /**
     * The queue in which the job was enqueued.
     * 
     * @return
     */
    public Queue getQueue()
    {
        return queue;
    }

    void setQueue(Queue queue)
    {
        this.queue = queue;
    }

    /**
     * A list of all the parameters used by this job (both those passed at enqueue time and those defined as default parameters fot this
     * kind of jobs)
     * 
     * @return
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    void setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    public Integer getProgress()
    {
        return progress;
    }

    public void setProgress(Integer progress)
    {
        this.progress = progress;
    }
}
