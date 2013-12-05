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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="DeploymentParameter")
public class DeploymentParameter
{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=true, name="classId")
	private Integer classId;
	@ManyToOne(fetch=FetchType.LAZY, targetEntity=com.enioka.jqm.jpamodel.Node.class)
	@JoinColumn(name="node", nullable=false)
	private Node node;
	@Column(nullable=false, name="nbThread")
	private Integer nbThread;
	@Column(name="pollingInterval", nullable=false)
	private Integer pollingInterval;
	@ManyToOne(targetEntity=com.enioka.jqm.jpamodel.Queue.class)
	@JoinColumn(name="queue", nullable=false)
	private Queue queue;



	public Integer getId()
	{
		return id;
	}
	public void setId(final Integer id)
	{
		this.id = id;
	}
	public Integer getClassId()
	{
		return classId;
	}
	public void setClassId(final Integer classId)
	{
		this.classId = classId;
	}
	public void setNbThread(final Integer nbThread)
	{
		this.nbThread = nbThread;
	}
	public Node getNode()
	{
		return node;
	}
	public void setNode(final Node node)
	{
		this.node = node;
	}
	public Integer getPollingInterval()
	{
		return pollingInterval;
	}
	public void setPollingInterval(final Integer pollingInterval)
	{
		this.pollingInterval = pollingInterval;
	}
	public Integer getNbThread()
	{
		return nbThread;
	}

	public Queue getQueue() {

		return queue;
	}

	public void setQueue(final Queue queue) {

		this.queue = queue;
	}
}
