package mcd;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class JobParameter
{
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String key;
	private String value;
	private Integer JobId;


	/**
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key)
	{
		this.key = key;
	}
	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	/**
	 * @return the jobId
	 */
	public Integer getJobId()
	{
		return JobId;
	}
	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(Integer jobId)
	{
		JobId = jobId;
	}
}
