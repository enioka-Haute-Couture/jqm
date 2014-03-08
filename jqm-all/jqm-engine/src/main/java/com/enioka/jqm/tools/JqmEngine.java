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

import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import com.enioka.jqm.jndi.JndiContext;
import com.enioka.jqm.jndi.JndiContextFactory;
import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;

class JqmEngine implements JqmEngineMBean
{
    private List<DeploymentParameter> dps = new ArrayList<DeploymentParameter>();
    private List<Polling> pollers = new ArrayList<Polling>();
    private InternalPoller intPoller = null;
    private Node node = null;
    private EntityManager em = Helpers.getNewEm();
    private Map<String, URL[]> cache = new ConcurrentHashMap<String, URL[]>();
    private JettyServer server = null;
    private JndiContext jndiCtx = null;
    private static Logger jqmlogger = Logger.getLogger(JqmEngine.class);
    private Semaphore ended = new Semaphore(0);
    private ObjectName name;
    private boolean hasEnded = false;

    /**
     * Starts the engine
     * 
     * @param nodeName
     *            the name of the node to start, as in the NODE table of the database.
     * @throws JqmInitError
     */
    public void start(String nodeName)
    {
        if (nodeName == null || nodeName.isEmpty())
        {
            throw new IllegalArgumentException("nodeName cannot be null or empty");
        }

        // Node configuration is in the database
        node = checkAndUpdateNode(nodeName);

        // Check if double-start
        long toWait = (long) (1.1 * Long.parseLong(Helpers.getParameter("aliveSignalMs", "60000", em)));
        if (node.getLastSeenAlive() != null
                && Calendar.getInstance().getTimeInMillis() - node.getLastSeenAlive().getTimeInMillis() <= toWait)
        {
            long r = Calendar.getInstance().getTimeInMillis() - node.getLastSeenAlive().getTimeInMillis();
            throw new JqmInitErrorTooSoon("Another engine named " + nodeName + " was running no less than " + r / 1000
                    + " seconds ago. Either stop the other node, or if it already stopped, please wait " + (toWait - r) / 1000 + " seconds");
        }

        // Log level
        try
        {
            Logger.getRootLogger().setLevel(Level.toLevel(node.getRootLogLevel()));
            Logger.getLogger("com.enioka").setLevel(Level.toLevel(node.getRootLogLevel()));
            jqmlogger.info("Log level is set at " + node.getRootLogLevel() + " which translates as log4j level "
                    + Level.toLevel(node.getRootLogLevel()));
        }
        catch (Exception e)
        {
            jqmlogger.warn("Log level could not be set", e);
        }

        // Log!
        jqmlogger.info("JQM engine for node " + nodeName + " is starting");

        // Log multicasting (& log4j stdout redirect)
        GlobalParameter gp1 = em.createQuery("SELECT g FROM GlobalParameter g WHERE g.key = :k", GlobalParameter.class)
                .setParameter("k", "logFilePerLaunch").getSingleResult();
        if (gp1.getValue().equals("true"))
        {
            RollingFileAppender a = ((RollingFileAppender) Logger.getRootLogger().getAppender("rollingfile"));
            MulticastPrintStream s = new MulticastPrintStream(System.out, FilenameUtils.getFullPath(a.getFile()));
            System.setOut(s);
            ((ConsoleAppender) Logger.getRootLogger().getAppender("consoleAppender")).setWriter(new OutputStreamWriter(s));
            s = new MulticastPrintStream(System.err, "C:/TEMP");
            System.setErr(s);
        }

        // JNDI
        if (jndiCtx == null)
        {
            try
            {
                jndiCtx = JndiContextFactory.createJndiContext(Thread.currentThread().getContextClassLoader());
            }
            catch (NamingException e)
            {
                throw new JqmInitError("Could not register the JNDI provider", e);
            }
        }

        // Jetty
        this.server = new JettyServer();
        this.server.start(node);
        if (node.getPort() == 0)
        {
            // During tests, we use a random port (0) so we must update configuration.
            // New nodes are also created with a non-assigned port.
            em.getTransaction().begin();
            node.setPort(server.getActualPort());
            em.getTransaction().commit();
        }

        // JMX
        try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            name = new ObjectName("com.enioka.jqm:type=Node,name=" + this.node.getName());
            mbs.registerMBean(this, name);
        }
        catch (Exception e)
        {
            throw new JqmInitError("Could not create JMX beans", e);
        }
        jqmlogger.info("JMX management bean for the engine was registered");

