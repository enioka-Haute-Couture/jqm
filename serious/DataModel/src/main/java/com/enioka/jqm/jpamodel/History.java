package com.enioka.jqm.jpamodel;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class History {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=false)
	private Integer returnedValue;
	private Calendar jobDate;
	@Column(length=1000)
	private String msg;
	@ManyToOne(fetch=FetchType.EAGER, targetEntity=com.enioka.jqm.jpamodel.Message.class)
	private Message message;
	@OneToOne(fetch=FetchType.LAZY, targetEntity=com.enioka.jqm.jpamodel.JobInstance.class)
	private JobInstance jobInstance;
	private Calendar enqueueDate;
	private Calendar executionDate;
	private Calendar endDate;


	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}
	/**
	 * @return the returnedValue
	 */
	public Integer getReturnedValue()
	{
		return returnedValue;
	}
	/**
	 * @param returnedValue the returnedValue to set
	 */
	public void setReturnedValue(Integer returnedValue)
	{
		this.returnedValue = returnedValue;
	}
	/**
	 * @return the jobDate
	 */
	public Calendar getJobDate()
	{
		return jobDate;
	}
	/**
	 * @param jobDate the jobDate to set
	 */
	public void setJobDate(Calendar jobDate)
	{
		this.jobDate = jobDate;
	}
	/**
	 * @return the msg
	 */
	public String getMsg()
	{
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public Message getMessage()
	{
		return message;
	}

	public void setMessage(Message message)
	{
		this.message = message;
	}

	public JobInstance getJobInstance()
	{
		return jobInstance;
	}

	public void setJobInstance(JobInstance jobInstance)
	{
		this.jobInstance = jobInstance;
	}

	public Calendar getEnqueueDate()
	{
		return enqueueDate;
	}

	public void setEnqueueDate(Calendar enqueueDate)
	{
		this.enqueueDate = enqueueDate;
	}

	public Calendar getExecutionDate()
	{
		return executionDate;
	}

	public void setExecutionDate(Calendar executionDate)
	{
		this.executionDate = executionDate;
	}

	public Calendar getEndDate()
	{
		return endDate;
	}

	public void setEndDate(Calendar endDate)
	{
		this.endDate = endDate;
	}
}
