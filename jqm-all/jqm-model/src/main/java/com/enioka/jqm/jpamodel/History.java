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
    
    @Column(name = "returnedValue")
    private Integer returnedValue;
    
    @JoinColumn(name = "jd")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = com.enioka.jqm.jpamodel.JobDef.class)
    private JobDef jd;
    
    @Column(name = "sessionId")
    private String sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = com.enioka.jqm.jpamodel.Queue.class)
    @JoinColumn(name = "queue")
    private Queue queue;
    
    @OneToMany(fetch = FetchType.EAGER, targetEntity = com.enioka.jqm.jpamodel.Message.class, cascade = CascadeType.ALL, mappedBy = "history")
    private List<Message> messages;
    
    @Column(nullable = true)
    private Integer jobInstanceId;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "enqueueDate")
    private Calendar enqueueDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "executionDate")
    private Calendar executionDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "endDate", nullable = true)
    private Calendar endDate;
    
    @Column(name = "userName", nullable = true)
    private String userName;
    
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = com.enioka.jqm.jpamodel.Node.class)
    @JoinColumn(name = "node")
    private Node node;
    
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "history_id")
    private List<JobHistoryParameter> parameters;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "parentJobId", nullable = true)
    private Integer parentJobId;
    
    @Column(length = 20, name = "status")
    @Enumerated(EnumType.STRING)
    private State status = State.SUBMITTED;
    
    @Column
    private String keyword1;
    
    @Column
    private String keyword2;
    
    @Column
    private String keyword3;
    
    @Column
    private String application;
    
    @Column
    private String module;
    
    @Column
    private Integer progress;

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
}
