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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.Node;

/**
 * A minimal API designed to interact well with CLI tools such as schedulers. Some of its methods (file retrieval) are also used by the two
 * other sets of APIs.
 * 
 */
@Path("/simple")
public class ServiceSimple
{
    private static Logger log = LoggerFactory.getLogger(ServiceSimple.class);

    private @Context
    HttpServletResponse res;
    private @Context
    SecurityContext security;
    private Node n = null;
    @Context
    private ServletContext context;

    // ///////////////////////////////////////////////////////////
    // Constructor: determine if running on top of JQM or not
    // ///////////////////////////////////////////////////////////

    public ServiceSimple(@Context ServletContext context)
    {
        if (context.getInitParameter("jqmnodeid") != null)
        {
            EntityManager em = null;
            try
            {
                em = Helpers.getEm();

                n = em.find(Node.class, Integer.parseInt(context.getInitParameter("jqmnodeid")));
                if (n == null)
                {
                    throw new RuntimeException("invalid configuration: no node of ID " + context.getInitParameter("jqmnodeid"));
                }
            }
            finally
            {
                Helpers.closeQuietly(em);
            }
        }
    }

    // ///////////////////////////////////////////////////////////
    // The one and only really simple API
    // ///////////////////////////////////////////////////////////
    @GET
    @Path("status")
    @Produces(MediaType.TEXT_PLAIN)
    public String getStatus(@QueryParam("id") int id)
    {
        return JqmClientFactory.getClient().getJob(id).getState().toString();
    }

    // ///////////////////////////////////////////////////////////
    // File retrieval
    // ///////////////////////////////////////////////////////////

    @GET
    @Path("stdout")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getLogOut(@QueryParam("id") int id)
    {
        res.setHeader("Content-Disposition", "attachment; filename=" + id + ".stdout.txt");
        return getFile(FilenameUtils.concat("./logs", StringUtils.leftPad("" + id, 10, "0") + ".stdout.log"));
    }

    @GET
    @Path("stderr")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getLogErr(@QueryParam("id") int id)
    {
        res.setHeader("Content-Disposition", "attachment; filename=" + id + ".stderr.txt");
        return getFile(FilenameUtils.concat("./logs", StringUtils.leftPad("" + id, 10, "0") + ".stderr.log"));
    }

    @GET
    @Path("file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getDeliverableStream(@QueryParam("id") String randomId)
    {
        if (n == null)
        {
            throw new ErrorDto("can only retrieve a file when the web app runs on top of JQM", "", 7, Status.BAD_REQUEST);
        }

        Deliverable d = null;
        EntityManager em = null;
        try
        {
            em = ((HibernateClient) JqmClientFactory.getClient()).getEm();
            d = em.createQuery("SELECT d from Deliverable d WHERE d.randomId = :ii", Deliverable.class).setParameter("ii", randomId)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            throw new ErrorDto("Deliverable does not exist", 8, e, Status.BAD_REQUEST);
        }
        catch (Exception e)
        {
            throw new ErrorDto("Could not retrieve Deliverable metadata from database", 9, e, Status.INTERNAL_SERVER_ERROR);
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        String ext = FilenameUtils.getExtension(d.getOriginalFileName());
        res.setHeader("Content-Disposition", "attachment; filename=" + d.getFileFamily() + "." + d.getId() + "." + ext);
        return getFile(FilenameUtils.concat(n.getDlRepo(), d.getFilePath()));
    }

    public InputStream getFile(String path)
    {
        try
        {
            log.debug("file retrieval service called by user " + getUserName() + " for file " + path);
            return new FileInputStream(path);
        }
        catch (FileNotFoundException e)
        {
            throw new ErrorDto("Could not find the desired file", 8, e, Status.NO_CONTENT);
        }
    }

    @GET
    @Path("enginelog")
    @Produces(MediaType.TEXT_PLAIN)
    public String getEngineLog(@QueryParam("latest") int latest)
    {
        // Failsafe
        if (latest > 10000)
        {
            latest = 10000;
        }

        ReversedLinesFileReader r = null;
        try
        {
            File f = new File(FilenameUtils.concat("./logs/", "jqm-" + context.getInitParameter("jqmnode") + ".log"));
            r = new ReversedLinesFileReader(f);
            StringBuilder sb = new StringBuilder(latest);
            String buf = r.readLine();
            int i = 1;
            while (buf != null && i <= latest)
            {
                sb.append(buf);
                sb.append(System.getProperty("line.separator"));
                i++;
                buf = r.readLine();
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw new ErrorDto("Could not return the desired file", 8, e, Status.NO_CONTENT);
        }
        finally
        {
            IOUtils.closeQuietly(r);
        }
    }

    private String getUserName()
    {
        if (security != null && security.getUserPrincipal() != null && security.getUserPrincipal().getName() != null)
        {
            return security.getUserPrincipal().getName();
        }
        else
        {
            return "anonymous";
        }
    }

    // ///////////////////////////////////////////////////////////
    // Enqueue - a form service...
    // ///////////////////////////////////////////////////////////

    @POST
    @Path("ji")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String enqueue(@FormParam("applicationname") String applicationName, @FormParam("module") String module,
            @FormParam("mail") String mail, @FormParam("keyword1") String keyword1, @FormParam("keyword2") String keyword2,
            @FormParam("keyword3") String keyword3, @FormParam("parentid") Integer parentId, @FormParam("user") String user,
            @FormParam("sessionid") String sessionId, @FormParam("parameterNames") List<String> prmNames,
            @FormParam("parameterValues") List<String> prmValues)
    {
        if (user == null && security != null && security.getUserPrincipal() != null)
        {
            user = security.getUserPrincipal().getName();
        }

        JobRequest jd = new JobRequest(applicationName, user);

        jd.setModule(module);
        jd.setEmail(mail);
        jd.setKeyword1(keyword1);
        jd.setKeyword2(keyword2);
        jd.setKeyword3(keyword3);
        jd.setParentID(parentId);
        jd.setSessionID(sessionId);

        for (int i = 0; i < prmNames.size(); i++)
        {
            String name = prmNames.get(i);
            String value = null;
            try
            {
                value = prmValues.get(i);
            }
            catch (IndexOutOfBoundsException e)
            {
                throw new ErrorDto("There should be as many parameter names as parameter values", 6, e, Status.BAD_REQUEST);
            }
            jd.addParameter(name, value);
            log.trace("Adding a parameter: " + name + " - " + value);
        }

        Integer i = JqmClientFactory.getClient().enqueue(jd);
        return i.toString();
    }
}
