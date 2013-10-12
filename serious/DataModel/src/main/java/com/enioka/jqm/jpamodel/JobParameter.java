package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class JobParameter implements Serializable
{
	private static final long serialVersionUID = -8894511645365690426L;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=false, length=50)
	private String key;
	@Column(nullable=false, length=1000)
	private String value;
	@ManyToOne(fetch=FetchType.LAZY, targetEntity=com.enioka.jqm.jpamodel.JobDefinition.class)
	private JobInstance jobInstance;


	/**
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key)
	{
		this.key = key;
	}
	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	public Integer getId()
	{
		return id;
	}
	public void setId(Integer id)
	{
		this.id = id;
	}
	public JobInstance getJobInstance()
	{
		return jobInstance;
	}
	public void setJobInstance(JobInstance jobInstance)
	{
		this.jobInstance = jobInstance;
	}
}
