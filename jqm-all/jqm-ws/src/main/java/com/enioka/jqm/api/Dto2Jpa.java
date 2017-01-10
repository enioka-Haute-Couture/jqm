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
package com.enioka.jqm.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;

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
import com.enioka.jqm.webui.admin.dto.JobDefDto;
import com.enioka.jqm.webui.admin.dto.NodeDto;
import com.enioka.jqm.webui.admin.dto.ParameterDto;
import com.enioka.jqm.webui.admin.dto.QueueDto;
import com.enioka.jqm.webui.admin.dto.QueueMappingDto;
import com.enioka.jqm.webui.admin.dto.RRoleDto;
import com.enioka.jqm.webui.admin.dto.RUserDto;

class Dto2Jpa
{
    private Dto2Jpa()
    {

    }

    @SuppressWarnings("unchecked")
    static <J> J setJpa(Object dto, EntityManager em)
    {
        if (dto instanceof JobDefDto)
        {
            return (J) setJpa(em, (JobDefDto) dto);
        }
        else if (dto instanceof GlobalParameterDto)
        {
            return (J) setJpa(em, (GlobalParameterDto) dto);
        }
        else if (dto instanceof JndiObjectResourceDto)
        {
            return (J) setJpa(em, (JndiObjectResourceDto) dto);
        }
        else if (dto instanceof QueueMappingDto)
        {
            return (J) setJpa(em, (QueueMappingDto) dto);
        }
        else if (dto instanceof QueueDto)
        {
            return (J) setJpa(em, (QueueDto) dto);
        }
        else if (dto instanceof RUserDto)
        {
            return (J) setJpa(em, (RUserDto) dto);
        }
        else if (dto instanceof RRoleDto)
        {
            return (J) setJpa(em, (RRoleDto) dto);
        }
        else if (dto instanceof NodeDto)
        {
            return (J) setJpa(em, (NodeDto) dto);
        }
        return null;
    }

    private static Node setJpa(EntityManager em, NodeDto dto)
    {
        Node n = null;

        if (dto.getId() == null)
        {
            n = new Node();
        }
        else
        {
            n = em.find(Node.class, dto.getId());
        }

        // Update or set fields
        n.setDns(dto.getDns());
        n.setDlRepo(dto.getOutputDirectory());
        n.setJmxRegistryPort(dto.getJmxRegistryPort());
        n.setJmxServerPort(dto.getJmxServerPort());
        n.setName(dto.getName());
        n.setPort(dto.getPort());
        n.setRepo(dto.getJobRepoDirectory());
        n.setRootLogLevel(dto.getRootLogLevel());
        n.setStop(dto.getStop());
        n.setLoadApiAdmin(dto.getLoadApiAdmin());
        n.setLoadApiClient(dto.getLoadApiClient());
        n.setLoapApiSimple(dto.getLoapApiSimple());
        n.setTmpDirectory(dto.getTmpDirectory());
        n.setEnabled(dto.getEnabled());

        // save
        n = em.merge(n);

        // Done
        return n;
    }

    private static GlobalParameter setJpa(EntityManager em, GlobalParameterDto dto)
    {
        GlobalParameter n = null;

        if (dto.getId() == null)
        {
            n = new GlobalParameter();
        }
        else
        {
            n = em.find(GlobalParameter.class, dto.getId());
        }

        // Update or set fields
        n.setKey(dto.getKey());
        n.setValue(dto.getValue());

        // save
        n = em.merge(n);

        // Done
        return n;
    }

    private static JobDef setJpa(EntityManager em, JobDefDto dto)
    {
        JobDef jpa = null;

        if (dto.getId() == null)
        {
            jpa = new JobDef();
        }
        else
        {
            jpa = em.find(JobDef.class, dto.getId());
        }

        jpa.setApplication(dto.getApplication());
        jpa.setApplicationName(dto.getApplicationName());
        jpa.setCanBeRestarted(dto.isCanBeRestarted());
        jpa.setDescription(dto.getDescription());
        jpa.setEnabled(dto.isEnabled());
        jpa.setHighlander(dto.isHighlander());
        jpa.setJarPath(dto.getJarPath());
        jpa.setJavaClassName(dto.getJavaClassName());
        jpa.setKeyword1(dto.getKeyword1());
        jpa.setKeyword2(dto.getKeyword2());
        jpa.setKeyword3(dto.getKeyword3());
        jpa.setModule(dto.getModule());
        jpa.setQueue(em.find(Queue.class, dto.getQueueId()));
        jpa.setMaxTimeRunning(dto.getReasonableRuntimeLimitMinute());
        jpa.setSpecificIsolationContext(dto.getSpecificIsolationContext());
        jpa.setHiddenJavaClasses(dto.getHiddenJavaClasses());
        jpa.setChildFirstClassLoader(dto.isChildFirstClassLoader());

        jpa = em.merge(jpa);

        List<JobDefParameter> prmFromBefore = new ArrayList<JobDefParameter>();
        List<JobDefParameter> prmNow = new ArrayList<JobDefParameter>();
        for (JobDefParameter ee : jpa.getParameters())
        {
            prmFromBefore.add(ee);
        }

        for (ParameterDto p : dto.getParameters())
        {
            if (p.getKey() == null || p.getKey().isEmpty() || p.getValue() == null || p.getValue().isEmpty())
            {
                continue;
            }

            JobDefParameter np = null;
            if (p.getId() == null)
            {
                np = new JobDefParameter();
            }
            else
            {
                np = em.find(JobDefParameter.class, p.getId());
            }
            np.setKey(p.getKey());
            np.setValue(p.getValue());
            jpa.getParameters().add(np);
            np = em.merge(np);
            prmNow.add(np);
        }

        // Remove parameters that are not present anymore
        before: for (JobDefParameter presentbefore : prmFromBefore)
        {
            for (JobDefParameter stillhere : prmNow)
            {
                if (stillhere.getId() == presentbefore.getId())
                {
                    continue before;
                }
            }
            jpa.getParameters().remove(presentbefore);
            em.remove(presentbefore);
        }

        return jpa;
    }

