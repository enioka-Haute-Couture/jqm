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
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="JndiObjectResource")
public class JndiObjectResource implements Serializable
{
	private static final long serialVersionUID = 5387852232057745693L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	// JNDI alias. JQM only allows "context/resource" aliases. E.g.: jms/myqueueconnectionfactory.
	@Column(nullable = false, length = 100, name="name", unique = true)
	private String name;

	// Not used in JQM. Here for completion sake. (Possible values: Container, ?)
	@Column(nullable = true, length = 20, name="auth")
	private String auth = null;

	// Class name of the requested resource. E.g.: com.ibm.mq.jms.MQQueueConnectionFactory
	@Column(nullable = false, length = 100, name="type")
	private String type;

	// Class name of the factory which will create the resource. JQM only allows resources with a factory implementing ObjectFactory. (no
	// singleton and other resources). E.g.: com.ibm.mq.jms.MQQueueConnectionFactoryFactory
	@Column(nullable = false, length = 100, name="factory")
	private String factory;

	// A free text description
	@Column(nullable = true, length = 250, name="description")
	private String description;

	// The parameters. These are specific to each Object type. (e.g. for MQSeries: HOST, PORT, CHAN, TRAN, QMGR, ...)
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "resource")
	//	@Column(name="parameters")
	private Collection<JndiObjectResourceParameter> parameters = new ArrayList<JndiObjectResourceParameter>();

	public int getId()
	{
		return id;
	}

	public void setId(final int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getAuth()
	{
		return auth;
	}

	public void setAuth(final String auth)
	{
		this.auth = auth;
	}

	public String getType()
	{
		return type;
	}

	public void setType(final String type)
	{
		this.type = type;
	}

	public String getFactory()
	{
		return factory;
	}

	public void setFactory(final String factory)
	{
		this.factory = factory;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public Collection<JndiObjectResourceParameter> getParameters()
	{
		return parameters;
	}

	public void setParameters(final Collection<JndiObjectResourceParameter> parameters)
	{
		this.parameters = parameters;
	}
}
