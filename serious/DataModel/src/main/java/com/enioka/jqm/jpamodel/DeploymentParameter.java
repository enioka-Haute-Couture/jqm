package com.enioka.jqm.jpamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class DeploymentParameter
{
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=false)
	private Integer classId;
	@ManyToOne(fetch=FetchType.LAZY, targetEntity=com.enioka.jqm.jpamodel.Node.class)
	private Node node;
	@Column(nullable=false)
	private Integer nbThread;
	private Integer pollingInterval;
	@ManyToOne(targetEntity=com.enioka.jqm.jpamodel.Queue.class)
	private Queue queue;



	public Integer getId()
	{
		return id;
	}
	public void setId(Integer id)
	{
		this.id = id;
	}
	public Integer getClassId()
	{
		return classId;
	}
	public void setClassId(Integer classId)
	{
		this.classId = classId;
	}
	public void setNbThread(Integer nbThread)
	{
		this.nbThread = nbThread;
	}
	public Node getNode()
	{
		return node;
	}
	public void setNode(Node node)
	{
		this.node = node;
	}
	public Integer getPollingInterval()
	{
		return pollingInterval;
	}
	public void setPollingInterval(Integer pollingInterval)
	{
		this.pollingInterval = pollingInterval;
	}
	public Integer getNbThread()
	{
		return nbThread;
	}

    public Queue getQueue() {

    	return queue;
    }

    public void setQueue(Queue queue) {

    	this.queue = queue;
    }
}
