package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


/**
 *
 * @author pierre.coppee
 */

@Entity
public class Queue implements Serializable{

	/**
     *
     */
    private static final long serialVersionUID = 4677042929807285233L;
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	@Column(nullable=false, length=50)
    private String name;
	@Column(nullable=false, length=1000)
    private String description;
	@Column(nullable=false)
    private Integer maxTempInQueue;
	@Column(nullable=false)
    private Integer maxTempRunning;
    private boolean defaultQueue;


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public Integer getMaxTempInQueue()
	{
		return maxTempInQueue;
	}

	public void setMaxTempInQueue(Integer maxTempInQueue)
	{
		this.maxTempInQueue = maxTempInQueue;
	}

	public Integer getMaxTempRunning()
	{
		return maxTempRunning;
	}

	public void setMaxTempRunning(Integer maxTempRunning)
	{
		this.maxTempRunning = maxTempRunning;
	}

	public boolean isDefaultQueue()
	{
		return defaultQueue;
	}

	public void setDefaultQueue(boolean defaultQueue)
	{
		this.defaultQueue = defaultQueue;
	}
}
