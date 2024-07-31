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
package com.enioka.jqm.ws.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.client.api.JobRequest;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.model.Deliverable;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;

/**
 * A minimal API designed to interact well with CLI tools such as schedulers. Some of its methods (file retrieval) are also used by the two
 * other sets of APIs.
 *
 */
@Path("/simple")
public class ServiceSimple
{
    private static Logger log = LoggerFactory.getLogger(ServiceSimple.class);

    private Long jqmNodeId = null;

    public ServiceSimple(@Context ServletContext context)
    {
        jqmNodeId = Long.parseLong(context.getInitParameter("jqmnodeid").toString());
    }

    /////////////////////////////////////////////////////////////
    // Somes file APIs are only available if the node runs on top of JQM
    /////////////////////////////////////////////////////////////

    private Node getLocalNodeIfRunningOnJqm()
    {
        if (jqmNodeId != null)
        {
            // Running on a JQM node, not a standard servlet container.
            try (DbConn cnx = Helpers.getDbSession())
            {
                try
                {
                    return Node.select_single(cnx, "node_select_by_id", jqmNodeId);
                }
                catch (NoResultException e)
                {
                    throw new RuntimeException("invalid configuration: no node of ID " + jqmNodeId);
                }
            }
        }
        return null;
    }

    /////////////////////////////////////////////////////////////
    // The one and only really simple API
    /////////////////////////////////////////////////////////////

    @GET
    @Path("status")
    @Produces(MediaType.TEXT_PLAIN)
    public String getStatus(@QueryParam("id") long id)
    {
        return Helpers.getClient().getJob(id).getState().toString();
    }

    /////////////////////////////////////////////////////////////
    // File retrieval
    /////////////////////////////////////////////////////////////

    @GET
    @Path("stdout")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getLogOut(@QueryParam("id") long id, @Context HttpServletResponse res, @Context SecurityContext security)
    {
        res.setHeader("Content-Disposition", "attachment; filename=" + id + ".stdout.txt");
        return getFile(FilenameUtils.concat("./logs", StringUtils.leftPad("" + id, 10, "0") + ".stdout.log"), security);
    }

    @GET
    @Path("stderr")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getLogErr(@QueryParam("id") long id, @Context HttpServletResponse res, @Context SecurityContext security)
    {
        res.setHeader("Content-Disposition", "attachment; filename=" + id + ".stderr.txt");
        return getFile(FilenameUtils.concat("./logs", StringUtils.leftPad("" + id, 10, "0") + ".stderr.log"), security);
    }

    @GET
    @Path("file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getDeliverableStream(@QueryParam("id") String randomId, @Context HttpServletResponse res,
            @Context SecurityContext security)
    {
        Node n = getLocalNodeIfRunningOnJqm();
        if (n == null)
        {
            throw new ErrorDto("can only retrieve a file when the web app runs on top of JQM", "", 7, Status.BAD_REQUEST);
        }

        Deliverable d = null;
        try (DbConn cnx = Helpers.getDbSession())
        {
            List<Deliverable> dd = Deliverable.select(cnx, "deliverable_select_by_randomid", randomId);
            if (dd.isEmpty())
            {
                throw new NoResultException("requested file does not exist");
            }

            d = dd.get(0);
        }
        catch (NoResultException e)
        {
            throw new ErrorDto("Deliverable does not exist", 8, e, Status.BAD_REQUEST);
        }
        catch (Exception e)
        {
            throw new ErrorDto("Could not retrieve Deliverable metadata from database", 9, e, Status.INTERNAL_SERVER_ERROR);
        }

        String ext = FilenameUtils.getExtension(d.getOriginalFileName());
        res.setHeader("Content-Disposition", "attachment; filename=" + d.getFileFamily() + "." + d.getId() + "." + ext);
        return getFile(FilenameUtils.concat(n.getDlRepo(), d.getFilePath()), security);
    }

