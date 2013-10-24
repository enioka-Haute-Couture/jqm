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

@Entity
public class JndiObjectResource implements Serializable
{
	private static final long serialVersionUID = 5387852232057745693L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	// JNDI alias. JQM only allows "context/resource" aliases. E.g.: jms/myqueueconnectionfactory.
	@Column(nullable = false, length = 100)
	private String name;

	// Not used in JQM. Here for completion sake. (Possible values: Container, ?)
	@Column(nullable = true, length = 20)
	private String auth = null;

	// Class name of the requested resource. E.g.: com.ibm.mq.jms.MQQueueConnectionFactory
	@Column(nullable = false, length = 100)
	private String type;

	// Class name of the factory which will create the resource. JQM only allows resources with a factory implementing ObjectFactory. (no
	// singleton and other resources). E.g.: com.ibm.mq.jms.MQQueueConnectionFactoryFactory
	@Column(nullable = false, length = 100)
	private String factory;

	// A free text description
	@Column(nullable = true, length = 250)
	private String description;

	// The parameters. These are specific to each Object type. (e.g. for MQSeries: HOST, PORT, CHAN, TRAN, QMGR, ...)
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "resource")
	private Collection<JndiObjectResourceParameter> parameters = new ArrayList<JndiObjectResourceParameter>();

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAuth()
	{
		return auth;
	}

	public void setAuth(String auth)
	{
		this.auth = auth;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getFactory()
	{
		return factory;
	}

	public void setFactory(String factory)
	{
		this.factory = factory;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Collection<JndiObjectResourceParameter> getParameters()
	{
		return parameters;
	}

	public void setParameters(Collection<JndiObjectResourceParameter> parameters)
	{
		this.parameters = parameters;
	}
}
