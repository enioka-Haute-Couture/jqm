/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

package com.enioka.jqm.engine;

import java.lang.management.ManagementFactory;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.sql.SQLTransientException;
import java.util.List;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.enioka.jqm.cl.ExtClassLoader;
import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.jdbc.QueryResult;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.RRole;
import com.enioka.jqm.shared.exceptions.JqmRuntimeException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.shiro.lang.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: cnx should be first arg.

/**
 * This is a helper class for internal use only.
 *
 */
final class Helpers
{
    private static Logger jqmlogger = LoggerFactory.getLogger(Helpers.class);

    // The one and only Database context in the engine.
    private static Db _db = DbManager.getDb();

    private Helpers()
    {

    }

    /**
     * Get a fresh connection on the engine database.
     *
     * @return a DbConn.
     */
    public static DbConn getNewDbSession()
    {
        return _db.getConn();
    }

    public static boolean isDbInitialized()
    {
        return _db != null;
    }

    /**
     * For internal test use only <br/>
     * <bold>WARNING</bold> This will invalidate all open DB sessions!
     */
    static void resetDb()
    {
        _db = null;
    }

    /**
     * Create a text message that will be stored in the database. Must be called inside a transaction.
     */
    static void createMessage(String textMessage, JobInstance jobInstance, DbConn cnx)
    {
        cnx.runUpdate("message_insert", jobInstance.getId(), textMessage);
    }

    /**
     * Create a Deliverable inside the database that will track a file created by a JobInstance Must be called from inside a transaction
     *
     * @param path
     *            FilePath (relative to a root directory - cf. Node)
     * @param originalFileName
     *            FileName
     * @param fileFamily
     *            File family (may be null). E.g.: "daily report"
     * @param jobId
     *            Job Instance ID
     * @param cnx
     *            the DbConn to use.
     */
    static long createDeliverable(String path, String originalFileName, String fileFamily, Long jobId, DbConn cnx)
    {
        QueryResult qr = cnx.runUpdate("deliverable_insert", fileFamily, path, jobId, originalFileName, UUID.randomUUID().toString());
        return qr.getGeneratedId();
    }

    static void checkConfiguration(String nodeName, DbConn cnx)
    {
        // Node
        List<Node> nodes = Node.select(cnx, "node_select_by_key", nodeName);
        if (nodes.size() == 0)
        {
            throw new JqmInitError("The node does not exist. It must be referenced (CLI option createnode) before it can be used");
        }
        Node nn = nodes.get(0);

        if (!StringUtils.hasText(nn.getDlRepo()) || !StringUtils.hasText(nn.getRepo()) || !StringUtils.hasText(nn.getTmpDirectory()))
        {
            throw new JqmInitError(
                    "The node does not have all its paths specified. Check node configuration (or recreate it with the CLI).");
        }

        // Default queue
        List<Queue> defaultQueues = Queue.select(cnx, "q_select_default");
        if (defaultQueues.size() == 0)
        {
            throw new JqmInitError("There is no default queue. Correct this (for example with CLI option -u, or with the web admin)");
        }
        if (defaultQueues.size() > 1)
        {
            throw new JqmInitError(
                    "There is more than one default queue. Correct this (for example with CLI option -u, or with the web admin)");
        }

        // Deployment parameters
        int i = cnx.runSelectSingle("dp_select_count_for_node", Integer.class, nn.getId());
        if (i == 0)
        {
            jqmlogger.warn(
                    "This node is not bound to any queue. Either use the GUI to bind it or use CLI option -u to bind it to the default queue");
        }

        // Roles
        List<RRole> roles = RRole.select(cnx, "role_select_by_key", "administrator");
        if (roles.size() != 1)
        {
            throw new JqmInitError("The 'administrator' role does not exist. It is needed for the APIs. Run CLI option -u to create it.");
        }

        // Mail session
        i = cnx.runSelectSingle("jndi_select_count_for_key", Integer.class, "mail/default");
        if (i == 0L)
        {
            throw new JqmInitError("Mail session named mail/default does not exist but is required for the engine to run"
                    + ". Use CLI option Update-Schema to create an empty one or use the admin web GUI to create it.");
        }
    }

