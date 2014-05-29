package com.enioka.jqm.webui.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.webui.admin.dto.GlobalParameterDto;
import com.enioka.jqm.webui.admin.dto.JndiObjectResourceDto;
import com.enioka.jqm.webui.admin.dto.JobDefDto;
import com.enioka.jqm.webui.admin.dto.NodeDTO;
import com.enioka.jqm.webui.admin.dto.QueueDTO;
import com.enioka.jqm.webui.admin.dto.QueueMappingDTO;

@Path("/")
public class AdminService
{
    private static EntityManagerFactory emf;

    private synchronized static EntityManager getEm()
    {
        if (emf == null)
        {
            Properties p = new Properties();
            p.put("javax.persistence.nonJtaDataSource", "java:/comp/env/jdbc/jqm");
            emf = Persistence.createEntityManagerFactory("jobqueue-api-pu", p);
        }

        return emf.createEntityManager();
    }

    // ////////////////////////////////////////////////////////////////////////
    // Common methods
    // ////////////////////////////////////////////////////////////////////////

    private <J, D> List<D> getDtoList(Class<J> jpaClass)
    {
        List<D> res = new ArrayList<D>();
        EntityManager em = getEm();

        try
        {
            List<J> r = em.createQuery("SELECT n FROM " + jpaClass.getSimpleName() + " n", jpaClass).getResultList();
            for (J n : r)
            {
                res.add(Jpa2Dto.<D> getDTO(n));
            }
            return res;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            em.close();
        }
    }

    private <J, D> D getDto(Class<J> jpaClass, int id)
    {
        EntityManager em = getEm();
        try
        {
            return Jpa2Dto.<D> getDTO(em.find(jpaClass, id));
        }
        finally
        {
            em.close();
        }
    }

    private <J> void deleteItem(Class<J> jpaClass, Integer id)
    {
        EntityManager em = getEm();
        Object j = null;
        try
        {
            j = em.find(jpaClass, id);
            em.getTransaction().begin();
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
        EntityManager em = getEm();
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
        EntityManager em = getEm();
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
    public List<NodeDTO> getNodes()
    {
        return getDtoList(Node.class);
    }

    @GET
    @Path("node/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public NodeDTO getNode(@PathParam("id") int id)
    {
        return getDto(Node.class, id);
    }

    // ////////////////////////////////////////////////////////////////////////
    // Queues
    // ////////////////////////////////////////////////////////////////////////

    @GET
    @Path("q")
    @Produces(MediaType.APPLICATION_JSON)
    public List<QueueDTO> getQueues()
    {
        return getDtoList(Queue.class);
    }

    @PUT
    @Path("q")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueues(List<QueueDTO> dtos)
    {
        setItems(Queue.class, dtos);
    }

    @GET
    @Path("q/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public QueueDTO getQueue(@PathParam("id") int id)
    {
        return getDto(Queue.class, id);
    }

    @PUT
    @Path("q/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueue(@PathParam("id") Integer id, QueueDTO dto)
    {
        dto.setId(id);
        setItem(dto);
    }

    @POST
    @Path("q")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueue(QueueDTO dto)
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
    public List<QueueMappingDTO> getQueueMappings()
    {
        return getDtoList(DeploymentParameter.class);
    }

    @PUT
    @Path("qmapping")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueueMappings(List<QueueMappingDTO> dtos)
    {
        setItems(DeploymentParameter.class, dtos);
    }

    @GET
    @Path("qmapping/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public QueueMappingDTO getQueueMapping(@PathParam("id") int id)
    {
        return getDto(DeploymentParameter.class, id);
    }

    @PUT
    @Path("qmapping/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueueMapping(@PathParam("id") Integer id, QueueMappingDTO dto)
    {
        dto.setId(id);
        setItem(dto);
    }

    @POST
    @Path("qmapping")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setQueueMapping(QueueMappingDTO dto)
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
    public JobDefDto getJobDef(@PathParam("id") int id)
    {
        return getDto(JobDef.class, id);
    }

    @PUT
    @Path("jd/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setJobDef(@PathParam("id") Integer id, GlobalParameterDto dto)
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
}
