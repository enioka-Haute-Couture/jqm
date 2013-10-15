package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Embeddable
public class Message implements Serializable{

	/**
     *
     */
    private static final long serialVersionUID = 1234354709423602792L;
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(length=1000)
	private String textMessage;
	@ManyToOne(optional=false, cascade=CascadeType.ALL)
	@JoinColumn(name="message_history", nullable=false)
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
