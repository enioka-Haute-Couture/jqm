package com.enioka.jqm.jpamodel;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
@Embeddable
public class History implements Serializable{

	/**
     *
     */
    private static final long serialVersionUID = -5249529794213078668L;
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=false)
	private Integer returnedValue;
	private Calendar jobDate;
	@Column(length=1000)
	private String msg;
	@OneToMany(fetch=FetchType.EAGER, targetEntity=com.enioka.jqm.jpamodel.Message.class, cascade=CascadeType.ALL, mappedBy="history")
	private List<Message> messages;
	@OneToOne(fetch=FetchType.LAZY, targetEntity=com.enioka.jqm.jpamodel.JobInstance.class)
	private JobInstance jobInstance;
	private Calendar enqueueDate;
	private Calendar executionDate;
	private Calendar endDate;
	@OneToMany(orphanRemoval=true)
	@JoinColumn(name="history_parameter")
    private List<JobHistoryParameter> parameters;


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


    public List<JobHistoryParameter> getParameters() {

    	return parameters;
    }


    public void setParameters(List<JobHistoryParameter> parameters) {

    	this.parameters = parameters;
    }


    public List<Message> getMessages() {

    	return messages;
    }


    public void setMessages(List<Message> messages) {

    	this.messages = messages;
    }
}
