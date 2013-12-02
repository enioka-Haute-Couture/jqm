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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 
 * @author pierre.coppee
 */
@Entity
@Table(name = "JobInstance")
public class JobInstance implements Serializable
{

	private static final long serialVersionUID = -7710486847228806301L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "jd_id")
	private JobDef jd;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private JobInstance parent;
	@Column(length = 50, name = "username")
	private String userName;
	@Column(name = "sessionId")
	private String sessionID;
	@Column(length = 50, name = "state")
	private String state;
	@ManyToOne(targetEntity = com.enioka.jqm.jpamodel.Queue.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "queue_id")
	private Queue queue;
	@ManyToOne(targetEntity = com.enioka.jqm.jpamodel.Node.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "node_id")
	private Node node;
	@Column(name = "sendEmail")
	private String email;
	@Column(name = "progress")
	private Integer progress;
	@Column(name = "internalPosition", nullable = false)
	private double internalPosition;

	@OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "jobinstance")
	private List<JobParameter> parameters;

	public int getCurrentPosition(EntityManager em)
	{
		if (this.state.equals("SUBMITTED"))
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

	public String getState()
	{
		return state;
	}

	public void setState(final String state)
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

	public JobInstance getParent()
	{
		return parent;
	}

	public void setParent(final JobInstance parent)
	{
		this.parent = parent;
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
}
