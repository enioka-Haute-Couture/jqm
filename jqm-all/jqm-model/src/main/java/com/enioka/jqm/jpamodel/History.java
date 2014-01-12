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

package com.enioka.jqm.jpamodel;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "History")
public class History implements Serializable
{
    private static final long serialVersionUID = -5249529794213078668L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    /************/
    /* Identity */

    @JoinColumn(name = "jobdef_id")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = com.enioka.jqm.jpamodel.JobDef.class)
    private JobDef jd;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = com.enioka.jqm.jpamodel.Queue.class)
    @JoinColumn(name = "queue_id")
    private Queue queue;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = com.enioka.jqm.jpamodel.Node.class)
    @JoinColumn(name = "node_id")
    private Node node;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "history_id")
    private List<JobHistoryParameter> parameters;

    @Column(name = "highlander")
    private boolean highlander = false;

    /***********/
    /* RESULTS */

    @Column(length = 20, name = "status")
    @Enumerated(EnumType.STRING)
    private State status = State.SUBMITTED;

    @Column(name = "return_code")
    private Integer returnedValue;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "history")
    private List<Message> messages;

    @Column
    private Integer progress;

    /***********/
    /* TIME */

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "enqueue_date")
    private Calendar enqueueDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "execution_date")
    private Calendar executionDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = true)
    private Calendar endDate;

    /***************************/
    /* Instance classification */

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "job_instance_id", nullable = false)
    private Integer jobInstanceId;

    @Column(name = "username")
    private String userName;

    @Column(name = "email")
    private String email;

    @Column(name = "parent_job_id")
    private Integer parentJobId;

    @Column(length = 50, name = "instance_application")
    private String instanceApplication;

    @Column(length = 50, name = "instance_module")
    private String instanceModule;

    @Column(length = 50, name = "instance_keyword1")
    private String instanceKeyword1;

    @Column(length = 50, name = "instance_keyword2")
    private String instanceKeyword2;

    @Column(length = 50, name = "instance_keyword3")
    private String instanceKeyword3;

    /**************************/
    /* Job Def classification */
    @Column(length = 50, name = "keyword1")
    private String keyword1;

    @Column(length = 50, name = "keyword2")
    private String keyword2;

    @Column(length = 50, name = "keyword3")
    private String keyword3;

    @Column(length = 50, name = "application")
    private String application;

    @Column(length = 50, name = "module")
    private String module;

    public Integer getId()
    {
        return id;
    }

    public void setId(final Integer id)
    {
        this.id = id;
    }

    /**
     * @return the returnedValue
     */
    public Integer getReturnedValue()
    {
        return returnedValue;
    }

    /**
     * @param returnedValue
     *            the returnedValue to set
     */
    public void setReturnedValue(final Integer returnedValue)
    {
        this.returnedValue = returnedValue;
    }

    public Calendar getEnqueueDate()
    {
        return enqueueDate;
    }

    public void setEnqueueDate(final Calendar enqueueDate)
    {
        this.enqueueDate = enqueueDate;
    }

    public Calendar getExecutionDate()
    {
        return executionDate;
    }

    public void setExecutionDate(final Calendar executionDate)
    {
        this.executionDate = executionDate;
    }

    public Calendar getEndDate()
    {
        return endDate;
    }

    public void setEndDate(final Calendar endDate)
    {
        this.endDate = endDate;
    }

    public List<JobHistoryParameter> getParameters()
    {

        return parameters;
    }

    public List<Message> getMessages()
    {

        return messages;
    }

    public void setMessages(final List<Message> messages)
    {

        this.messages = messages;
    }

    public Queue getQueue()
    {
        return queue;
    }

    public void setQueue(final Queue queue)
    {
        this.queue = queue;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(final String userName)
    {
        this.userName = userName;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode(final Node node)
    {
        this.node = node;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(final String sessionId)
    {
        this.sessionId = sessionId;

    }

    public void setParameters(List<JobHistoryParameter> parameters)
    {
        this.parameters = parameters;
    }

    public JobDef getJd()
    {
        return jd;
    }

    public void setJd(JobDef jd)
    {
        this.jd = jd;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Integer getJobInstanceId()
    {
        return jobInstanceId;
    }

    public void setJobInstanceId(Integer jobInstanceId)
    {
        this.jobInstanceId = jobInstanceId;
    }

    public Integer getParentJobId()
    {
        return parentJobId;
    }

    public void setParentJobId(Integer parentJobId)
    {
        this.parentJobId = parentJobId;
    }

    public State getStatus()
    {
        return status;
    }

    public State getState()
    {
        return status;
    }

    public void setStatus(State status)
    {
        this.status = status;
    }

    public String getKeyword1()
    {

        return keyword1;
    }

    public void setKeyword1(String keyword1)
    {

        this.keyword1 = keyword1;
    }

    public String getKeyword2()
    {

        return keyword2;
    }

    public void setKeyword2(String keyword2)
    {

        this.keyword2 = keyword2;
    }

    public String getKeyword3()
    {

        return keyword3;
    }

    public void setKeyword3(String keyword3)
    {

        this.keyword3 = keyword3;
    }

    public String getApplication()
    {

        return application;
    }

    public void setApplication(String application)
    {

        this.application = application;
    }

    public String getModule()
    {

        return module;
    }

    public void setModule(String module)
    {

        this.module = module;
    }

    public Integer getProgress()
    {

        return progress;
    }

    public void setProgress(Integer progress)
    {

        this.progress = progress;
    }

    public boolean isHighlander()
    {
        return highlander;
    }

    public void setHighlander(boolean highlander)
    {
        this.highlander = highlander;
    }

    public String getInstanceApplication()
    {
        return instanceApplication;
    }

    public void setInstanceApplication(String instanceApplication)
    {
        this.instanceApplication = instanceApplication;
    }

    public String getInstanceModule()
    {
        return instanceModule;
    }

    public void setInstanceModule(String instanceModule)
    {
        this.instanceModule = instanceModule;
    }

    public String getInstanceKeyword1()
    {
        return instanceKeyword1;
    }

    public void setInstanceKeyword1(String instanceKeyword1)
    {
        this.instanceKeyword1 = instanceKeyword1;
    }

    public String getInstanceKeyword2()
    {
        return instanceKeyword2;
    }

    public void setInstanceKeyword2(String instanceKeyword2)
    {
        this.instanceKeyword2 = instanceKeyword2;
    }

    public String getInstanceKeyword3()
    {
        return instanceKeyword3;
    }

    public void setInstanceKeyword3(String instanceKeyword3)
    {
        this.instanceKeyword3 = instanceKeyword3;
    }
}
