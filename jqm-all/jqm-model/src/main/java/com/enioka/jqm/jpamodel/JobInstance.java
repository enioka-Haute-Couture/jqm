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
import javax.persistence.EntityManager;
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

import org.hibernate.annotations.Index;

/**
 * 
 * @author pierre.coppee
 */
@Entity
@Table(name = "JobInstance")
@org.hibernate.annotations.Table(indexes = @Index(name = "idx_lock_jobinstance_2", columnNames = { "jd_id", "state" }), appliesTo = "JobInstance")
public class JobInstance implements Serializable
{

    private static final long serialVersionUID = -7710486847228806301L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "jd_id")
    private JobDef jd;

    @Column(name = "parentId")
    private Integer parentId;

    @Column(length = 50, name = "username")
    private String userName;

    @Column(name = "sessionId")
    private String sessionID;

    @Column(length = 50, name = "state")
    @Enumerated(EnumType.STRING)
    @Index(name = "idx_lock_jobinstance_1")
    private State state;

    @ManyToOne(targetEntity = com.enioka.jqm.jpamodel.Queue.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_id")
    @Index(name = "idx_lock_jobinstance_1")
    private Queue queue;

    @ManyToOne(targetEntity = com.enioka.jqm.jpamodel.Node.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id")
    private Node node;

    @Column(name = "sendEmail")
    private String email;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "internalPosition", nullable = false)
    private double internalPosition;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creationDate")
    private Calendar creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "attributionDate")
    private Calendar attributionDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "executionDate")
    private Calendar executionDate;

    @Column(length = 50, name = "application")
    private String application;

    @Column(length = 50, name = "module")
    private String module;

    @Column(length = 50, name = "keyword1")
    private String keyword1;

    @Column(length = 50, name = "keyword2")
    private String keyword2;

    @Column(length = 50, name = "keyword3")
    private String keyword3;

    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST }, mappedBy = "jobInstance")
    private List<JobParameter> parameters;

    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST }, mappedBy = "jobInstance")
    private List<MessageJi> messages;

    public int getCurrentPosition(EntityManager em)
    {
        if (this.state.equals(State.SUBMITTED))
        {
            return em
                    .createQuery("SELECT COUNT(ji) FROM JobInstance ji WHERE ji.internalPosition < :p AND ji.state = 'SUBMITTED'",
                            Long.class).setParameter("p", this.internalPosition).getSingleResult().intValue() + 1;
        }
        else
        {
            return 0;
        }
    }

    public JobParameter addParameter(String key, String value)
    {
        JobParameter jp = new JobParameter();
        jp.setJobinstance(this);
        jp.setKey(key);
        jp.setValue(value);
        return jp;
    }

    public int getId()
    {
        return id;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getSessionID()
    {
        return sessionID;
    }

    public JobDef getJd()
    {
        return jd;
    }

    public void setJd(final JobDef jd)
    {
        this.jd = jd;
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public void setUserName(final String user)
    {
        this.userName = user;
    }

    public void setSessionID(final String sessionID)
    {
        this.sessionID = sessionID;
    }

    public Queue getQueue()
    {

        return queue;
    }

    public void setQueue(final Queue queue)
    {

        this.queue = queue;
    }

    public List<JobParameter> getParameters()
    {

        return parameters;
    }

    public void setParameters(final List<JobParameter> parameters)
    {

        this.parameters = parameters;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode(final Node node)
    {
        this.node = node;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Integer getProgress()
    {
        return progress;
    }

    public void setProgress(Integer progress)
    {
        this.progress = progress;
    }

    public double getInternalPosition()
    {
        return internalPosition;
    }

    public void setInternalPosition(double internalPosition)
    {
        this.internalPosition = internalPosition;
    }

    public Integer getParentId()
    {
        return parentId;
    }

    public void setParentId(Integer parentId)
    {
        this.parentId = parentId;
    }

    public Calendar getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate)
    {
        this.creationDate = creationDate;
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

    public List<MessageJi> getMessages()
    {
        return messages;
    }

    public void setMessages(List<MessageJi> messages)
    {
        this.messages = messages;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Calendar getExecutionDate()
    {
        return executionDate;
    }

    public void setExecutionDate(Calendar executionDate)
    {
        this.executionDate = executionDate;
    }

    public Calendar getAttributionDate()
    {
        return attributionDate;
    }

    public void setAttributionDate(Calendar attributionDate)
    {
        this.attributionDate = attributionDate;
    }
}