    static void dumpParameters(DbConn cnx, Node n)
    {
        String terse = GlobalParameter.getParameter(cnx, "disableVerboseStartup", "false");
        if ("false".equals(terse))
        {
            jqmlogger.info("Global cluster parameters are as follow:");
            List<GlobalParameter> prms = GlobalParameter.select(cnx, "globalprm_select_all");
            for (GlobalParameter prm : prms)
            {
                jqmlogger.info(String.format("\t%1$s = %2$s", prm.getKey(), prm.getValue()));
            }

            jqmlogger.info("Node parameters are as follow:");
            jqmlogger.info("\tfile produced storage directory: " + n.getDlRepo());
            jqmlogger.info("\tHTTP listening interface: " + n.getDns());
            jqmlogger.info("\tlooks for payloads inside: " + n.getRepo());
            jqmlogger.info("\tlog level: " + n.getRootLogLevel());
            jqmlogger.info("\ttemp files will be created inside: " + n.getTmpDirectory());
            jqmlogger.info("\tJMX registry port: " + n.getJmxRegistryPort());
            jqmlogger.info("\tJMX server port: " + n.getJmxServerPort());
            jqmlogger.info("\tHTTP listening port: " + n.getPort());
            jqmlogger.info("\tAPI admin enabled: " + n.getLoadApiAdmin());
            jqmlogger.info("\tAPI client enabled: " + n.getLoadApiClient());
            jqmlogger.info("\tAPI simple enabled: " + n.getLoapApiSimple());

            // Pollers
            jqmlogger.info("Node polling parameters are as follow:");
            List<DeploymentParameter> dps = DeploymentParameter.select(cnx, "dp_select_for_node", n.getId());
            for (DeploymentParameter dp : dps)
            {
                String q = cnx.runSelectSingle("q_select_by_id", String.class, dp.getQueue()); // TODO: avoid this query with a join.
                jqmlogger.info(
                        "\t" + q + " - every " + dp.getPollingInterval() + "ms - maximum " + dp.getNbThread() + " concurrent threads");
            }

            // Some technical data from the JVM hosting the node
            Runtime rt = Runtime.getRuntime();
            jqmlogger.info("JVM parameters are as follow:");
            jqmlogger.info("\tMax usable memory reported by Java runtime, MB: " + (int) (rt.maxMemory() / 1024 / 1024));
            jqmlogger.info("\tJVM arguments are: " + ManagementFactory.getRuntimeMXBean().getInputArguments());
        }
    }

    /**
     * Send a mail message using a JNDI resource.<br>
     * As JNDI resource providers are inside the EXT class loader, this uses reflection. This method is basically a bonus on top of the
     * MailSessionFactory offered to payloads, making it accessible also to the engine.
     *
     * @param to
     * @param subject
     * @param body
     * @param mailSessionJndiAlias
     * @throws MessagingException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static void sendMessage(String to, String subject, String body, String mailSessionJndiAlias)
    {
        jqmlogger.debug("sending mail to " + to + " - subject is " + subject);
        ClassLoader extLoader = ExtClassLoader.instance;
        extLoader = extLoader == null ? Helpers.class.getClassLoader() : extLoader;
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Object mailSession = null;

        try
        {
            mailSession = InitialContext.doLookup(mailSessionJndiAlias);
        }
        catch (NamingException e)
        {
            throw new JqmRuntimeException("could not find mail session description (mail/default JNDI resource)", e);
        }

        try
        {
            Thread.currentThread().setContextClassLoader(extLoader);
            Class transportZ = extLoader.loadClass("jakarta.mail.Transport");
            Class sessionZ = extLoader.loadClass("jakarta.mail.Session");
            Class mimeMessageZ = extLoader.loadClass("jakarta.mail.internet.MimeMessage");
            Class messageZ = extLoader.loadClass("jakarta.mail.Message");
            Class recipientTypeZ = extLoader.loadClass("jakarta.mail.Message$RecipientType");
            Object msg = mimeMessageZ.getConstructor(sessionZ).newInstance(mailSession);

            mimeMessageZ.getMethod("setRecipients", recipientTypeZ, String.class).invoke(msg, recipientTypeZ.getField("TO").get(null), to);
            mimeMessageZ.getMethod("setSubject", String.class).invoke(msg, subject);
            mimeMessageZ.getMethod("setText", String.class).invoke(msg, body);

            transportZ.getMethod("send", messageZ).invoke(null, msg);
            jqmlogger.trace("Mail was sent");
        }
        catch (Exception e)
        {
            throw new JqmRuntimeException("an exception occurred during mail sending", e);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    static void sendEndMessage(JobInstance ji)
    {
        try
        {
            String message = "The Job number " + ji.getId() + " finished correctly\n\n" + "Job description:\n" + "\n" + "- Parent: "
                    + ji.getParentId() + "\n" + "- User name: " + ji.getUserName() + "\n" + "- Session ID: " + ji.getSessionID() + "\n"
                    + "\n" + "Best regards,\n";
            sendMessage(ji.getEmail(), "[JQM] Job: " + ji.getId() + " ENDED", message, "mail/default");
        }
        catch (Exception e)
        {
            jqmlogger.warn("Could not send email. Job has nevertheless run correctly", e);
        }
    }

    static boolean testDbFailure(Exception e)
    {
        Throwable cause = e.getCause();
        return (ExceptionUtils.indexOfType(e, SQLTransientException.class) != -1)
                || (ExceptionUtils.indexOfType(e, SQLNonTransientConnectionException.class) != -1)
                || (ExceptionUtils.indexOfType(e, SocketException.class) != -1)
                || (ExceptionUtils.indexOfType(e, SocketTimeoutException.class) != -1)
                || (cause != null && cause.getMessage().equals("This connection has been closed"))
                || (cause instanceof SQLException && e.getMessage().equals("Failed to validate a newly established connection."))
                || (cause instanceof SQLNonTransientException && cause.getMessage().equals("connection exception: closed"));
    }
}
