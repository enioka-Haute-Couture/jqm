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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="DatabaseProp")
public class DatabaseProp implements Serializable
{

	/**
	 *
	 */
	private static final long serialVersionUID = 5286608402747360301L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Integer id;
	@Column(nullable = false)
	private String driver;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String url;
	@Column(nullable = false, name = "username")
	private String userName;
	@Column(nullable = false)
	private String pwd;

	public String getDriver()
	{
		return driver;
	}

	public void setDriver(final String driver)
	{
		this.driver = driver;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(final String url)
	{
		this.url = url;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(final String user)
	{
		this.userName = user;
	}

	public String getPwd()
	{
		return pwd;
	}

	public void setPwd(final String pwd)
	{

		this.pwd = pwd;
	}

	public Integer getId()
	{

		return id;
	}

	public String getName()
	{

		return name;
	}

	public void setName(final String name)
	{

		this.name = name;
	}
}
