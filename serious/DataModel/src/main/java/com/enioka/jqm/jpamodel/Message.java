package com.enioka.jqm.jpamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Message {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(length=1000)
	private String textMessage;
	@ManyToOne(optional=false)
	private History history;


	/**
	 * @return the id
	 */
	public Integer getId()
	{
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id)
	{
		this.id = id;
	}
	/**
	 * @return the textMessage
	 */
	public String getTextMessage()
	{
		return textMessage;
	}
	/**
	 * @param textMessage the textMessage to set
	 */
	public void setTextMessage(String textMessage)
	{
		this.textMessage = textMessage;
	}
	public History getHistory()
	{
		return history;
	}
	public void setHistory(History history)
	{
		this.history = history;
	}
}
