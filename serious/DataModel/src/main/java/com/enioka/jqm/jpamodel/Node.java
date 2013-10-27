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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Node
{
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=false, length=1000)
	private String listeningInterface;
	@Column(nullable=false)
	private Integer port;

	// Repo where the deliverables must be downloaded
	@Column(nullable=false)
	private String dlRepo;

	// Repo where the jar repository and the pom repository must be relative
	@Column(nullable=false)
	private String repo;

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

	public String getDlRepo() {

		return dlRepo;
	}

	public void setDlRepo(final String dlRepo) {

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
}