    private static JndiObjectResource setJpa(EntityManager em, JndiObjectResourceDto dto)
    {
        JndiObjectResource jpa = null;

        if (dto.getId() == null)
        {
            jpa = new JndiObjectResource();
        }
        else
        {
            jpa = em.find(JndiObjectResource.class, dto.getId());
        }

        jpa.setAuth(dto.getAuth());
        jpa.setDescription(dto.getDescription());
        jpa.setFactory(dto.getFactory());
        jpa.setName(dto.getName());
        jpa.setSingleton(dto.getSingleton());
        jpa.setType(dto.getType());
        jpa.setTemplate(dto.getTemplate());

        jpa = em.merge(jpa);

        List<JndiObjectResourceParameter> prmFromBefore = new ArrayList<JndiObjectResourceParameter>();
        List<JndiObjectResourceParameter> prmNow = new ArrayList<JndiObjectResourceParameter>();
        for (JndiObjectResourceParameter ee : jpa.getParameters())
        {
            prmFromBefore.add(ee);
        }

        for (ParameterDto p : dto.getParameters())
        {
            if (p.getKey() == null || p.getKey().isEmpty() || p.getValue() == null || p.getValue().isEmpty())
            {
                continue;
            }

            JndiObjectResourceParameter np = null;
            if (p.getId() == null)
            {
                np = new JndiObjectResourceParameter();
            }
            else
            {
                np = em.find(JndiObjectResourceParameter.class, p.getId());
            }
            np.setKey(p.getKey());
            np.setValue(p.getValue());
            np.setResource(jpa);
            np = em.merge(np);
            prmNow.add(np);
        }

        // Remove parameters that are not present anymore
        ml: for (JndiObjectResourceParameter presentbefore : prmFromBefore)
        {
            for (JndiObjectResourceParameter stillhere : prmNow)
            {
                if (stillhere.getId() == presentbefore.getId())
                {
                    continue ml;
                }
            }
            jpa.getParameters().remove(presentbefore);
            em.remove(presentbefore);
        }

        return jpa;
    }

    private static DeploymentParameter setJpa(EntityManager em, QueueMappingDto dto)
    {
        DeploymentParameter jpa = null;

        if (dto.getId() == null)
        {
            jpa = new DeploymentParameter();
        }
        else
        {
            jpa = em.find(DeploymentParameter.class, dto.getId());
        }

        // Update or set fields
        jpa.setNbThread(dto.getNbThread());
        jpa.setNode(em.find(Node.class, dto.getNodeId()));
        jpa.setPollingInterval(dto.getPollingInterval());
        jpa.setQueue(em.find(Queue.class, dto.getQueueId()));
        jpa.setEnabled(dto.getEnabled());

        // Save
        jpa = em.merge(jpa);

        // Done
        return jpa;
    }

    private static Queue setJpa(EntityManager em, QueueDto dto)
    {
        Queue jpa = null;

        if (dto.getId() == null)
        {
            jpa = new Queue();
        }
        else
        {
            jpa = em.find(Queue.class, dto.getId());
        }

        // Update or set fields
        jpa.setDefaultQueue(dto.isDefaultQueue());
        jpa.setDescription(dto.getDescription());
        jpa.setName(dto.getName());
        jpa.setTimeToLive(-1);

        // save
        jpa = em.merge(jpa);

        // Done
        return jpa;
    }

