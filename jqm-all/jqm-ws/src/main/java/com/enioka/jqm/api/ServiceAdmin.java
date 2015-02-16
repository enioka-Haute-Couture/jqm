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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.RPermission;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.jpamodel.RUser;
import com.enioka.jqm.pki.JpaCa;
import com.enioka.jqm.webui.admin.dto.GlobalParameterDto;
import com.enioka.jqm.webui.admin.dto.JndiObjectResourceDto;
import com.enioka.jqm.webui.admin.dto.JobDefDto;
import com.enioka.jqm.webui.admin.dto.NodeDto;
import com.enioka.jqm.webui.admin.dto.PemissionsBagDto;
import com.enioka.jqm.webui.admin.dto.QueueDto;
import com.enioka.jqm.webui.admin.dto.QueueMappingDto;
import com.enioka.jqm.webui.admin.dto.RRoleDto;
import com.enioka.jqm.webui.admin.dto.RUserDto;

@Path("/admin")
public class ServiceAdmin
{
    // ////////////////////////////////////////////////////////////////////////
    // Common methods
    // ////////////////////////////////////////////////////////////////////////

    private <J, D> List<D> getDtoList(Class<J> jpaClass)
    {
        List<D> res = new ArrayList<D>();
        EntityManager em = Helpers.getEm();

        try
        {
            List<J> r = em.createQuery("SELECT n FROM " + jpaClass.getSimpleName() + " n", jpaClass).getResultList();
            for (J n : r)
            {
                res.add(Jpa2Dto.<D> getDTO(n, em));
            }
            return res;
        }
        catch (Exception e)
        {
            throw new ErrorDto("The server failed to list all objects of type " + jpaClass.getSimpleName(), 2, e,
                    Status.INTERNAL_SERVER_ERROR);
        }
        finally
        {
            em.close();
        }
    }

    private <J, D> D getDto(Class<J> jpaClass, int id)
    {
        EntityManager em = Helpers.getEm();
        try
        {
            D res = Jpa2Dto.<D> getDTO(em.find(jpaClass, id), em);
            if (res == null)
            {
                throw new ErrorDto("There is no object of type " + jpaClass.getSimpleName() + " in the database with ID " + id + ".",
                        "An attempt was made to GET an object that does not exist by a client", 1, Status.NOT_FOUND);
            }
            return res;
        }
        finally
        {
            em.close();
        }
    }

    private <J> void deleteItem(Class<J> jpaClass, Integer id)
    {
        EntityManager em = Helpers.getEm();
        Object j = null;
        try
        {
            j = em.find(jpaClass, id);
            if (j == null)
            {
                throw new ErrorDto("There is no object of type " + jpaClass.getSimpleName() + " in the database",
                        "An attempt was made to DELETE an object that does not exist by a client", 3, Status.NOT_FOUND);
            }
            em.getTransaction().begin();
            Dto2Jpa.clean(j, em);
            em.remove(j);
            em.getTransaction().commit();
        }
        finally
        {
            em.close();
        }
    }

    private <D> void setItem(D dto)
    {
        EntityManager em = Helpers.getEm();
        try
        {
            em.getTransaction().begin();
            Dto2Jpa.setJpa(dto, em);
            em.getTransaction().commit();
        }
        finally
        {
            em.close();
        }
    }

