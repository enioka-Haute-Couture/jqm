package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "GlobalParameter")
public class GlobalParameter implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2619971486012565203L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(length=50, name="KEYNAME")
	private String key;
	@Column(length=1000, name="value")
	private String value;

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
	public Integer getId()
	{
		return id;
	}
}
