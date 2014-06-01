package com.enioka.jqm.webui.admin.service;

import java.util.ArrayList;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JndiObjectResourceParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.RPermission;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.jpamodel.RUser;
import com.enioka.jqm.webui.admin.dto.GlobalParameterDto;
import com.enioka.jqm.webui.admin.dto.JndiObjectResourceDto;
import com.enioka.jqm.webui.admin.dto.ParameterDto;
import com.enioka.jqm.webui.admin.dto.JobDefDto;
import com.enioka.jqm.webui.admin.dto.NodeDTO;
import com.enioka.jqm.webui.admin.dto.QueueDTO;
import com.enioka.jqm.webui.admin.dto.QueueMappingDTO;
import com.enioka.jqm.webui.admin.dto.RRoleDto;
import com.enioka.jqm.webui.admin.dto.RUserDto;

@SuppressWarnings("unchecked")
public class Jpa2Dto
{
    public static <D> D getDTO(Object o)
    {
        if (o instanceof JobDef)
        {
            return (D) getDTO((JobDef) o);
        }
        else if (o instanceof GlobalParameter)
        {
            return (D) getDTO((GlobalParameter) o);
        }
        else if (o instanceof Node)
        {
            return (D) getDTO((Node) o);
        }
        else if (o instanceof Queue)
        {
            return (D) getDTO((Queue) o);
        }
        else if (o instanceof DeploymentParameter)
        {
            return (D) getDTO((DeploymentParameter) o);
        }
        else if (o instanceof JndiObjectResource)
        {
            return (D) getDTO((JndiObjectResource) o);
        }
        else if (o instanceof RUser)
        {
            return (D) getDTO((RUser) o);
        }
        else if (o instanceof RRole)
        {
            return (D) getDTO((RRole) o);
        }

        return null;
    }

    private static NodeDTO getDTO(Node n)
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

    private static QueueDTO getDTO(Queue q)
    {
        QueueDTO res = new QueueDTO();
        res.setDefaultQueue(q.isDefaultQueue());
        res.setDescription(q.getDescription());
        res.setId(q.getId());
        res.setName(q.getName());

        return res;
    }

    private static QueueMappingDTO getDTO(DeploymentParameter s)
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

    private static JndiObjectResourceDto getDTO(JndiObjectResource s)
    {
        JndiObjectResourceDto res = new JndiObjectResourceDto();
        res.setAuth(s.getAuth());
        res.setDescription(s.getDescription());
        res.setFactory(s.getFactory());
        res.setId(s.getId());
        res.setName(s.getName());
        res.setSingleton(s.getSingleton());
        res.setType(s.getType());

        res.setParameters(new ArrayList<ParameterDto>());
        for (JndiObjectResourceParameter p : s.getParameters())
        {
            res.getParameters().add(new ParameterDto(p.getId(), p.getKey(), p.getValue()));
        }

        return res;
    }

    private static GlobalParameterDto getDTO(GlobalParameter s)
    {
        GlobalParameterDto res = new GlobalParameterDto();
        res.setId(s.getId());
        res.setKey(s.getKey());
        res.setValue(s.getValue());

        return res;
    }

    private static JobDefDto getDTO(JobDef d)
    {
        JobDefDto res = new JobDefDto();
        res.setId(d.getId());
        res.setApplication(d.getApplication());
        res.setApplicationName(d.getApplicationName());
        res.setCanBeRestarted(d.isCanBeRestarted());
        res.setDescription(d.getDescription());
        res.setHighlander(d.isHighlander());
        res.setJarPath(d.getJarPath());
        res.setJavaClassName(d.getJavaClassName());
        res.setKeyword1(d.getKeyword1());
        res.setKeyword2(d.getKeyword2());
        res.setKeyword3(d.getKeyword3());
        res.setModule(d.getModule());
        res.setQueueId(d.getQueue().getId());

        for (JobDefParameter p : d.getParameters())
        {
            res.getParameters().add(new ParameterDto(p.getId(), p.getKey(), p.getValue()));
        }

        return res;
    }

    private static RUserDto getDTO(RUser d)
    {
        RUserDto res = new RUserDto();
        res.setCertificateThumbprint(d.getCertificateThumbprint());
        res.setCreationDate(d.getCreationDate());
        res.setExpirationDate(d.getExpirationDate());
        res.setId(d.getId());
        res.setLocked(d.getLocked());
        res.setLogin(d.getLogin());
        res.setNewPassword(null);
        res.setFreeText(d.getFreeText());
        res.setEmail(d.getEmail());

        for (RRole r : d.getRoles())
        {
            res.getRoles().add(r.getId());
        }

        return res;
    }

    private static RRoleDto getDTO(RRole d)
    {
        RRoleDto res = new RRoleDto();
        res.setDescription(d.getDescription());
        res.setId(d.getId());
        res.setName(d.getName());

        for (RPermission p : d.getPermissions())
        {
            res.getPermissions().add(p.getName());
        }

        return res;
    }
}
