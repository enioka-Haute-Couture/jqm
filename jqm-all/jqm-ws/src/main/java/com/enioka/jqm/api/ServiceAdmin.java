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

import com.enioka.admin.MetaService;
import com.enioka.api.admin.NodeDto;
import com.enioka.api.admin.PemissionsBagDto;
import com.enioka.api.admin.QueueDto;
import com.enioka.jqm.jdbc.DbConn;
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

@Path("/admin")
public class ServiceAdmin
{

    // ////////////////////////////////////////////////////////////////////////
    // Nodes
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("node")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache("public, max-age=60")
    public List<NodeDto> getNodes()
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getDbSession();
            return MetaService.getNodes(cnx);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    @GET
    @Path("node/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache("public, max-age=60")
    public NodeDto getNode(@PathParam("id") int id)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getDbSession();
            return MetaService.getNode(cnx, id);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    /*
     * @PUT
     * 
     * @Path("node")
     * 
     * @Consumes(MediaType.APPLICATION_JSON) public void setNodes(List<NodeDto> dtos) { setItems(Node.class, dtos); }
     * 
     * @POST
     * 
     * @Path("node")
     * 
     * @Consumes(MediaType.APPLICATION_JSON) public void setNode(NodeDto dto) { setItem(dto); }
     */

    // ////////////////////////////////////////////////////////////////////////
    // Queues
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("q")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public List<QueueDto> getQueues()
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getDbSession();
            return MetaService.getQueues(cnx);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    @PUT
    @Path("q")
    @Consumes(MediaType.APPLICATION_JSON)
    @HttpCache
    public void setQueues(List<QueueDto> dtos)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getDbSession();
            MetaService.syncQueues(cnx, dtos);
            cnx.commit();
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    @GET
    @Path("q/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @HttpCache
    public QueueDto getQueue(@PathParam("id") int id)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getDbSession();
            return MetaService.getQueue(cnx, id);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    @PUT
    @Path("q/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueue(@PathParam("id") Integer id, QueueDto dto)
    {
        dto.setId(id);
        setQueue(dto);
    }

    @POST
    @Path("q")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueue(QueueDto dto)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getDbSession();
            MetaService.upsertQueue(cnx, dto);
            cnx.commit();
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    @DELETE
    @Path("q/{id}")
    public void deleteQueue(@PathParam("id") Integer id)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getDbSession();
            MetaService.deleteQueue(cnx, id);
            cnx.commit();
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // Deployment parameters - queue mappings
    // ////////////////////////////////////////////////////////////////////////

    // @GET
    // @Path("qmapping")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public List<QueueMappingDto> getQueueMappings()
    // {
    // return getDtoList(DeploymentParameter.class);
    // }
    //
    // @PUT
    // @Path("qmapping")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setQueueMappings(List<QueueMappingDto> dtos)
    // {
    // setItems(DeploymentParameter.class, dtos);
    // }
    //
    // @GET
    // @Path("qmapping/{id}")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public QueueMappingDto getQueueMapping(@PathParam("id") int id)
    // {
    // return getDto(DeploymentParameter.class, id);
    // }
    //
    // @PUT
    // @Path("qmapping/{id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setQueueMapping(@PathParam("id") Integer id, QueueMappingDto dto)
    // {
    // dto.setId(id);
    // setItem(dto);
    // }
    //
    // @POST
    // @Path("qmapping")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setQueueMapping(QueueMappingDto dto)
    // {
    // setItem(dto);
    // }
    //
    // @DELETE
    // @Path("qmapping/{id}")
    // public void deleteQueueMapping(@PathParam("id") Integer id)
    // {
    // deleteItem(DeploymentParameter.class, id);
    // }
    //
    // // ////////////////////////////////////////////////////////////////////////
    // // JNDI
    // // ////////////////////////////////////////////////////////////////////////
    //
    // @GET
    // @Path("jndi")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public List<JndiObjectResourceDto> getJndiResources()
    // {
    // return getDtoList(JndiObjectResource.class);
    // }
    //
    // @PUT
    // @Path("jndi")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setJndiResources(List<JndiObjectResourceDto> dtos)
    // {
    // setItems(JndiObjectResourceDto.class, dtos);
    // }
    //
    // @GET
    // @Path("jndi/{id}")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public JndiObjectResourceDto getJndiResource(@PathParam("id") Integer id)
    // {
    // return getDto(JndiObjectResource.class, id);
    // }
    //
    // @PUT
    // @Path("jndi/{id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setJndiResource(@PathParam("id") Integer id, JndiObjectResourceDto dto)
    // {
    // dto.setId(id);
    // setItem(dto);
    // }
    //
    // @POST
    // @Path("jndi")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setJndiResource(JndiObjectResourceDto dto)
    // {
    // setItem(dto);
    // }
    //
    // @DELETE
    // @Path("jndi/{id}")
    // public void deleteJndiResource(@PathParam("id") Integer id)
    // {
    // deleteItem(JndiObjectResource.class, id);
    // }
    //
    // // ////////////////////////////////////////////////////////////////////////
    // // Global parameters
    // // ////////////////////////////////////////////////////////////////////////
    //
    // @GET
    // @Path("prm")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public List<GlobalParameterDto> getGlobalParameters()
    // {
    // return getDtoList(GlobalParameter.class);
    // }
    //
    // @PUT
    // @Path("prm")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setGlobalParameters(List<GlobalParameterDto> dtos)
    // {
    // setItems(GlobalParameter.class, dtos);
    // }
    //
    // @GET
    // @Path("prm/{id}")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public GlobalParameterDto getGlobalParameter(@PathParam("id") int id)
    // {
    // return getDto(GlobalParameter.class, id);
    // }
    //
    // @PUT
    // @Path("prm/{id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setGlobalParameter(@PathParam("id") Integer id, GlobalParameterDto dto)
    // {
    // dto.setId(id);
    // setItem(dto);
    // }
    //
    // @POST
    // @Path("prm")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setGlobalParameter(GlobalParameterDto dto)
    // {
    // setItem(dto);
    // }
    //
    // @DELETE
    // @Path("prm/{id}")
    // public void deleteGlobalParameter(@PathParam("id") Integer id)
    // {
    // deleteItem(GlobalParameter.class, id);
    // }
    //
    // // ////////////////////////////////////////////////////////////////////////
    // // JobDef
    // // ////////////////////////////////////////////////////////////////////////
    //
    // @GET
    // @Path("jd")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public List<JobDefDto> getJobDefs()
    // {
    // return getDtoList(JobDef.class);
    // }
    //
    // @PUT
    // @Path("jd")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setJobDefs(List<JobDefDto> dtos)
    // {
    // setItems(JobDef.class, dtos);
    // }
    //
    // @GET
    // @Path("jd/{id}")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public JobDefDto getJobDef(@PathParam("id") int id)
    // {
    // return getDto(JobDef.class, id);
    // }
    //
    // @PUT
    // @Path("jd/{id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setJobDef(@PathParam("id") Integer id, JobDefDto dto)
    // {
    // dto.setId(id);
    // setItem(dto);
    // }
    //
    // @POST
    // @Path("jd")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setJobDef(JobDefDto dto)
    // {
    // setItem(dto);
    // }
    //
    // @DELETE
    // @Path("jd/{id}")
    // public void deleteJobDef(@PathParam("id") Integer id)
    // {
    // deleteItem(JobDef.class, id);
    // }
    //
    // // ////////////////////////////////////////////////////////////////////////
    // // User
    // // ////////////////////////////////////////////////////////////////////////
    //
    // @GET
    // @Path("user")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public List<RUserDto> getUsers()
    // {
    // return getDtoList(RUser.class);
    // }
    //
    // @PUT
    // @Path("user")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setUsers(List<RUserDto> dtos)
    // {
    // setItems(RUser.class, dtos);
    // }
    //
    // @GET
    // @Path("user/{id}")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public RUserDto getUser(@PathParam("id") int id)
    // {
    // return getDto(RUser.class, id);
    // }
    //
    // @PUT
    // @Path("user/{id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setUser(@PathParam("id") Integer id, RUserDto dto)
    // {
    // dto.setId(id);
    // setItem(dto);
    // }
    //
    // @POST
    // @Path("user")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setUser(RUserDto dto)
    // {
    // setItem(dto);
    // }
    //
    // @DELETE
    // @Path("user/{id}")
    // public void deleteUser(@PathParam("id") Integer id)
    // {
    // deleteItem(RUser.class, id);
    // }
    //
    // // ////////////////////////////////////////////////////////////////////////
    // // Role
    // // ////////////////////////////////////////////////////////////////////////
    //
    // @GET
    // @Path("role")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public List<RRoleDto> getRoles()
    // {
    // return getDtoList(RRole.class);
    // }
    //
    // @PUT
    // @Path("role")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setRoles(List<RRoleDto> dtos)
    // {
    // setItems(RRole.class, dtos);
    // }
    //
    // @GET
    // @Path("role/{id}")
    // @Produces(MediaType.APPLICATION_JSON)
    // @HttpCache
    // public RRoleDto getRole(@PathParam("id") int id)
    // {
    // return getDto(RRole.class, id);
    // }
    //
    // @PUT
    // @Path("role/{id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setRole(@PathParam("id") Integer id, RRoleDto dto)
    // {
    // dto.setId(id);
    // setItem(dto);
    // }
    //
    // @POST
    // @Path("role")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public void setRole(RRoleDto dto)
    // {
    // setItem(dto);
    // }
    //
    // @DELETE
    // @Path("role/{id}")
    // public void deleteRole(@PathParam("id") Integer id)
    // {
    // deleteItem(RRole.class, id);
    // }
    //

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("me")
    @HttpCache("private, max-age=36000")
    public PemissionsBagDto getMyself(@Context HttpServletRequest req)
    {
        List<String> res = new ArrayList<String>();

        DbConn cnx = null;
        try
        {
            cnx = Helpers.getDbSession();
            String auth = GlobalParameter.getParameter(cnx, "enableWsApiAuth", "true");
            if (auth.equals("false"))
            {
                res.add("*:*");
            }
            else
            {
                RUser memyselfandi = RUser.selectlogin(cnx, req.getUserPrincipal().getName());

                for (RRole r : memyselfandi.getRoles(cnx))
                {
                    for (RPermission p : r.getPermissions(cnx))
                    {
                        res.add(p.getName());
                    }
                }
            }
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }

        PemissionsBagDto b = new PemissionsBagDto();
        b.permissions = res;
        return b;
    }
    //
    // @Path("user/{id}/certificate")
    // @Produces("application/zip")
    // @GET
    // public InputStream getNewCertificate(@PathParam("id") int userIds)
    // {
    // EntityManager em = null;
    // try
    // {
    // em = Helpers.getDbSession();
    // RUser u = em.find(RUser.class, userIds);
    // return JpaCa.getClientData(em, u.getLogin());
    // }
    // catch (Exception e)
    // {
    // throw new ErrorDto("could not create certificate", 5, e, Status.INTERNAL_SERVER_ERROR);
    // }
    // finally
    // {
    // em.close();
    // }
    // }
    //
    // // ////////////////////////////////////////////////////////////////////////
    // // Engine log
    // // ////////////////////////////////////////////////////////////////////////
    //
    // @Path("node/{nodeName}/log")
    // @Produces("application/octet-stream")
    // @GET
    // public InputStream getNodeLog(@PathParam("nodeName") String nodeName, @QueryParam("latest") int latest,
    // @Context HttpServletResponse res)
    // {
    // SelfDestructFileStream fs = (SelfDestructFileStream) ((HibernateClient) JqmClientFactory.getClient()).getEngineLog(nodeName,
    // latest);
    // res.setHeader("Content-Disposition", "attachment; filename=" + nodeName + ".log");
    // return fs;
    // }
}