    private static RUser setJpa(EntityManager em, RUserDto dto)
    {
        RUser jpa = null;

        if (dto.getId() == null)
        {
            jpa = new RUser();
        }
        else
        {
            jpa = em.find(RUser.class, dto.getId());
        }

        jpa.setEmail(dto.getEmail());
        jpa.setExpirationDate(dto.getExpirationDate());
        jpa.setFreeText(dto.getFreeText());
        jpa.setLocked(dto.getLocked());
        jpa.setLogin(dto.getLogin());

        jpa = em.merge(jpa);

        RRole r = null;
        for (RRole ex : jpa.getRoles())
        {
            ex.getUsers().remove(jpa);
            // jpa.getRoles().remove(ex);
        }
        for (Integer rid : dto.getRoles())
        {
            r = em.find(RRole.class, rid);
            if (r == null)
            {
                throw new ErrorDto("Trying to associate an account with a non-existing role", "", 4, Status.BAD_REQUEST);
            }
            jpa.getRoles().add(r);
            r.getUsers().add(jpa);
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty())
        {
            ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
            jpa.setPassword(new Sha512Hash(dto.getNewPassword(), salt, 100000).toHex());
            jpa.setHashSalt(salt.toHex());
        }

        // Done
        return jpa;
    }

    private static RRole setJpa(EntityManager em, RRoleDto dto)
    {
        RRole jpa = null;

        if (dto.getId() == null)
        {
            jpa = new RRole();
        }
        else
        {
            jpa = em.find(RRole.class, dto.getId());
        }

        jpa.setName(dto.getName());
        jpa.setDescription(dto.getDescription());

        jpa = em.merge(jpa);

        for (RPermission perm : jpa.getPermissions())
        {
            em.remove(perm);
        }
        jpa.getPermissions().clear();
        for (String perm : dto.getPermissions())
        {
            RPermission jp = new RPermission();
            jp.setName(perm);
            jp.setRole(jpa);
            jpa.getPermissions().add(jp);
        }

        // Done
        return jpa;
    }

    static <J> void clean(J jpa, EntityManager em)
    {
        if (jpa instanceof JobDef)
        {
            if (0 != em.createQuery("SELECT COUNT(ji) FROM JobInstance ji WHERE ji.jd = :j", Long.class).setParameter("j", jpa)
                    .getSingleResult())
            {
                throw new JqmInvalidRequestException("Cannot remove a job definition used in currently queued or running job instances");
            }
            em.createQuery("UPDATE History h set h.jd = NULL where h.jd = :j").setParameter("j", jpa).executeUpdate();
        }
        else if (jpa instanceof Queue)
        {
            if (0 != em.createQuery("SELECT COUNT(jd) FROM JobDef jd WHERE jd.queue = :q", Long.class).setParameter("q", jpa)
                    .getSingleResult())
            {
                throw new JqmInvalidRequestException("Cannot remove a queue used in Job Definitions");
            }
            if (0 != em.createQuery("SELECT COUNT(ji) FROM JobInstance ji WHERE ji.queue = :q", Long.class).setParameter("q", jpa)
                    .getSingleResult())
            {
                throw new JqmInvalidRequestException("Cannot remove a queue used in currently queued or running job instance");
            }

            em.createQuery("UPDATE History h set h.queue = NULL where h.queue = :q").setParameter("q", jpa).executeUpdate();
            em.createQuery("DELETE FROM DeploymentParameter dp WHERE dp.queue = :q").setParameter("q", jpa).executeUpdate();
        }
        else if (jpa instanceof Node)
        {
            if (0 != em.createQuery("SELECT COUNT(ji) FROM JobInstance ji WHERE ji.node = :j", Long.class).setParameter("j", jpa)
                    .getSingleResult())
            {
                throw new JqmInvalidRequestException("Cannot remove a node used in currently running job instance");
            }
            Node n = (Node) jpa;
            Calendar limit = Calendar.getInstance();
            limit.add(Calendar.MINUTE, -10);
            if (n.getLastSeenAlive() != null && n.getLastSeenAlive().after(limit))
            {
                throw new JqmInvalidRequestException(
                        "Can only remove a node either properly shut down or that has crashed more than 10 minutes ago.");
            }

            em.createQuery("UPDATE History h set h.node = NULL where h.node = :n").setParameter("n", n).executeUpdate();
            em.createQuery("DELETE FROM DeploymentParameter dp where dp.node = :n").setParameter("n", n).executeUpdate();
        }
        else if (jpa instanceof RUser)
        {
            for (RRole r : em.createQuery("SELECT r FROM RRole r WHERE :u MEMBER OF r.users", RRole.class).setParameter("u", jpa)
                    .getResultList())
            {
                r.getUsers().remove(jpa);
            }
        }
        else if (jpa instanceof RRole)
        {
            if (0 != em.createQuery("SELECT COUNT(u) FROM RUser u WHERE :r MEMBER OF u.roles", Long.class).setParameter("r", jpa)
                    .getSingleResult())
            {
                throw new JqmInvalidRequestException("Cannot remove a role currently assigned to a user");
            }
        }

        // Other types do not have relationships needing cleaning up.
    }
}
