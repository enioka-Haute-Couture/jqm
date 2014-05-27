package com.enioka.jqm.webui.admin.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QueueMappingDTO implements Serializable
{
    private static final long serialVersionUID = -5650890125510347623L;

    private Integer id;
    private Integer nodeId;
    private Integer nbThread;
    private Integer pollingInterval;
    private Integer queueId;
    private String nodeName, queueName;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Integer nodeId)
    {
        this.nodeId = nodeId;
    }

    public Integer getNbThread()
    {
        return nbThread;
    }

    public void setNbThread(Integer nbThread)
    {
        this.nbThread = nbThread;
    }

    public Integer getPollingInterval()
    {
        return pollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval)
    {
        this.pollingInterval = pollingInterval;
    }

    public Integer getQueueId()
    {
        return queueId;
    }

    public void setQueueId(Integer queueId)
    {
        this.queueId = queueId;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public String getQueueName()
    {
        return queueName;
    }

    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }
}
