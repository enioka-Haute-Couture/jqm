package com.enioka.jqm.configservices;

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.NonUniqueResultException;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JndiObjectResource;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.RRole;
import com.enioka.jqm.repository.UserManagementRepository;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigurationService
{
    private static Logger jqmlogger = LoggerFactory.getLogger(DefaultConfigurationService.class);

    /**
     * Creates or updates metadata common to all nodes: default queue, global parameters, roles...<br>
     * It is idempotent. It also has the effect of making broken metadata viable again.
     */
    public static void updateConfiguration(DbConn cnx)
    {
        // Default queue
        Queue q = null;
        long i = cnx.runSelectSingle("q_select_count_all", Integer.class);
        if (i == 0L)
        {
            Queue.create(cnx, "DEFAULT", "default queue", true);
            jqmlogger.info("A default queue was created in the configuration");
        }
        else
        {
            try
            {
                jqmlogger.info("Default queue is named " + cnx.runSelectSingle("q_select_default", 4, String.class));
            }
            catch (NonUniqueResultException e)
            {
                // Faulty configuration, but why not
                q = Queue.select(cnx, "q_select_all").get(0);
                cnx.runUpdate("q_update_default_none");
                cnx.runUpdate("q_update_default_by_id", q.getId());
                jqmlogger.info("Queue " + q.getName() + " was modified to become the default queue as there were multiple default queues");
            }
            catch (NoResultException e)
            {
                // Faulty configuration, but why not
                q = Queue.select(cnx, "q_select_all").get(0);
                cnx.runUpdate("q_update_default_none");
                cnx.runUpdate("q_update_default_by_id", q.getId());
                jqmlogger.info("Queue " + q.getName() + " was modified to become the default queue as there were multiple default queues");
            }
        }

        // Global parameters
        GlobalParameter.setParameter(cnx, "mavenRepo", "http://repo1.maven.org/maven2/");
        GlobalParameter.setParameter(cnx, Constants.GP_DEFAULT_CONNECTION_KEY, Constants.GP_JQM_CONNECTION_ALIAS);
        GlobalParameter.setParameter(cnx, "logFilePerLaunch", "true");
        GlobalParameter.setParameter(cnx, "internalPollingPeriodMs", "60000");
        GlobalParameter.setParameter(cnx, "disableWsApi", "false");
        GlobalParameter.setParameter(cnx, "enableWsApiSsl", "false");
        GlobalParameter.setParameter(cnx, "enableWsApiAuth", "true");
        GlobalParameter.setParameter(cnx, "enableInternalPki", "true");

        // Roles
        RRole adminr = UserManagementRepository.createRoleIfMissing(cnx, "administrator", "all permissions without exception", "*:*");
        UserManagementRepository.createRoleIfMissing(cnx, "config admin",
                "can read and write all configuration, except security configuration", "node:*", "queue:*", "qmapping:*", "jndi:*", "prm:*",
                "jd:*");
        UserManagementRepository.createRoleIfMissing(cnx, "config viewer", "can read all configuration except for security configuration",
                "node:read", "queue:read", "qmapping:read", "jndi:read", "prm:read", "jd:read");
        UserManagementRepository.createRoleIfMissing(cnx, "client",
                "can use the full client API except reading logs, files and altering position", "node:read", "queue:read", "job_instance:*",
                "jd:read");
        UserManagementRepository.createRoleIfMissing(cnx, "client power user", "can use the full client API", "node:read", "queue:read",
                "job_instance:*", "jd:read", "logs:read", "queue_position:create", "files:read");
        UserManagementRepository.createRoleIfMissing(cnx, "client read only", "can query job instances and get their files", "queue:read",
                "job_instance:read", "logs:read", "files:read");

        // Users
        UserManagementRepository.createUserIfMissing(cnx, "root", new SecureRandomNumberGenerator().nextBytes().toHex(),
                "all powerful user", adminr.getName());

        // Mail session
        i = cnx.runSelectSingle("jndi_select_count_for_key", Integer.class, "mail/default");
        if (i == 0)
        {
            Map<String, String> prms = new HashMap<>();
            prms.put("smtpServerHost", "smtp.gmail.com");

            JndiObjectResource.create(cnx, "mail/default", "jakarta.mail.Session", "com.enioka.jqm.providers.MailSessionFactory",
                    "default parameters used to send e-mails", true, prms);
        }

        // Done
        cnx.commit();
    }

    /**
     * Creates or updates a node.<br>
     * This method makes the assumption metadata is valid. e.g. there MUST be a single default queue.<br>
     * Call {@link #updateConfiguration(EntityManager)} before to be sure if necessary.
     *
     * @param nodeName
     *            name of the node that should be created or updated (if incompletely defined only)
     * @param em
     *            an EntityManager on which a transaction will be opened.
     */
    public static void updateNodeConfiguration(String nodeName, DbConn cnx, int port)
    {
        // Node
        Long nodeId = null;
        try
        {
            nodeId = cnx.runSelectSingle("node_select_by_key", Long.class, nodeName);
        }
        catch (NoResultException e)
        {
            jqmlogger.info("Node " + nodeName + " does not exist in the configuration and will be created with default values");

            nodeId = Node.create(cnx, nodeName, port, System.getProperty("user.dir") + "/jobs/", System.getProperty("user.dir") + "/jobs/",
                    System.getProperty("user.dir") + "/tmp/", "localhost", "INFO").getId();
            cnx.commit();
        }

        // Deployment parameters
        long i = cnx.runSelectSingle("dp_select_count_for_node", Long.class, nodeId);
        if (i == 0L)
        {
            jqmlogger.info("As this node is not bound to any queue, it will be set to poll from the default queue with default parameters");
            Long default_queue_id = cnx.runSelectSingle("q_select_default", 1, Long.class);
            DeploymentParameter.create(cnx, nodeId, 5, 1000, default_queue_id);

            cnx.commit();
        }
    }
}
