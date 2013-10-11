package com.enioka.jqm.jpamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


/**
 *
 * @author pierre.coppee
 */
@Entity
public class JobDefinition {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public Integer id;
	public boolean canBeRestarted = true;
	@Column(nullable=false, length=100)
	public String javaClassName;
	@Column(length=1000)
	public String filePath;
	@ManyToOne(optional=false)
	public Queue queue;
	public Integer maxTimeRunning;
	public String applicationName;
	public Integer sessionID;
	@Column(length=50)
	public String application;
	@Column(length=50)
	public String module;
	@Column(length=50)
	public String other1;
	@Column(length=50)
	public String other2;
	@Column(length=50)
	public String other3;
	public boolean highlander = false;
	@Column
    private String jarPath;


	public Integer getId()
	{
		return id;
	}

	public boolean isCanBeRestarted()
	{
		return canBeRestarted;
	}

	public void setCanBeRestarted(boolean canBeRestarted)
	{
		this.canBeRestarted = canBeRestarted;
	}

	public String getJavaClassName()
	{
		return javaClassName;
	}

	public void setJavaClassName(String javaClassName)
	{
		this.javaClassName = javaClassName;
	}

	public Integer getMaxTimeRunning()
	{
		return maxTimeRunning;
	}

	public void setMaxTimeRunning(Integer maxTimeRunning)
	{
		this.maxTimeRunning = maxTimeRunning;
	}

	public String getApplicationName()
	{
		return applicationName;
	}

	public void setApplicationName(String applicationName)
	{
		this.applicationName = applicationName;
	}

	public Integer getSessionID()
	{
		return sessionID;
	}

	public void setSessionID(Integer sessionID)
	{
		this.sessionID = sessionID;
	}

	public String getApplication()
	{
		return application;
	}

	public void setApplication(String application)
	{
		this.application = application;
	}

	public String getModule()
	{
		return module;
	}

	public void setModule(String module)
	{
		this.module = module;
	}

	public String getOther1()
	{
		return other1;
	}

	public void setOther1(String other1)
	{
		this.other1 = other1;
	}

	public String getOther2()
	{
		return other2;
	}

	public void setOther2(String other2)
	{
		this.other2 = other2;
	}

	public String getOther3()
	{
		return other3;
	}

	public void setOther3(String other3)
	{
		this.other3 = other3;
	}

	public boolean isHighlander()
	{
		return highlander;
	}

	public void setHighlander(boolean highlander)
	{
		this.highlander = highlander;
	}

	public Queue getQueue()
	{
		return queue;
	}

	public void setQueue(Queue queue)
	{
		this.queue = queue;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}


    public String getJarPath() {

    	return jarPath;
    }


    public void setJarPath(String jarPath) {

    	this.jarPath = jarPath;
    }
}
