package com.enioka.jqm.webui.admin.dto;

import java.util.ArrayList;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JndiObjectResourceParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;

public class Frontier
{

    public static NodeDTO getDTO(Node n)
    {
        NodeDTO res = new NodeDTO();
        res.setDns(n.getDns());
        res.setId(n.getId());
        res.setJmxRegistryPort(n.getJmxRegistryPort());
        res.setJmxServerPort(n.getJmxServerPort());
        res.setJobRepoDirectory(n.getRepo());
        res.setLastSeenAlive(n.getLastSeenAlive());
        res.setName(n.getName());
        res.setOutputDirectory(n.getDlRepo());
        res.setPort(n.getPort());
        res.setRootLogLevel(n.getRootLogLevel());

        return res;
    }

    public static QueueDTO getDTO(Queue q)
    {
        QueueDTO res = new QueueDTO();
        res.setDefaultQueue(q.isDefaultQueue());
        res.setDescription(q.getDescription());
        res.setId(q.getId());
        res.setName(q.getName());

        return res;
    }

    public static QueueMappingDTO getDTO(DeploymentParameter s)
    {
        QueueMappingDTO res = new QueueMappingDTO();
        res.setId(s.getId());
        res.setNbThread(s.getNbThread());
        res.setNodeId(s.getNode().getId());
        res.setPollingInterval(s.getPollingInterval());
        res.setQueueId(s.getQueue().getId());
        res.setQueueName(s.getQueue().getName());
        res.setNodeName(s.getNode().getName());

        return res;
    }

    public static JndiObjectResourceDto getDTO(JndiObjectResource s)
    {
        JndiObjectResourceDto res = new JndiObjectResourceDto();
        res.setAuth(s.getAuth());
        res.setDescription(s.getDescription());
        res.setFactory(s.getFactory());
        res.setId(s.getId());
        res.setName(s.getName());
        res.setSingleton(s.getSingleton());
        res.setType(s.getType());

        res.setParameters(new ArrayList<JndiObjectResourcePrmDto>());
        for (JndiObjectResourceParameter p : s.getParameters())
        {
            res.getParameters().add(new JndiObjectResourcePrmDto(p.getId(), p.getKey(), p.getValue()));
        }

        return res;
    }

    public static GlobalParameterDto getDTO(GlobalParameter s)
    {
        GlobalParameterDto res = new GlobalParameterDto();
        res.setId(s.getId());
        res.setKey(s.getKey());
        res.setValue(s.getValue());

        return res;
    }
}
