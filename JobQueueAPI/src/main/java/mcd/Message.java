package mcd;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class Message {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private String textMessage;
	private Integer historyId;


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
	/**
	 * @return the historyId
	 */
	public Integer getHistoryId()
	{
		return historyId;
	}
	/**
	 * @param historyId the historyId to set
	 */
	public void setHistoryId(Integer historyId)
	{
		this.historyId = historyId;
	}
}
