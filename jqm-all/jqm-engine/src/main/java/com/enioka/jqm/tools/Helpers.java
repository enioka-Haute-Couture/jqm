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

package com.enioka.jqm.tools;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.security.SecureRandom;
import java.sql.SQLTransientException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipFile;

import javax.mail.MessagingException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.StringUtils;
import org.hibernate.LazyInitializationException;
import org.hibernate.exception.JDBCConnectionException;

import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JndiObjectResourceParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.RPermission;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.jpamodel.RUser;
import com.enioka.jqm.jpamodel.State;

/**
 * This is a helper class for internal use only.
 * 
 */
final class Helpers
{
    private static final String PERSISTENCE_UNIT = "jobqueue-api-pu";
    private static Logger jqmlogger = Logger.getLogger(Helpers.class);

    // The one and only EMF in the engine.
    private static Properties props = new Properties();
    private static EntityManagerFactory emf;

    // Resource file contains at least the jqm jdbc connection definition. Static because JNDI root context is common to the whole JVM.
    static String resourceFile = "resources.xml";

    private Helpers()
    {

    }

    /**
     * Get a fresh EM on the jobqueue-api-pu persistence Unit
     * 
     * @return an EntityManager
     */
    static EntityManager getNewEm()
    {
        getEmf();
        return emf.createEntityManager();
    }

    static void setEmf(EntityManagerFactory newEmf)
    {
        emf = newEmf;
    }

    static EntityManagerFactory getEmf()
    {
        if (emf == null)
        {
            emf = createFactory();
        }
        return emf;
    }