        // Security
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManagerPayload());
        }
        jqmlogger.info("Security manager was registered");

        // Get queues to listen to
        dps = em.createQuery("SELECT dp FROM DeploymentParameter dp WHERE dp.node.id = :n", DeploymentParameter.class)
                .setParameter("n", node.getId()).getResultList();

        // Pollers
        for (DeploymentParameter i : dps)
        {
            Polling p = new Polling(i, cache, this);
            pollers.add(p);
            Thread t = new Thread(p);
            t.start();
        }
        jqmlogger.info("All required queues are now polled");

        // Internal poller (stop notifications, keepalive)
        intPoller = new InternalPoller(this);
        Thread t = new Thread(intPoller);
        t.start();

        // Done
        em.close();
        jqmlogger.info("End of JQM engine initialization");
    }

    /**
     * Nicely stop the engine
     */
    @Override
    public void stop()
    {
        jqmlogger.info("JQM engine " + this.node.getName() + " has received a stop order");

        // Stop pollers
        for (Polling p : pollers)
        {
            p.stop();
        }

        // Jetty is closed automatically when all pollers are down

        // Wait for the end of the world
        try
        {
            this.ended.acquire();
        }
        catch (InterruptedException e)
        {
            jqmlogger.error("interrutped", e);
        }
        hasEnded = true;
        jqmlogger.debug("Stop order was correctly handled. Engine for node " + this.node.getName() + " has stopped.");
    }

    Node checkAndUpdateNode(String nodeName)
    {
        em.getTransaction().begin();

        // Node
        Node n = null;
        try
        {
            n = em.createQuery("SELECT n FROM Node n WHERE n.name = :l", Node.class).setParameter("l", nodeName).getSingleResult();
        }
        catch (NoResultException e)
        {
            jqmlogger.info("Node " + nodeName + " does not exist in the configuration and will be created with default values");
            n = new Node();
            n.setDlRepo(System.getProperty("user.dir") + "/outputfiles/");
            n.setName(nodeName);
            n.setPort(0);
            n.setRepo(System.getProperty("user.dir") + "/jobs/");
            n.setExportRepo(System.getProperty("user.dir") + "/exports/");
            em.persist(n);
        }

        // Default queue
        Queue q = null;
        long i = (Long) em.createQuery("SELECT COUNT(qu) FROM Queue qu").getSingleResult();
        jqmlogger.info("There are " + i + " queues defined in the database");
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

        // GlobalParameter
        GlobalParameter gp = null;
        i = (Long) em.createQuery("SELECT COUNT(gp) FROM GlobalParameter gp WHERE gp.key = :k")
                .setParameter("k", Constants.GP_DEFAULT_CONNECTION_KEY).getSingleResult();
        if (i == 0)
        {
            gp = new GlobalParameter();

            gp.setKey("mavenRepo");
            gp.setValue("http://repo1.maven.org/maven2/");
            em.persist(gp);

            gp = new GlobalParameter();
            gp.setKey(Constants.GP_DEFAULT_CONNECTION_KEY);
            gp.setValue(Constants.GP_JQM_CONNECTION_ALIAS);
            em.persist(gp);

            gp = new GlobalParameter();
            gp.setKey("deadline");
            gp.setValue("10");
            em.persist(gp);

            gp = new GlobalParameter();
            gp.setKey("logFilePerLaunch");
            gp.setValue("true");
            em.persist(gp);

            gp = new GlobalParameter();
            gp.setKey("internalPollingPeriodMs");
            gp.setValue("10000");
            em.persist(gp);

            gp = new GlobalParameter();
            gp.setKey("aliveSignalMs");
            gp.setValue("60000");
            em.persist(gp);
        }

        // Deployment parameter
        DeploymentParameter dp = null;
        i = (Long) em.createQuery("SELECT COUNT(dp) FROM DeploymentParameter dp WHERE dp.node = :localnode").setParameter("localnode", n)
                .getSingleResult();
        if (i == 0)
        {
            dp = new DeploymentParameter();
            dp.setClassId(1);
            dp.setNbThread(5);
            dp.setNode(n);
            dp.setPollingInterval(1000);
            dp.setQueue(q);
            em.persist(dp);
            jqmlogger.info("This node will poll from the default queue with default parameters");
        }
        else
        {
            jqmlogger.info("This node is configured to take jobs from at least one queue");
        }

        // JNDI alias for the JDBC connection to the JQM database
        DatabaseProp localDb = null;
        try
        {
            localDb = (DatabaseProp) em.createQuery("SELECT dp FROM DatabaseProp dp WHERE dp.name = :alias")
                    .setParameter("alias", Constants.GP_JQM_CONNECTION_ALIAS).getSingleResult();
            jqmlogger.trace("The jdbc/jqm alias already exists and references " + localDb.getUrl());
        }
        catch (NoResultException e)
        {
            Map<String, Object> props = em.getEntityManagerFactory().getProperties();
            localDb = new DatabaseProp();
            localDb.setDriver((String) props.get("javax.persistence.jdbc.driver"));
            localDb.setName(Constants.GP_JQM_CONNECTION_ALIAS);
            localDb.setPwd((String) props.get("javax.persistence.jdbc.password"));
            localDb.setUrl((String) props.get("javax.persistence.jdbc.url"));
            localDb.setUserName((String) props.get("javax.persistence.jdbc.user"));
            em.persist(localDb);

            jqmlogger.info("A  JNDI alias named " + Constants.GP_JQM_CONNECTION_ALIAS
                    + " towards the JQM db has been created. It references: " + localDb.getUrl());
        }

        // Done
        em.getTransaction().commit();
        return n;
    }

    public List<DeploymentParameter> getDps()
    {
        return dps;
    }

    public void setDps(List<DeploymentParameter> dps)
    {
        this.dps = dps;
    }

    Node getNode()
    {
        return this.node;
    }

    synchronized void checkEngineEnd()
    {
        jqmlogger.trace("Checking if engine should end with the latest poller");
        for (Polling poller : pollers)
        {
            if (poller.isRunning())
            {
                return;
            }
        }
        if (hasEnded)
        {
            return;
        }
        jqmlogger.trace("The engine should end with the latest poller");

        // If here, all pollers are down. Stop Jetty too
        this.server.stop();

        // Also stop the internal poller
        this.intPoller.stop();

        // Reset the stop counter - we may want to restart one day
        em = Helpers.getNewEm();
        this.em.getTransaction().begin();
        try
        {
            this.em.find(Node.class, this.node.getId(), LockModeType.PESSIMISTIC_WRITE);
            this.node.setStop(false);
            this.node.setLastSeenAlive(null);
            this.em.getTransaction().commit();
        }
        catch (Exception e)
        {
            // Shutdown exception is ignored (happens during tests)
            this.em.getTransaction().rollback();
        }
        em.close();

        // JMX
        try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.unregisterMBean(name);
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not unregister engine JMX bean", e);
        }

        // Done
        this.ended.release();
        jqmlogger.info("JQM engine has stopped");
    }

    // //////////////////////////////////////////////////////////////////////////
    // JMX stat methods (they get their own EM to be thread safe)
    // //////////////////////////////////////////////////////////////////////////

    @Override
    public long getCumulativeJobInstancesCount()
    {
        EntityManager em = Helpers.getNewEm();
        Long nb = em.createQuery("SELECT COUNT(i) From History i WHERE i.node = :n", Long.class).setParameter("n", this.node)
                .getSingleResult();
        em.close();
        return nb;
    }

    @Override
    public float getJobsFinishedPerSecondLastMinute()
    {
        EntityManager em = Helpers.getNewEm();
        Calendar minusOneMinute = Calendar.getInstance();
        minusOneMinute.add(Calendar.MINUTE, -1);
        Float nb = em.createQuery("SELECT COUNT(i) From History i WHERE i.endDate >= :d and i.node = :n", Long.class)
                .setParameter("d", minusOneMinute).setParameter("n", this.node).getSingleResult() / 60f;
        em.close();
        return nb;
    }

    @Override
    public long getCurrentlyRunningJobCount()
    {
        EntityManager em = Helpers.getNewEm();
        Long nb = em.createQuery("SELECT COUNT(i) From JobInstance i WHERE i.node = :n", Long.class).setParameter("n", this.node)
                .getSingleResult();
        em.close();
        return nb;
    }

    @Override
    public boolean isAllPollersPolling()
    {
        for (Polling p : this.pollers)
        {
            if (!p.isRunning())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isFull()
    {
        for (Polling p : this.pollers)
        {
            if (p.isFull())
            {
                return true;
            }
        }
        return false;
    }
}
