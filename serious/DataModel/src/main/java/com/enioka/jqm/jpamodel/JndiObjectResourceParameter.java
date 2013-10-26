package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class JndiObjectResourceParameter implements Serializable
{
	private static final long serialVersionUID = -8023896508793524111L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	// Name of the parameter. E.g.: QMGR for the queue manager name with MQSeries.
	@Column(nullable = false, length = 50, name = "KEYNAME")
	private String key;

	// Value. E.g.: QM.POUET
	@Column(nullable = false, length = 250)
	private String value;

	// Field for the reverse relationship towards the ObjectResource holding the parameter.
	@ManyToOne
	@JoinColumn(name = "resource_id")
	private JndiObjectResource resource;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public JndiObjectResource getResource()
	{
		return resource;
	}

	public void setResource(JndiObjectResource resource)
	{
		this.resource = resource;
	}
}