    public <D, J> void setItems(Class<J> jpaClass, List<D> dtos)
    {
        EntityManager em = Helpers.getEm();
        try
        {
            List<J> existBefore = em.createQuery("SELECT n FROM " + jpaClass.getSimpleName() + " n", jpaClass).getResultList();
            List<J> existAfter = new ArrayList<J>();

            em.getTransaction().begin();

            // Update or create items
            for (D dto : dtos)
            {
                existAfter.add(Dto2Jpa.<J> setJpa(dto, em));
            }

            // Delete old items
            old: for (J before : existBefore)
            {
                for (J after : existAfter)
                {
                    if (before.equals(after))
                    {
                        continue old;
                    }
                }
                Dto2Jpa.clean(before, em);
                em.remove(before);
            }

            // Done
            em.getTransaction().commit();
        }
        finally
        {
            em.close();
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // Nodes
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("node")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache("public, max-age=60")
    public List<NodeDto> getNodes()
    {
        return getDtoList(Node.class);
    }

    @GET
    @Path("node/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache("public, max-age=60")
    public NodeDto getNode(@PathParam("id") int id)
    {
        System.out.println(SecurityUtils.getSubject().getPrincipal());
        return getDto(Node.class, id);
    }

    @PUT
    @Path("node")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setNodes(List<NodeDto> dtos)
    {
        setItems(Node.class, dtos);
    }

    @POST
    @Path("node")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setNode(NodeDto dto)
    {
        setItem(dto);
    }

    // ////////////////////////////////////////////////////////////////////////
    // Queues
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("q")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public List<QueueDto> getQueues()
    {
        return getDtoList(Queue.class);
    }

    @PUT
    @Path("q")
    @Consumes(MediaType.APPLICATION_JSON)
    @HttpCache
    public void setQueues(List<QueueDto> dtos)
    {
        setItems(Queue.class, dtos);
    }

    @GET
    @Path("q/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public QueueDto getQueue(@PathParam("id") int id)
    {
        return getDto(Queue.class, id);
    }

    @PUT
    @Path("q/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueue(@PathParam("id") Integer id, QueueDto dto)
    {
        dto.setId(id);
        setItem(dto);
    }

    @POST
    @Path("q")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueue(QueueDto dto)
    {
        setItem(dto);
    }

    @DELETE
    @Path("q/{id}")
    public void deleteQueue(@PathParam("id") Integer id)
    {
        deleteItem(Queue.class, id);
    }

    // ////////////////////////////////////////////////////////////////////////
    // Deployment parameters - queue mappings
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("qmapping")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public List<QueueMappingDto> getQueueMappings()
    {
        return getDtoList(DeploymentParameter.class);
    }

    @PUT
    @Path("qmapping")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueueMappings(List<QueueMappingDto> dtos)
    {
        setItems(DeploymentParameter.class, dtos);
    }

    @GET
    @Path("qmapping/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public QueueMappingDto getQueueMapping(@PathParam("id") int id)
    {
        return getDto(DeploymentParameter.class, id);
    }

    @PUT
    @Path("qmapping/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueueMapping(@PathParam("id") Integer id, QueueMappingDto dto)
    {
        dto.setId(id);
        setItem(dto);
    }

    @POST
    @Path("qmapping")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueueMapping(QueueMappingDto dto)
    {
        setItem(dto);
    }

    @DELETE
    @Path("qmapping/{id}")
    public void deleteQueueMapping(@PathParam("id") Integer id)
    {
        deleteItem(DeploymentParameter.class, id);
    }

    // ////////////////////////////////////////////////////////////////////////
    // JNDI
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("jndi")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public List<JndiObjectResourceDto> getJndiResources()
    {
        return getDtoList(JndiObjectResource.class);
    }

    @PUT
    @Path("jndi")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setJndiResources(List<JndiObjectResourceDto> dtos)
    {
        setItems(JndiObjectResourceDto.class, dtos);
    }

    @GET
    @Path("jndi/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public JndiObjectResourceDto getJndiResource(@PathParam("id") Integer id)
    {
        return getDto(JndiObjectResource.class, id);
    }

    @PUT
    @Path("jndi/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setJndiResource(@PathParam("id") Integer id, JndiObjectResourceDto dto)
    {
        dto.setId(id);
        setItem(dto);
    }

    @POST
    @Path("jndi")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setJndiResource(JndiObjectResourceDto dto)
    {
        setItem(dto);
    }

    @DELETE
    @Path("jndi/{id}")
    public void deleteJndiResource(@PathParam("id") Integer id)
    {
        deleteItem(JndiObjectResource.class, id);
    }

    // ////////////////////////////////////////////////////////////////////////
    // Global parameters
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("prm")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public List<GlobalParameterDto> getGlobalParameters()
    {
        return getDtoList(GlobalParameter.class);
    }

    @PUT
    @Path("prm")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setGlobalParameters(List<GlobalParameterDto> dtos)
    {
        setItems(GlobalParameter.class, dtos);
    }

    @GET
    @Path("prm/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public GlobalParameterDto getGlobalParameter(@PathParam("id") int id)
    {
        return getDto(GlobalParameter.class, id);
    }

    @PUT
    @Path("prm/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setGlobalParameter(@PathParam("id") Integer id, GlobalParameterDto dto)
    {
        dto.setId(id);
        setItem(dto);
    }

    @POST
    @Path("prm")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setGlobalParameter(GlobalParameterDto dto)
    {
        setItem(dto);
    }

    @DELETE
    @Path("prm/{id}")
    public void deleteGlobalParameter(@PathParam("id") Integer id)
    {
        deleteItem(GlobalParameter.class, id);
    }

    // ////////////////////////////////////////////////////////////////////////
    // JobDef
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("jd")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public List<JobDefDto> getJobDefs()
    {
        return getDtoList(JobDef.class);
    }

    @PUT
    @Path("jd")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setJobDefs(List<JobDefDto> dtos)
    {
        setItems(JobDef.class, dtos);
    }

    @GET
    @Path("jd/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public JobDefDto getJobDef(@PathParam("id") int id)
    {
        return getDto(JobDef.class, id);
    }

    @PUT
    @Path("jd/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setJobDef(@PathParam("id") Integer id, JobDefDto dto)
    {
        dto.setId(id);
        setItem(dto);
    }

    @POST
    @Path("jd")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setJobDef(JobDefDto dto)
    {
        setItem(dto);
    }

    @DELETE
    @Path("jd/{id}")
    public void deleteJobDef(@PathParam("id") Integer id)
    {
        deleteItem(JobDef.class, id);
    }

    // ////////////////////////////////////////////////////////////////////////
    // User
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public List<RUserDto> getUsers()
    {
        return getDtoList(RUser.class);
    }

    @PUT
    @Path("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setUsers(List<RUserDto> dtos)
    {
        setItems(RUser.class, dtos);
    }

    @GET
    @Path("user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public RUserDto getUser(@PathParam("id") int id)
    {
        return getDto(RUser.class, id);
    }

    @PUT
    @Path("user/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setUser(@PathParam("id") Integer id, RUserDto dto)
    {
        dto.setId(id);
        setItem(dto);
    }

    @POST
    @Path("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setUser(RUserDto dto)
    {
        setItem(dto);
    }

    @DELETE
    @Path("user/{id}")
    public void deleteUser(@PathParam("id") Integer id)
    {
        deleteItem(RUser.class, id);
    }

    // ////////////////////////////////////////////////////////////////////////
    // Role
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("role")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public List<RRoleDto> getRoles()
    {
        return getDtoList(RRole.class);
    }

    @PUT
    @Path("role")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setRoles(List<RRoleDto> dtos)
    {
        setItems(RRole.class, dtos);
    }

    @GET
    @Path("role/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public RRoleDto getRole(@PathParam("id") int id)
    {
        return getDto(RRole.class, id);
    }

    @PUT
    @Path("role/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setRole(@PathParam("id") Integer id, RRoleDto dto)
    {
        dto.setId(id);
        setItem(dto);
    }

    @POST
    @Path("role")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setRole(RRoleDto dto)
    {
        setItem(dto);
    }

    @DELETE
    @Path("role/{id}")
    public void deleteRole(@PathParam("id") Integer id)
    {
        deleteItem(RRole.class, id);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("me")
    @HttpCache("private, max-age=36000")
    public PemissionsBagDto getMyself(@Context HttpServletRequest req)
    {
        EntityManager em = Helpers.getEm();
        List<String> res = new ArrayList<String>();
        String auth = em.createQuery("SELECT gp From GlobalParameter gp where gp.key = 'enableWsApiAuth'", GlobalParameter.class)
                .getSingleResult().getValue();
        if (auth.equals("false"))
        {
            res.add("*:*");
        }
        else
        {
            RUser memyselfandi = em.createQuery("SELECT u FROM RUser u WHERE u.login = :l", RUser.class)
                    .setParameter("l", req.getUserPrincipal().getName()).getSingleResult();

            for (RRole r : memyselfandi.getRoles())
            {
                for (RPermission p : r.getPermissions())
                {
                    res.add(p.getName());
                }
            }
        }
        em.close();

        PemissionsBagDto b = new PemissionsBagDto();
        b.permissions = res;
        return b;
    }

    @Path("user/{id}/certificate")
    @Produces("application/zip")
    @GET
    public InputStream getNewCertificate(@PathParam("id") int userIds)
    {
        EntityManager em = null;
        try
        {
            em = Helpers.getEm();
            RUser u = em.find(RUser.class, userIds);
            return JpaCa.getClientData(em, u.getLogin());
        }
        catch (Exception e)
        {
            throw new ErrorDto("could not create certificate", 5, e, Status.INTERNAL_SERVER_ERROR);
        }
        finally
        {
            em.close();
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // Engine log
    // ////////////////////////////////////////////////////////////////////////

    @Path("node/{nodeName}/log")
    @Produces("application/octet-stream")
    @GET
    public InputStream getNodeLog(@PathParam("nodeName") String nodeName, @QueryParam("latest") int latest, @Context HttpServletResponse res)
    {
        SelfDestructFileStream fs = (SelfDestructFileStream) ((HibernateClient) JqmClientFactory.getClient())
                .getEngineLog(nodeName, latest);
        res.setHeader("Content-Disposition", "attachment; filename=" + nodeName + ".log");
        return fs;
    }
}