    private static EntityManagerFactory createFactory()
    {
        InputStream fis = null;
        try
        {
            Properties p = new Properties();
            fis = Helpers.class.getClassLoader().getResourceAsStream("jqm.properties");
            if (fis != null)
            {
                jqmlogger.trace("A jqm.properties file was found");
                p.load(fis);
                IOUtils.closeQuietly(fis);
                props.putAll(p);
            }
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, props);
        }
        catch (IOException e)
        {
            jqmlogger.fatal("conf/jqm.properties file is invalid", e);
            IOUtils.closeQuietly(fis);
            throw new JqmInitError("Invalid JQM configuration file", e);
        }
        catch (Exception e)
        {
            jqmlogger.fatal("Unable to connect with the database. Maybe your configuration file is wrong. "
                    + "Please check the password or the url in the $JQM_DIR/conf/resources.xml", e);
            throw new JqmInitError("Database connection issue", e);
        }
        finally
        {
            IOUtils.closeQuietly(fis);
        }
    }

    static void closeQuietly(EntityManager em)
    {
        try
        {
            if (em != null)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        catch (Exception e)
        {
            // fail silently
        }
    }

    static void closeQuietly(ZipFile zf)
    {
        try
        {
            if (zf != null)
            {
                zf.close();
            }
        }
        catch (Exception e)
        {
            jqmlogger.warn("could not close jar file", e);
        }
    }

    static void allowCreateSchema()
    {
        props.put("hibernate.hbm2ddl.auto", "update");
    }

    static void registerJndiIfNeeded()
    {
        try
        {
            JndiContext.createJndiContext();
        }
        catch (NamingException e)
        {
            throw new JqmInitError("Could not register the JNDI provider", e);
        }
    }

    /**
     * For internal test use only <br/>
     * <bold>WARNING</bold> This will invalidate all open EntityManagers!
     */
    static void resetEmf()
    {
        if (emf != null)
        {
            emf.close();
            emf = null;
        }
    }

    static void setLogFileName(String name)
    {
        Appender a = Logger.getRootLogger().getAppender("rollingfile");
        if (a == null)
        {
            return;
        }
        RollingFileAppender r = (RollingFileAppender) a;
        r.setFile("./logs/jqm-" + name + ".log");
        r.activateOptions();
    }

    static void setLogLevel(String level)
    {
        try
        {
            Logger.getRootLogger().setLevel(Level.toLevel(level));
            Logger.getLogger("com.enioka").setLevel(Level.toLevel(level));
            jqmlogger.info("Setting general log level at " + level + " which translates as log4j level " + Level.toLevel(level));
        }
        catch (Exception e)
        {
            jqmlogger.warn("Log level could not be set", e);
        }
    }

    /**
     * Create a text message that will be stored in the database. Must be called inside a JPA transaction.
     * 
     * @return the JPA message created
     */
    static Message createMessage(String textMessage, JobInstance jobInstance, EntityManager em)
    {
        Message m = new Message();
        m.setTextMessage(textMessage);
        m.setJi(jobInstance.getId());
        em.persist(m);
        return m;
    }

    /**
     * Create a Deliverable inside the database that will track a file created by a JobInstance Must be called from inside a JPA transaction
     * 
     * @param path
     *            FilePath (relative to a root directory - cf. Node)
     * @param originalFileName
     *            FileName
     * @param fileFamily
     *            File family (may be null). E.g.: "daily report"
     * @param jobId
     *            Job Instance ID
     * @param em
     *            the EM to use.
     */
    static Deliverable createDeliverable(String path, String originalFileName, String fileFamily, Integer jobId, EntityManager em)
    {
        Deliverable j = new Deliverable();

        j.setFilePath(path);
        j.setRandomId(UUID.randomUUID().toString());
        j.setFileFamily(fileFamily);
        j.setJobId(jobId);
        j.setOriginalFileName(originalFileName);

        em.persist(j);
        return j;
    }

    /**
     * Retrieve the value of a single-valued parameter.
     * 
     * @param key
     * @param defaultValue
     * @param em
     */
    static String getParameter(String key, String defaultValue, EntityManager em)
    {
        try
        {
            GlobalParameter gp = em.createQuery("SELECT n from GlobalParameter n WHERE n.key = :key", GlobalParameter.class)
                    .setParameter("key", key).getSingleResult();
            return gp.getValue();
        }
        catch (NoResultException e)
        {
            return defaultValue;
        }
    }

    /**
     * Checks if a parameter exists. If it exists, it is left untouched. If it doesn't, it is created. Only works for parameters which key
     * is unique. Must be called from within an open JPA transaction.
     */
    static void initSingleParam(String key, String initValue, EntityManager em)
    {
        try
        {
            em.createQuery("SELECT n from GlobalParameter n WHERE n.key = :key", GlobalParameter.class).setParameter("key", key)
                    .getSingleResult();
            return;
        }
        catch (NoResultException e)
        {
            GlobalParameter gp = new GlobalParameter();
            gp.setKey(key);
            gp.setValue(initValue);
            em.persist(gp);
        }
        catch (NonUniqueResultException e)
        {
            // It exists! Nothing to do...
        }
    }

    /**
     * Checks if a parameter exists. If it exists, it is updated. If it doesn't, it is created. Only works for parameters which key is
     * unique. Will create a transaction on the given entity manager.
     */
    static void setSingleParam(String key, String value, EntityManager em)
    {
        try
        {
            em.getTransaction().begin();
            GlobalParameter prm = em.createQuery("SELECT n from GlobalParameter n WHERE n.key = :key", GlobalParameter.class)
                    .setParameter("key", key).getSingleResult();
            prm.setValue(value);
            em.getTransaction().commit();
        }
        catch (NoResultException e)
        {
            GlobalParameter gp = new GlobalParameter();
            gp.setKey(key);
            gp.setValue(value);
            em.persist(gp);
            em.getTransaction().commit();
        }
    }

    static void checkConfiguration(String nodeName, EntityManager em)
    {
        // Node
        long n = em.createQuery("SELECT COUNT(n) FROM Node n WHERE n.name = :l", Long.class).setParameter("l", nodeName).getSingleResult();
        if (n == 0L)
        {
            throw new JqmInitError("The node does not exist. It must be referenced (CLI option createnode) before it can be used");
        }
        Node nn = em.createQuery("SELECT n FROM Node n WHERE n.name = :l", Node.class).setParameter("l", nodeName).getSingleResult();

        if (!StringUtils.hasText(nn.getDlRepo()) || !StringUtils.hasText(nn.getRepo()) || !StringUtils.hasText(nn.getTmpDirectory()))
        {
            throw new JqmInitError(
                    "The node does not have all its paths specified. Check node configuration (or recreate it with the CLI).");
        }

        // Default queue
        long i = (Long) em.createQuery("SELECT COUNT(qu) FROM Queue qu where qu.defaultQueue = true").getSingleResult();
        if (i == 0L)
        {
            throw new JqmInitError("There is no default queue. Correct this (for example with CLI option -u, or with the web admin)");
        }
        if (i > 1L)
        {
            throw new JqmInitError(
                    "There is more than one default queue. Correct this (for example with CLI option -u, or with the web admin)");
        }

        // Deployment parameters
        i = (Long) em.createQuery("SELECT COUNT(dp) FROM DeploymentParameter dp WHERE dp.node.name = :localnode", Long.class)
                .setParameter("localnode", nodeName).getSingleResult();
        if (i == 0L)
        {
            jqmlogger.warn(
                    "This node is not bound to any queue. Either use the GUI to bind it or use CLI option -u to bind it to the default queue");
        }

        // Roles
        i = em.createQuery("SELECT count(rr) from RRole rr WHERE rr.name = :rr", Long.class).setParameter("rr", "administrator")
                .getSingleResult();
        if (i == 0L)
        {
            throw new JqmInitError("The 'administrator' role does not exist. It is needed for the APIs. Run CLI option -u to create it.");
        }

        // Mail session
        i = (Long) em.createQuery("SELECT COUNT(r) FROM JndiObjectResource r WHERE r.name = :nn").setParameter("nn", "mail/default")
                .getSingleResult();
        if (i == 0L)
        {
            throw new JqmInitError("Mail session named mail/default does not exist but is required for the engine to run"
                    + ". Use CLI option -u to create an empty one or use the admin web GUI to create it.");
        }
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
    static void updateNodeConfiguration(String nodeName, EntityManager em, int port)
    {
        // Node
        Node n = null;
        try
        {
            n = em.createQuery("SELECT n FROM Node n WHERE n.name = :l", Node.class).setParameter("l", nodeName).getSingleResult();
        }
        catch (NoResultException e)
        {
            jqmlogger.info("Node " + nodeName + " does not exist in the configuration and will be created with default values");
            em.getTransaction().begin();

            n = new Node();
            n.setDlRepo(System.getProperty("user.dir") + "/outputfiles/");
            n.setName(nodeName);
            n.setPort(port);
            n.setRepo(System.getProperty("user.dir") + "/jobs/");
            n.setTmpDirectory(System.getProperty("user.dir") + "/tmp/");
            n.setRootLogLevel("INFO");
            em.persist(n);
            em.getTransaction().commit();
        }

        // Deployment parameters
        DeploymentParameter dp = null;
        long i = (Long) em.createQuery("SELECT COUNT(dp) FROM DeploymentParameter dp WHERE dp.node = :localnode")
                .setParameter("localnode", n).getSingleResult();
        if (i == 0)
        {
            jqmlogger.info("As this node is not bound to any queue, it will be set to poll from the default queue with default parameters");
            Queue q = em.createQuery("SELECT q FROM Queue q WHERE q.defaultQueue = true", Queue.class).getSingleResult();
            em.getTransaction().begin();
            dp = new DeploymentParameter();
            dp.setNbThread(5);
            dp.setNode(n);
            dp.setPollingInterval(1000);
            dp.setQueue(q);
            em.persist(dp);

            em.getTransaction().commit();
        }
    }

    static void updateNodeConfiguration(String nodeName, EntityManager em)
    {
        updateNodeConfiguration(nodeName, em, 0);
    }

    /**
     * Creates or updates metadata common to all nodes: default queue, global parameters, roles...<br>
     * It is idempotent. It also has the effect of making broken metadata viable again.
     */
    static void updateConfiguration(EntityManager em)
    {
        em.getTransaction().begin();

        // Default queue
        Queue q = null;
        long i = (Long) em.createQuery("SELECT COUNT(qu) FROM Queue qu").getSingleResult();
        if (i == 0L)
        {
            q = new Queue();
            q.setDefaultQueue(true);
            q.setDescription("default queue");
            q.setTimeToLive(1024);
            q.setName("DEFAULT");
            em.persist(q);

            jqmlogger.info("A default queue was created in the configuration");
        }
        else
        {
            try
            {
                q = em.createQuery("SELECT q FROM Queue q WHERE q.defaultQueue = true", Queue.class).getSingleResult();
                jqmlogger.info("Default queue is named " + q.getName());
            }
            catch (NonUniqueResultException e)
            {
                // Faulty configuration, but why not
                q = em.createQuery("SELECT q FROM Queue q", Queue.class).getResultList().get(0);
                q.setDefaultQueue(true);
                jqmlogger.info("Queue " + q.getName() + " was modified to become the default queue as there were mutliple default queue");
            }
            catch (NoResultException e)
            {
                // Faulty configuration, but why not
                q = em.createQuery("SELECT q FROM Queue q", Queue.class).getResultList().get(0);
                q.setDefaultQueue(true);
                jqmlogger.warn("Queue  " + q.getName() + " was modified to become the default queue as there was no default queue");
            }
        }

        // Global parameters
        initSingleParam("mavenRepo", "http://repo1.maven.org/maven2/", em);
        initSingleParam(Constants.GP_DEFAULT_CONNECTION_KEY, Constants.GP_JQM_CONNECTION_ALIAS, em);
        initSingleParam("logFilePerLaunch", "true", em);
        initSingleParam("internalPollingPeriodMs", "60000", em);
        initSingleParam("disableWsApi", "false", em);
        initSingleParam("enableWsApiSsl", "false", em);
        initSingleParam("enableWsApiAuth", "true", em);
        initSingleParam("enableInternalPki", "true", em);

        // Roles
        RRole adminr = createRoleIfMissing(em, "administrator", "all permissions without exception", "*:*");
        createRoleIfMissing(em, "config admin", "can read and write all configuration, except security configuration", "node:*", "queue:*",
                "qmapping:*", "jndi:*", "prm:*", "jd:*");
        createRoleIfMissing(em, "config viewer", "can read all configuration except for security configuration", "node:read", "queue:read",
                "qmapping:read", "jndi:read", "prm:read", "jd:read");
        createRoleIfMissing(em, "client", "can use the full client API except reading logs, files and altering position", "node:read",
                "queue:read", "job_instance:*", "jd:read");
        createRoleIfMissing(em, "client power user", "can use the full client API", "node:read", "queue:read", "job_instance:*", "jd:read",
                "logs:read", "queue_position:create", "files:read");
        createRoleIfMissing(em, "client read only", "can query job instances and get their files", "queue:read", "job_instance:read",
                "logs:read", "files:read");

        // Users
        createUserIfMissing(em, "root", "all powerful user", adminr);

        // Mail session
        i = (Long) em.createQuery("SELECT COUNT(r) FROM JndiObjectResource r WHERE r.name = :nn").setParameter("nn", "mail/default")
                .getSingleResult();
        if (i == 0)
        {
            HashMap<String, String> prms = new HashMap<String, String>();
            prms.put("smtpServerHost", "smtp.gmail.com");

            JndiObjectResource res = new JndiObjectResource();
            res.setAuth(null);
            res.setDescription("default parameters used to send e-mails");
            res.setFactory("com.enioka.jqm.providers.MailSessionFactory");
            res.setName("mail/default");
            res.setType("javax.mail.Session");
            res.setSingleton(true);
            em.persist(res);

            JndiObjectResourceParameter prm = new JndiObjectResourceParameter();
            prm.setKey("smtpServerHost");
            prm.setValue("smtp.gmail.com");
            res.getParameters().add(prm);
            prm.setResource(res);
        }

        // Done
        em.getTransaction().commit();
    }

    static RRole createRoleIfMissing(EntityManager em, String roleName, String description, String... permissions)
    {
        try
        {
            return em.createQuery("SELECT rr from RRole rr WHERE rr.name = :r", RRole.class).setParameter("r", roleName).getSingleResult();
        }
        catch (NoResultException e)
        {
            RRole r = new RRole();
            r.setName(roleName);
            r.setDescription(description);
            em.persist(r);

            for (String s : permissions)
            {
                RPermission p = new RPermission();
                p.setName(s);
                p.setRole(r);
                em.persist(p);
                r.getPermissions().add(p);
            }
            return r;
        }
    }

    static RUser createUserIfMissing(EntityManager em, String login, String description, RRole... roles)
    {
        RUser res = null;
        try
        {
            res = em.createQuery("SELECT r from RUser r WHERE r.login = :l", RUser.class).setParameter("l", login).getSingleResult();
        }
        catch (NoResultException e)
        {
            res = new RUser();
            res.setFreeText(description);
            res.setLogin(login);
            res.setPassword(String.valueOf((new SecureRandom()).nextInt()));
            encodePassword(res);
            em.persist(res);
        }
        res.setLocked(false);
        for (RRole r : res.getRoles())
        {
            r.getUsers().remove(res);
        }
        res.getRoles().clear();
        for (RRole r : roles)
        {
            res.getRoles().add(r);
            r.getUsers().add(res);
        }

        return res;
    }

    static void encodePassword(RUser user)
    {
        ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
        user.setPassword(new Sha512Hash(user.getPassword(), salt, 100000).toHex());
        user.setHashSalt(salt.toHex());
    }

    /**
     * Transaction is not opened nor committed here but needed.
     * 
     */
    static History createHistory(JobInstance job, EntityManager em, State finalState, Calendar endDate)
    {
        History h = new History();
        h.setId(job.getId());
        h.setJd(job.getJd());
        h.setApplicationName(job.getJd().getApplicationName());
        h.setSessionId(job.getSessionID());
        h.setQueue(job.getQueue());
        h.setQueueName(job.getQueue().getName());
        h.setEnqueueDate(job.getCreationDate());
        h.setEndDate(endDate);
        h.setAttributionDate(job.getAttributionDate());
        h.setExecutionDate(job.getExecutionDate());
        h.setUserName(job.getUserName());
        h.setEmail(job.getEmail());
        h.setParentJobId(job.getParentId());
        h.setApplication(job.getJd().getApplication());
        h.setModule(job.getJd().getModule());
        h.setKeyword1(job.getJd().getKeyword1());
        h.setKeyword2(job.getJd().getKeyword2());
        h.setKeyword3(job.getJd().getKeyword3());
        h.setInstanceApplication(job.getApplication());
        h.setInstanceKeyword1(job.getKeyword1());
        h.setInstanceKeyword2(job.getKeyword2());
        h.setInstanceKeyword3(job.getKeyword3());
        h.setInstanceModule(job.getModule());
        h.setProgress(job.getProgress());
        h.setStatus(finalState);
        h.setNode(job.getNode());
        h.setNodeName(job.getNode().getName());
        h.setHighlander(job.getJd().isHighlander());

        em.persist(h);

        return h;
    }

    static String getMavenVersion()
    {
        String res = System.getProperty("mavenVersion");
        if (res != null)
        {
            return res;
        }

        InputStream is = Main.class.getResourceAsStream("/META-INF/maven/com.enioka.jqm/jqm-engine/pom.properties");
        Properties p = new Properties();
        try
        {
            p.load(is);
            res = p.getProperty("version");
        }
        catch (Exception e)
        {
            res = "maven version not found";
            jqmlogger.warn("maven version not found");
        }
        return res;
    }

    static JobDef findJobDef(String applicationName, EntityManager em)
    {
        try
        {
            return em.createQuery("SELECT j FROM JobDef j WHERE j.applicationName = :n", JobDef.class).setParameter("n", applicationName)
                    .getSingleResult();
        }
        catch (NoResultException ex)
        {
            return null;
        }
    }

    static Queue findQueue(String qName, EntityManager em)
    {
        try
        {
            return em.createQuery("SELECT q FROM Queue q WHERE q.name = :name", Queue.class).setParameter("name", qName).getSingleResult();
        }
        catch (NoResultException ex)
        {
            return null;
        }
    }

    static void dumpParameters(EntityManager em, Node n)
    {
        String terse = getParameter("disableVerboseStartup", "false", em);
        if ("false".equals(terse))
        {
            jqmlogger.info("Global cluster parameters are as follow:");
            List<GlobalParameter> prms = em.createQuery("SELECT gp FROM GlobalParameter gp", GlobalParameter.class).getResultList();
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

            jqmlogger.info("Node polling parameters are as follow:");
            List<DeploymentParameter> dps = em
                    .createQuery("SELECT dp FROM DeploymentParameter dp WHERE dp.node.id = :n", DeploymentParameter.class)
                    .setParameter("n", n.getId()).getResultList();

            // Pollers
            for (DeploymentParameter dp : dps)
            {
                jqmlogger.info("\t" + dp.getQueue().getName() + " - every " + dp.getPollingInterval() + "ms - maximum " + dp.getNbThread()
                        + " concurrent threads");
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
    static void sendMessage(String to, String subject, String body, String mailSessionJndiAlias) throws MessagingException
    {
        jqmlogger.debug("sending mail to " + to + " - subject is " + subject);
        ClassLoader extLoader = getExtClassLoader();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Object mailSession = null;

        try
        {
            mailSession = InitialContext.doLookup(mailSessionJndiAlias);
        }
        catch (NamingException e)
        {
            throw new MessagingException("could not find mail session description", e);
        }

        try
        {
            Thread.currentThread().setContextClassLoader(extLoader);
            Class transportZ = extLoader.loadClass("javax.mail.Transport");
            Class sessionZ = extLoader.loadClass("javax.mail.Session");
            Class mimeMessageZ = extLoader.loadClass("javax.mail.internet.MimeMessage");
            Class messageZ = extLoader.loadClass("javax.mail.Message");
            Class recipientTypeZ = extLoader.loadClass("javax.mail.Message$RecipientType");
            Object msg = mimeMessageZ.getConstructor(sessionZ).newInstance(mailSession);

            mimeMessageZ.getMethod("setRecipients", recipientTypeZ, String.class).invoke(msg, recipientTypeZ.getField("TO").get(null), to);
            mimeMessageZ.getMethod("setSubject", String.class).invoke(msg, subject);
            mimeMessageZ.getMethod("setText", String.class).invoke(msg, body);

            transportZ.getMethod("send", messageZ).invoke(null, msg);
            jqmlogger.trace("Mail was sent");
        }
        catch (Exception e)
        {
            throw new MessagingException("an exception occurred during mail sending", e);
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
            String message = "The Job number " + ji.getId() + " finished correctly\n\n" + "Job description:\n" + "- Job definition: "
                    + ji.getJd().getApplicationName() + "\n" + "- Parent: " + ji.getParentId() + "\n" + "- User name: " + ji.getUserName()
                    + "\n" + "- Session ID: " + ji.getSessionID() + "\n" + "- Queue: " + ji.getQueue().getName() + "\n" + "- Node: "
                    + ji.getNode().getName() + "\n" + "Best regards,\n";
            sendMessage(ji.getEmail(), "[JQM] Job: " + ji.getId() + " ENDED", message, "mail/default");
        }
        catch (Exception e)
        {
            jqmlogger.warn("Could not send email. Job has nevertheless run correctly", e);
        }
    }

    static ClassLoader getExtClassLoader()
    {
        try
        {
            return ((JndiContext) NamingManager.getInitialContext(null)).getExtCl();
        }
        catch (NamingException e)
        {
            // Don't do anything - this actually cannot happen. Death to checked exceptions.
            return null;
        }
    }

    static boolean testDbFailure(Exception e)
    {
        return (e instanceof LazyInitializationException) || (e instanceof JDBCConnectionException)
                || (e.getCause() instanceof JDBCConnectionException)
                || (e.getCause() != null && e.getCause().getCause() instanceof JDBCConnectionException)
                || (e.getCause() instanceof SQLTransientException)
                || (e.getCause() != null && e.getCause().getCause() instanceof SQLTransientException)
                || (e.getCause() != null && e.getCause().getCause() != null
                        && e.getCause().getCause().getCause() instanceof SQLTransientException)
                || (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause().getCause() != null
                        && e.getCause().getCause().getCause().getCause() instanceof SQLTransientException);
    }
}
