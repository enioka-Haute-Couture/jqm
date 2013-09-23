package mcd;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class nodeQueue {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private Integer queueId;
	private Integer nodeId;
	private Integer nbThread;


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
	 * @return the queueId
	 */
	public Integer getQueueId()
	{
		return queueId;
	}
	/**
	 * @param queueId the queueId to set
	 */
	public void setQueueId(Integer queueId)
	{
		this.queueId = queueId;
	}
	/**
	 * @return the nodeId
	 */
	public Integer getNodeId()
	{
		return nodeId;
	}
	/**
	 * @param nodeId the nodeId to set
	 */
	public void setNodeId(Integer nodeId)
	{
		this.nodeId = nodeId;
	}
	/**
	 * @return the nbThread
	 */
	public Integer getNbThread()
	{
		return nbThread;
	}
	/**
	 * @param nbThread the nbThread to set
	 */
	public void setNbThread(Integer nbThread)
	{
		this.nbThread = nbThread;
	}
}
