package mcd;

import java.util.Calendar;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class History {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private Integer returnedValue;
	private Calendar jobDate;
	private String msg;
	private Integer msgId;
	private Integer jobId;
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
	/**
	 * @return the msgId
	 */
	public Integer getMsgId()
	{
		return msgId;
	}
	/**
	 * @param msgId the msgId to set
	 */
	public void setMsgId(Integer msgId)
	{
		this.msgId = msgId;
	}
	/**
	 * @return the jobId
	 */
	public Integer getJobId()
	{
		return jobId;
	}
	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(Integer jobId)
	{
		this.jobId = jobId;
	}
}
