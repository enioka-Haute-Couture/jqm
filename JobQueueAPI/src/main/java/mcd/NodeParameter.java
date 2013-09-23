package mcd;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class NodeParameter {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private String nodeListeningInterface;
	private String port;
	private Integer pollingBase;


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
	 * @return the nodeListeningInterface
	 */
	public String getNodeListeningInterface()
	{
		return nodeListeningInterface;
	}
	/**
	 * @param nodeListeningInterface the nodeListeningInterface to set
	 */
	public void setNodeListeningInterface(String nodeListeningInterface)
	{
		this.nodeListeningInterface = nodeListeningInterface;
	}
	/**
	 * @return the port
	 */
	public String getPort()
	{
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(String port)
	{
		this.port = port;
	}
	/**
	 * @return the pollingBase
	 */
	public Integer getPollingBase()
	{
		return pollingBase;
	}
	/**
	 * @param pollingBase the pollingBase to set
	 */
	public void setPollingBase(Integer pollingBase)
	{
		this.pollingBase = pollingBase;
	}
}
