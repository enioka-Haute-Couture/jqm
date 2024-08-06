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
package com.enioka.api.admin;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * A mapping makes a {@link NodeDto} poll a {@link QueueDto}.
 */
@XmlRootElement
public class QueueMappingDto implements Serializable
{
    private static final long serialVersionUID = -5650890125510347623L;

    private Long id;
    private Long nodeId;
    private Integer nbThread;
    private Integer pollingInterval;
    private Long queueId;
    private String nodeName, queueName;
    private Boolean enabled = true;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
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

    public Long getQueueId()
    {
        return queueId;
    }

    public void setQueueId(Long queueId)
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
