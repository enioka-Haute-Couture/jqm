/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
	@Temporal(TemporalType.DATE)
	@Column(name = "jobDate")
	private Calendar jobDate;
	@JoinColumn(name = "jd")
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = com.enioka.jqm.jpamodel.JobDef.class)
	private JobDef jd;
	@Column(name = "sessionId")
	private Integer sessionId;
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = com.enioka.jqm.jpamodel.Queue.class)
	@JoinColumn(name = "queue")
	private Queue queue;
	@Column(length = 1000, name="msg")
	private String msg;
	@OneToMany(fetch = FetchType.EAGER, targetEntity = com.enioka.jqm.jpamodel.Message.class, cascade = CascadeType.ALL, mappedBy = "history")
	private List<Message> messages;
	@OneToOne(fetch = FetchType.LAZY, targetEntity = com.enioka.jqm.jpamodel.JobInstance.class)
	@JoinColumn(name = "jobInstance")
	private JobInstance jobInstance;
	@Temporal(TemporalType.DATE)
	@Column(name = "enqueueDate")
	private Calendar enqueueDate;
	@Temporal(TemporalType.DATE)
	@Column(name = "executionDate")
	private Calendar executionDate;
	@Temporal(TemporalType.DATE)
	@Column(name = "endDate")
	private Calendar endDate;
	@Column(name = "userName")
	private String userName;
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = com.enioka.jqm.jpamodel.Node.class)
	@JoinColumn(name = "node")
	private Node node;
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "history_id")
	private List<JobHistoryParameter> parameters;
	@Column(name = "position")
	private Integer position;
	@Column(name = "email")
	private String email;
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = com.enioka.jqm.jpamodel.JobDef.class)
	@JoinColumn(name = "parent")
	private JobDef parent;

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

	/**
	 * @return the jobDate
	 */
	public Calendar getJobDate()
	{
		return jobDate;
	}

	/**
	 * @param jobDate
	 *            the jobDate to set
	 */
	public void setJobDate(final Calendar jobDate)
	{
		this.jobDate = jobDate;
	}

	/**
	 * @return the msg
	 */
	public String getMsg()
	{
		return msg;
	}

	/**
	 * @param msg
	 *            the msg to set
	 */
	public void setMsg(final String msg)
	{
		this.msg = msg;
	}

	public JobInstance getJobInstance()
	{
		return jobInstance;
	}

	public void setJobInstance(final JobInstance jobInstance)
	{
		this.jobInstance = jobInstance;
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

	public Integer getSessionId()
	{
		return sessionId;
	}

	public void setSessionId(final Integer sessionId)
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

	public Integer getPosition()
	{
		return position;
	}

	public void setPosition(Integer position)
	{
		this.position = position;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public JobDef getParent()
	{
		return parent;
	}

	public void setParent(JobDef parent)
	{
		this.parent = parent;
	}
}
