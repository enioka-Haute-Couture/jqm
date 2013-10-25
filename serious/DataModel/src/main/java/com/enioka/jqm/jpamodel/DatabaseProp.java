package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class DatabaseProp implements Serializable
{

	/**
     *
     */
	private static final long serialVersionUID = 5286608402747360301L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Integer id;
	@Column(nullable = false)
	private String driver;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String url;
	@Column(nullable = false, name = "username")
	private String userName;
	@Column(nullable = false)
	private String pwd;

	public String getDriver()
	{
		return driver;
	}

	public void setDriver(String driver)
	{
		this.driver = driver;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String user)
	{
		this.userName = user;
	}

	public String getPwd()
	{
		return pwd;
	}

	public void setPwd(String pwd)
	{

		this.pwd = pwd;
	}

	public Integer getId()
	{

		return id;
	}

	public String getName()
	{

		return name;
	}

	public void setName(String name)
	{

		this.name = name;
	}
}
