package com.enioka.jqm.jpamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Node
{
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=false, length=1000)
	private String listeningInterface;
	@Column(nullable=false)
	private Integer port;
	@Column(nullable=false)
	private String dlRepo;

	public Integer getId()
	{
		return id;
	}
	public void setId(Integer id)
	{
		this.id = id;
	}
	public String getListeningInterface()
	{
		return listeningInterface;
	}
	public void setListeningInterface(String listeningInterface)
	{
		this.listeningInterface = listeningInterface;
	}
	public Integer getPort()
	{
		return port;
	}
	public void setPort(Integer port)
	{
		this.port = port;
	}

    public String getDlRepo() {

    	return dlRepo;
    }

    public void setDlRepo(String dlRepo) {

    	this.dlRepo = dlRepo;
    }
}
