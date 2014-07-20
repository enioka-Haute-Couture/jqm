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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * JPA persistence class for storing the execution requests. Said otherwise, <strong>this table holds the contents of the execution
 * queues</strong>.
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
    private String instanceApplication;

    @Column(length = 50, name = "module")
    private String instanceModule;

    @Column(length = 50, name = "keyword1")
    private String instanceKeyword1;

    @Column(length = 50, name = "keyword2")
    private String instanceKeyword2;

    @Column(length = 50, name = "keyword3")
    private String instanceKeyword3;

    /**
     * The place inside the queue, i.e. the number of job requests that will be run before this one can be run.
     */
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

    /**
     * Helper method to add a parameter without having to create it explicitely. The created parameter should be persisted afterwards.
     * 
     * @param key
     *            name of the parameter to add
     * @param value
     *            value of the parameter to create
     * @return the newly created parameter
     */
    public RuntimeParameter addParameter(String key, String value)
    {
        RuntimeParameter jp = new RuntimeParameter();
        jp.setJi(this.getId());
        jp.setKey(key);
        jp.setValue(value);
        return jp;
    }

    /**
     * A technical ID without any meaning. Generated by the database.
     */
    public int getId()
    {
        return id;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getSessionID()
    {
        return sessionID;
    }

    /**
     * The {@link JobDef} from which this {@link JobInstance} was instantiated.
     */
    public JobDef getJd()
    {
        return jd;
    }

    /**
     * See {@link #getJd()}
     */
    public void setJd(final JobDef jd)
    {
        this.jd = jd;
    }

    /**
     * The current status of the request. See {@link State}.
     */
    public State getState()
    {
        return state;
    }

    /**
     * See {@link #getState()}
     */
    public void setState(State state)
    {
        this.state = state;
    }

    /**
     * See {@link #getUserName()}
     */
    public void setUserName(final String user)
    {
        this.userName = user;
    }

    /**
     * See {@link #getSessionID()}
     */
    public void setSessionID(final String sessionID)
    {
        this.sessionID = sessionID;
    }

    /**
     * The {@link Queue} on which is the {@link JobInstance} should wait. Cannot be changed once the status is ATTRIBUTED (i.e. wait is
     * over).
     */
    public Queue getQueue()
    {
        return queue;
    }

    /**
     * See {@link #getQueue()}
     */
    public void setQueue(final Queue queue)
    {
        this.queue = queue;
    }

    /**
     * The node that is running the {@link JobInstance}. Null until wait is over and status is ATTRIBUTED.
     */
    public Node getNode()
    {
        return node;
    }

    /**
     * See {@link #getNode()}
     */
    public void setNode(final Node node)
    {
        this.node = node;
    }

    /**
     * Null by default. If specified, an e-mail will be sent to this address at run end.
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * See {@link #getEmail()}
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * User code may signal its progress through this integer. Purely optional.
     */
    public Integer getProgress()
    {
        return progress;
    }

    /**
     * See {@link #getProgress()}
     */
    public void setProgress(Integer progress)
    {
        this.progress = progress;
    }

    /**
     * Technical queue ordering field.
     */
    public double getInternalPosition()
    {
        return internalPosition;
    }

    /**
     * See {@link #getInternalPosition()}
     */
    public void setInternalPosition(double internalPosition)
    {
        this.internalPosition = internalPosition;
    }

    /**
     * Only set when a job request is created by a running job, in which case it contains the job {@link JobInstance} ID.
     */
    public Integer getParentId()
    {
        return parentId;
    }

    /**
     * See {@link #getParentId()}
     */
    public void setParentId(Integer parentId)
    {
        this.parentId = parentId;
    }

    /**
     * Time at which this {@link JobInstance} was committed inside the database.
     */
    public Calendar getCreationDate()
    {
        return creationDate;
    }

    /**
     * See {@link #getCreationDate()}
     */
    public void setCreationDate(Calendar creationDate)
    {
        this.creationDate = creationDate;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getApplication()
    {
        return instanceApplication;
    }

    /**
     * See {@link #getApplication()}
     */
    public void setApplication(String application)
    {
        this.instanceApplication = application;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getModule()
    {
        return instanceModule;
    }

    /**
     * See {@link #getModule()}
     */
    public void setModule(String module)
    {
        this.instanceModule = module;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getKeyword1()
    {
        return instanceKeyword1;
    }

    /**
     * See {@link #getKeyword1()}
     */
    public void setKeyword1(String keyword1)
    {
        this.instanceKeyword1 = keyword1;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getKeyword2()
    {
        return instanceKeyword2;
    }

    /**
     * See {@link #getKeyword2()}
     */
    public void setKeyword2(String keyword2)
    {
        this.instanceKeyword2 = keyword2;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getKeyword3()
    {
        return instanceKeyword3;
    }

    /**
     * See {@link #getKeyword3()}
     */
    public void setKeyword3(String keyword3)
    {
        this.instanceKeyword3 = keyword3;
    }

    void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * Time at which the execution request entered the RUNNING status - a few milliseconds before actual execution.
     */
    public Calendar getExecutionDate()
    {
        return executionDate;
    }

    /**
     * See {@link #getExecutionDate()}
     */
    public void setExecutionDate(Calendar executionDate)
    {
        this.executionDate = executionDate;
    }

    /**
     * Time at which the job execution request (the {@link JobInstance}) was taken by an engine.
     */
    public Calendar getAttributionDate()
    {
        return attributionDate;
    }

    /**
     * See {@link #getAttributionDate()}
     */
    public void setAttributionDate(Calendar attributionDate)
    {
        this.attributionDate = attributionDate;
    }
}
