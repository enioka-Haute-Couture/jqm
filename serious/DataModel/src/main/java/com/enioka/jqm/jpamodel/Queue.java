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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 
 * @author pierre.coppee
 */

@Entity
@Table(name = "Queue")
public class Queue implements Serializable
{
	private static final long serialVersionUID = 4677042929807285233L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Column(nullable = false, length = 50, name="name")
	private String name;
	@Column(nullable = false, length = 1000, name="description")
	private String description;
	@Column(nullable = false, name="maxTempInQueue")
	private Integer maxTempInQueue;
	@Column(nullable = false, name="maxTempRunning")
	private Integer maxTempRunning;
	@Column(name="defaultQueue")
	private boolean defaultQueue;

	@OneToMany(mappedBy="queue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Column(name="jobdefs")
	private List<JobDef> jobdefs = new ArrayList<JobDef>();

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(final String description)
	{
		this.description = description;
	}

	public int getId()
	{
		return id;
	}

	public void setId(final int id)
	{
		this.id = id;
	}

	public Integer getMaxTempInQueue()
	{
		return maxTempInQueue;
	}

	public void setMaxTempInQueue(final Integer maxTempInQueue)
	{
		this.maxTempInQueue = maxTempInQueue;
	}

	public Integer getMaxTempRunning()
	{
		return maxTempRunning;
	}

	public void setMaxTempRunning(final Integer maxTempRunning)
	{
		this.maxTempRunning = maxTempRunning;
	}

	public boolean isDefaultQueue()
	{
		return defaultQueue;
	}

	public void setDefaultQueue(final boolean defaultQueue)
	{
		this.defaultQueue = defaultQueue;
	}
}
