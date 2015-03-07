/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enioka.jqm.webui.admin.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QueueMappingDto implements Serializable
{
    private static final long serialVersionUID = -5650890125510347623L;

    private Integer id;
    private Integer nodeId;
    private Integer nbThread;
    private Integer pollingInterval;
    private Integer queueId;
    private String nodeName, queueName;
    private Boolean enabled = true;

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

    public Boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }
}