    private InputStream getFile(String path, @Context SecurityContext security)
    {
        try
        {
            log.debug("file retrieval service called by user " + getUserName(security) + " for file " + path);
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
        Node n = getLocalNodeIfRunningOnJqm();
        if (n == null)
        {
            throw new ErrorDto("can only retrieve a file when the web app runs on top of JQM", "", 7, Status.BAD_REQUEST);
        }

        // Failsafe
        if (latest > 10000)
        {
            latest = 10000;
        }

        File f = new File(FilenameUtils.concat("./logs/", "jqm-" + n.getId() + ".log"));
        try (ReversedLinesFileReader r = new ReversedLinesFileReader(f, Charset.defaultCharset()))
        {
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
    }

    private String getUserName(@Context SecurityContext security)
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

    /////////////////////////////////////////////////////////////
    // Enqueue - a form service...
    /////////////////////////////////////////////////////////////

    @POST
    @Path("ji")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String enqueue(@FormParam("applicationname") String applicationName, @FormParam("module") String module,
                          @FormParam("mail") String mail, @FormParam("keyword1") String keyword1, @FormParam("keyword2") String keyword2,
                          @FormParam("keyword3") String keyword3, @FormParam("parentid") Long parentId, @FormParam("user") String user,
                          @FormParam("sessionid") String sessionId, @FormParam("parameterNames") List<String> prmNames,
                          @FormParam("parameterValues") List<String> prmValues, @Context SecurityContext security)
    {
        if (user == null && security != null && security.getUserPrincipal() != null)
        {
            user = security.getUserPrincipal().getName();
        }

        JobRequest jd = Helpers.getClient().newJobRequest(applicationName, user);

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

        long i = jd.enqueue();
        return Long.toString(i);
    }

    /////////////////////////////////////////////////////////////
    // Health
    /////////////////////////////////////////////////////////////

    @GET
    @Path("localnode/health")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLocalNodeHealth() throws MalformedObjectNameException, UnknownHostException {
        // Local service only - not enabled when running on top of Tomcat & co.
        Node n = getLocalNodeIfRunningOnJqm();
        if (n == null)
        {
            throw new ErrorDto("can only retrieve local node health when the web app runs on top of JQM", "", 7, Status.BAD_REQUEST);
        }
        if (n.getJmxServerPort() == null || n.getJmxServerPort() == 0)
        {
            throw new ErrorDto("JMX is not enabled on this server", "", 8, Status.BAD_REQUEST);
        }

        // Connect to JMX server
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName nodeBean = new ObjectName("com.enioka.jqm:type=Node,name=" + n.getName());
        Set<ObjectName> names = server.queryNames(nodeBean, null);
        if (names.isEmpty())
        {
            throw new ErrorDto("could not find the JMX Mbean of the local JQM node", "", 8, Status.INTERNAL_SERVER_ERROR);
        }
        ObjectName localNodeBean = names.iterator().next();

        // Query bean
        Object result;
        try
        {
            result = server.getAttribute(localNodeBean, "AllPollersPolling");
        }
        catch (Exception e)
        {
            throw new ErrorDto("Issue when querying JMX server", 12, e, Status.INTERNAL_SERVER_ERROR);
        }
        if (!(result instanceof Boolean))
        {
            throw new ErrorDto("JMX bean answered with wrong datatype - answer was " + result.toString(), "", 9,
                    Status.INTERNAL_SERVER_ERROR);
        }

        // Analyze output
        boolean res = (Boolean) result;
        if (!res)
        {
            throw new ErrorDto("JQM node has is not working as expected", "", 11, Status.SERVICE_UNAVAILABLE);
        }

        final var standaloneMode = Boolean.parseBoolean(
            GlobalParameter.getParameter(DbManager.getDb().getConn(), "wsStandaloneMode", "false"));

        if (standaloneMode) {
            return "Pollers are polling - IP: " + Inet4Address.getLocalHost().getHostAddress();
        } else {
            return "Pollers are polling";
        }
    }
}
