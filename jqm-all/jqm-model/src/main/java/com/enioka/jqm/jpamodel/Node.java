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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Node")
public class Node
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(nullable = false, length = 1000, name = "nodeName", unique = true)
	private String listeningInterface;
	@Column(nullable = false, name = "port")
	private Integer port;

	// Repo where the deliverables must be downloaded
	@Column(nullable = false, name = "dlRepo")
	private String dlRepo;

	// Repo where the jar repository and the pom repository must be relative
	@Column(nullable = false, name = "repo")
	private String repo;

	// To stop nicely the current node
	@Column(nullable = false, name = "stop")
	private boolean stop = false;

	// To set the log level
	@Column(name = "rootLogLevel")
	private String rootLogLevel = "DEBUG";

	public Integer getId()
	{
		return id;
	}

	public void setId(final Integer id)
	{
		this.id = id;
	}

	public String getListeningInterface()
	{
		return listeningInterface;
	}

	public void setListeningInterface(final String listeningInterface)
	{
		this.listeningInterface = listeningInterface;
	}

	public Integer getPort()
	{
		return port;
	}

	public void setPort(final Integer port)
	{
		this.port = port;
	}

	public String getDlRepo()
	{

		return dlRepo;
	}

	public void setDlRepo(final String dlRepo)
	{

		this.dlRepo = dlRepo;
	}

	public String getRepo()
	{
		return repo;
	}

	public void setRepo(final String repo)
	{
		this.repo = repo;
	}

	public boolean isStop()
	{
		return stop;
	}

	public void setStop(boolean stop)
	{
		this.stop = stop;
	}

	public String getRootLogLevel()
	{
		return rootLogLevel;
	}

	public void setRootLogLevel(String rootLogLevel)
	{
		this.rootLogLevel = rootLogLevel;
	}
}
