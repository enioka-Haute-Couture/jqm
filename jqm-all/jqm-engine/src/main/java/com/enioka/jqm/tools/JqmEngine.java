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

import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.QueryResult;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.History;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Message;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.State;

/**
 * The engine itself. Everything starts in this class.
 */
class JqmEngine implements JqmEngineMBean, JqmEngineOperations
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JqmEngine.class);
    static String latestNodeStartedName = "";

    // Callbacks
    JqmEngineHandler handler = null;

    // Sync data for stopping the engine
    private Semaphore ended = new Semaphore(0);
    private boolean hasEnded = false;

    // Parameters and parameter cache
    private Node node = null;
    private ObjectName name;
    private final ClassloaderManager clManager = new ClassloaderManager();

    // Threads that together constitute the engine
    private Map<Integer, QueuePoller> pollers = new HashMap<Integer, QueuePoller>();
    private InternalPoller intPoller = null;
    private CronScheduler scheduler = null;

    // Misc data
    private Calendar startTime = Calendar.getInstance();
    private Thread killHook = null;
    boolean loadJmxBeans = true;
    private AtomicLong endedInstances = new AtomicLong(0);

    // DB connection resilience data
    private volatile Queue<QueuePoller> qpToRestart = new LinkedBlockingQueue<QueuePoller>();
    private volatile Queue<Loader> loaderToFinalize = new LinkedBlockingQueue<Loader>();
    private volatile Queue<Loader> loaderToRestart = new LinkedBlockingQueue<Loader>();
    private volatile Thread qpRestarter = null;

    /**
     * Starts the engine
     * 
     * @param nodeName
     *            the name of the node to start, as in the NODE table of the database.
     * @throws JqmInitError
     */
    void start(String nodeName, JqmEngineHandler h)
    {
        if (nodeName == null || nodeName.isEmpty())
        {
            throw new IllegalArgumentException("nodeName cannot be null or empty");
        }

        this.handler = h;

        // Set thread name - used in audits
        Thread.currentThread().setName("JQM engine;;" + nodeName);

        // First event
        if (this.handler != null)
        {
            this.handler.onNodeStarting(nodeName);
        }

        // Log: we are starting...
        jqmlogger.info("JQM engine version " + this.getVersion() + " for node " + nodeName + " is starting");
        jqmlogger.info("Java version is " + System.getProperty("java.version") + ". JVM was made by " + System.getProperty("java.vendor")
                + " as " + System.getProperty("java.vm.name") + " version " + System.getProperty("java.vm.version"));

        // JNDI first - the engine itself uses JNDI to fetch its connections!
        Helpers.registerJndiIfNeeded();

        // Database connection
        DbConn cnx = Helpers.getNewDbSession();
        clManager.setIsolationDefault(cnx);
        cnx.logDatabaseInfo(jqmlogger);

        // Node configuration is in the database
        try
        {
            node = Node.select_single(cnx, "node_select_by_key", nodeName);
        }
        catch (NoResultException e)
        {
            throw new JqmRuntimeException("the specified node name [" + nodeName
                    + "] does not exist in the configuration. Please create this node before starting it", e);
        }

        // Check if double-start
        long toWait = (long) (1.1 * Long.parseLong(GlobalParameter.getParameter(cnx, "internalPollingPeriodMs", "60000")));
        if (node.getLastSeenAlive() != null
                && Calendar.getInstance().getTimeInMillis() - node.getLastSeenAlive().getTimeInMillis() <= toWait)
        {
            long r = Calendar.getInstance().getTimeInMillis() - node.getLastSeenAlive().getTimeInMillis();
            throw new JqmInitErrorTooSoon("Another engine named " + nodeName + " was running less than " + r / 1000
                    + " seconds ago. Either stop the other node, or if it already stopped, please wait " + (toWait - r) / 1000
                    + " seconds");
        }
        jqmlogger.debug("The last time an engine with this name was seen was: " + node.getLastSeenAlive());

        // Prevent very quick multiple starts by immediately setting the keep-alive
        QueryResult qr = cnx.runUpdate("node_update_alive_by_id", node.getId());
        cnx.commit();
        if (qr.nbUpdated == 0)
        {
            throw new JqmInitErrorTooSoon("Another engine named " + nodeName + " is running");
        }

        // Only start if the node configuration seems OK
        Helpers.checkConfiguration(nodeName, cnx);

        // Log parameters
        Helpers.dumpParameters(cnx, node);

        // The handler may take any actions it wishes here - such as setting log levels, starting Jetty...
        if (this.handler != null)
        {
            this.handler.onNodeConfigurationRead(node);
        }

        // Remote JMX server
        if (node.getJmxRegistryPort() != null && node.getJmxServerPort() != null && node.getJmxRegistryPort() > 0
                && node.getJmxServerPort() > 0)
        {
            JmxAgent.registerAgent(node.getJmxRegistryPort(), node.getJmxServerPort(), node.getDns());
        }
        else
        {
            jqmlogger.info(
                    "JMX remote listener will not be started as JMX registry port and JMX server port parameters are not both defined");
        }

        // JMX
        if (node.getJmxServerPort() != null && node.getJmxServerPort() > 0)
        {
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
        }
        else
        {
            loadJmxBeans = false;
            jqmlogger.info("JMX management beans will not be loaded as JMX server port is null or zero");
        }

        // Security
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManagerPayload());
        }
        jqmlogger.info("Security manager was registered");

        // Scheduler
        scheduler = new CronScheduler(this);

        // Cleanup
        purgeDeadJobInstances(cnx, this.node);

        // Pollers
        syncPollers(cnx, this.node);
        jqmlogger.info("All required queues are now polled");

        // Internal poller (stop notifications, keep alive)
        intPoller = new InternalPoller(this);
        Thread t = new Thread(intPoller);
        t.start();

        // Kill notifications
        killHook = new SignalHandler(this);
        Runtime.getRuntime().addShutdownHook(killHook);

        // Done
        cnx.close();
        cnx = null;
        JqmEngine.latestNodeStartedName = node.getName();
        if (this.handler != null)
        {
            this.handler.onNodeStarted();
        }
        jqmlogger.info("End of JQM engine initialization");
    }

    /**
     * Gracefully stop the engine
     */
    @Override
    public void stop()
    {
        synchronized (killHook)
        {
            jqmlogger.info("JQM engine " + this.node.getName() + " has received a stop order");

            // Kill hook should be removed
            try
            {
                if (!Runtime.getRuntime().removeShutdownHook(killHook))
                {
                    jqmlogger.error("The engine could not unregister its shutdown hook");
                }
            }
            catch (IllegalStateException e)
            {
                // This happens if the stop sequence is initiated by the shutdown hook itself.
                jqmlogger.info("Stop order is due to an admin operation (KILL/INT)");
            }
        }

        // Stop pollers
        int pollerCount = pollers.size();
        for (QueuePoller p : pollers.values())
        {
            p.stop();
        }

        // Scheduler
        this.scheduler.stop();

        // Jetty is closed automatically when all pollers are down

        // Wait for the end of the world
        if (pollerCount > 0)
        {
            try
            {
                this.ended.acquire();
            }
            catch (InterruptedException e)
            {
                jqmlogger.error("interrupted", e);
            }
        }
        jqmlogger.debug("Stop order was correctly handled. Engine for node " + this.node.getName() + " has stopped.");
    }

    Node getNode()
    {
        return this.node;
    }

    synchronized void syncPollers(DbConn cnx, Node node)
    {
        if (node.getEnabled())
        {
            List<DeploymentParameter> dps = DeploymentParameter.select(cnx, "dp_select_for_node", node.getId());

            QueuePoller p = null;
            for (DeploymentParameter i : dps)
            {
                if (pollers.containsKey(i.getId()))
                {
                    // Nothing to do - the poller updates its own parameters.
                }
                else
                {
                    p = new QueuePoller(this, com.enioka.jqm.model.Queue.select(cnx, "q_select_by_id", i.getQueue()).get(0), i);
                    pollers.put(i.getId(), p);
                    Thread t = new Thread(p);
                    t.start();
                }
            }

            // Remove deleted pollers
            for (int dp : this.pollers.keySet().toArray(new Integer[0]))
            {
                boolean found = false;
                for (DeploymentParameter ndp : dps)
                {
                    if (ndp.getId().equals(dp))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    QueuePoller qp = this.pollers.get(dp);
                    qp.stop();
                    this.pollers.remove(dp);
                }
            }
        }
        else
        {
            // Pause all pollers
            for (QueuePoller qp : this.pollers.values())
            {
                qp.setMaxThreads(0);
            }
        }
    }

    synchronized void checkEngineEnd()
    {
        jqmlogger.trace("Checking if engine should end with the latest poller");
        for (QueuePoller poller : pollers.values())
        {
            if (poller.isRunning())
            {
                jqmlogger.trace("At least the poller on queue " + poller.getQueue().getName() + " is still running and prevents shutdown");
                return;
            }
        }
        if (hasEnded)
        {
            return;
        }
        jqmlogger.trace("The engine should end with the latest poller");
        hasEnded = true;

        // If here, all pollers are down. Stop everythong else.
        if (handler != null)
        {
            handler.onNodeStopped();
        }

        // Also stop the internal poller
        this.intPoller.stop();

        // Reset the stop counter - we may want to restart one day
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();
            cnx.runUpdate("node_update_has_stopped_by_id", node.getId());
            cnx.commit();
        }
        catch (Exception e)
        {
            // Shutdown exception is ignored (happens during tests)
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }

        // JMX
        if (loadJmxBeans)
        {
            try
            {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.unregisterMBean(name);
                jqmlogger.trace("unregistered bean " + name);
            }
            catch (Exception e)
            {
                jqmlogger.error("Could not unregister engine JMX bean", e);
            }
        }

        // Note: if present, the JMX listener is not stopped as it is JVM-global, like the JNDI context

        // Done
        this.ended.release();
        jqmlogger.info("JQM engine has stopped");
    }

    /**
     * To be called at node startup - it purges all job instances associated to this node.
     * 
     * @param cnx
     * @param node
     */
    private void purgeDeadJobInstances(DbConn cnx, Node node)
    {
        for (JobInstance ji : JobInstance.select(cnx, "ji_select_by_node", node.getId()))
        {
            try
            {
                cnx.runSelectSingle("history_select_state_by_id", String.class, ji.getId());
            }
            catch (NoResultException e)
            {
                History.create(cnx, ji, State.CRASHED, Calendar.getInstance());
                Message.create(cnx,
                        "Job was supposed to be running at server startup - usually means it was killed along a server by an admin or a crash",
                        ji.getId());
            }

            cnx.runUpdate("ji_delete_by_id", ji.getId());
        }
        cnx.commit();
    }

    /**
     * A poller should call this method when it encounters a database connection issue, and then should stop.<br>
     * This will ensure the poller is restarted when database connectivity is restored.<br>
     * Only pollers which have called this method are restarted, other are deemed not to have crashed.
     */
    void pollerRestartNeeded(QueuePoller qp)
    {
        qpToRestart.add(qp);
        startDbRestarter();
    }

    void loaderFinalizationNeeded(Loader l)
    {
        loaderToFinalize.add(l);
        startDbRestarter();
    }

    void loaderRestartNeeded(Loader l)
    {
        loaderToRestart.add(l);
        startDbRestarter();
    }

    synchronized void startDbRestarter()
    {
        // On first alert, start the thread which will check connection restoration and relaunch the pollers.
        if (qpRestarter != null)
        {
            return;
        }

        final JqmEngine ee = this;
        qpRestarter = new Thread()
        {
            @Override
            public void run()
            {
                jqmlogger.warn("The engine will now indefinitely try to restore connection to the database");
                DbConn cnx = null;
                boolean back = false;
                long timeToWait = 1; // seconds.

                // First thing is to wait - it is useless to immediately retry connecting as we *know* the DB is unreachable....
                try
                {
                    Thread.sleep(timeToWait * 1000);
                }
                catch (InterruptedException e)
                {
                    return;
                }

                // Then, infinite loop trying to reconnect to the DB. We wait 1s more on each loop, max 1 minute.
                while (!back)
                {
                    try
                    {
                        synchronized (ee)
                        {
                            cnx = Helpers.getNewDbSession();
                            cnx.runSelect("node_select_by_id", 1);
                            back = true;
                            ee.qpRestarter = null;
                            jqmlogger.warn("connection to database was restored");
                        }
                    }
                    catch (Exception e)
                    {
                        // The db is not back yet
                        try
                        {
                            jqmlogger.debug("waiting for db...");
                            Thread.sleep(1000 * timeToWait);
                            timeToWait = Math.min(timeToWait + 1, 60);
                        }
                        catch (InterruptedException e1)
                        {
                            // Not an issue here.
                            jqmlogger.debug("interrupted wait in db restarter");
                        }
                    }
                    finally
                    {
                        Helpers.closeQuietly(cnx);
                    }
                }

                // Restart pollers
                QueuePoller qp = qpToRestart.poll();
                while (qp != null)
                {
                    jqmlogger.warn("resetting poller on queue " + qp.getQueue().getName());
                    qp.reset();
                    Thread t = new Thread(qp);
                    t.start();
                    qp = qpToRestart.poll();
                }

                // Always restart internal poller
                intPoller.stop();
                ee.intPoller = new InternalPoller(ee);
                Thread t = new Thread(ee.intPoller);
                t.start();

                // Finalize loaders that could not store their result inside the database
                Loader l = loaderToFinalize.poll();
                while (l != null)
                {
                    jqmlogger.warn("storing delayed results for loader " + l.getId());
                    try
                    {
                        l.endOfRunDb();
                    }
                    catch (DatabaseException e3)
                    {
                        // There is an edge case here: if the DB fails after commit but before sending ACK.
                        // In that case, we may get a duplicate in the history table, and therefore a constraint violation.
                        if (!(e3.getCause().getMessage().toLowerCase().contains("violation")
                                || e3.getCause().getMessage().toLowerCase().contains("duplicate")))
                        {
                            throw e3;
                        }
                        jqmlogger.info("duplicate loader finalization avoided for loader " + l.getId());
                    }
                    l = loaderToFinalize.poll();
                }

                // Restart loaders that have failed during initialization
                l = loaderToRestart.poll();
                while (l != null)
                {
                    jqmlogger.warn("restarting (after db failure during initialization) loader " + l.getId());
                    (new Thread(l)).start();
                    l = loaderToRestart.poll();
                }

                // Done - let the thread end.
            }
        };
        qpRestarter.start();
    }

    ClassloaderManager getClassloaderManager()
    {
        return this.clManager;
    }

    void signalEndOfRun()
    {
        this.endedInstances.incrementAndGet();
    }

    JqmEngineHandler getHandler()
    {
        return this.handler;
    }

    // //////////////////////////////////////////////////////////////////////////
    // JMX stat methods (they get their own connection to be thread safe)
    // //////////////////////////////////////////////////////////////////////////

    @Override
    public long getCumulativeJobInstancesCount()
    {
        return this.endedInstances.get();
    }

    @Override
    public long getCurrentlyRunningJobCount()
    {
        Long nb = 0L;
        for (QueuePoller p : this.pollers.values())
        {
            nb += p.getCurrentlyRunningJobCount();
        }
        return nb;
    }

    @Override
    public boolean isAllPollersPolling()
    {
        for (QueuePoller p : this.pollers.values())
        {
            if (!p.isActuallyPolling())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean areAllPollersPolling()
    {
        return isAllPollersPolling();
    }

    @Override
    public boolean isFull()
    {
        for (QueuePoller p : this.pollers.values())
        {
            if (p.isFull())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getLateJobs()
    {
        int res = 0;
        for (QueuePoller p : this.pollers.values())
        {
            res += p.getLateJobs();
        }
        return res;
    }

    @Override
    public long getUptime()
    {
        return (Calendar.getInstance().getTimeInMillis() - this.startTime.getTimeInMillis()) / 1000;
    }

    @Override
    public String getVersion()
    {
        return Helpers.getMavenVersion();
    }

    @Override
    public void pause()
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();
            cnx.runUpdate("node_update_enabled_by_id", Boolean.FALSE, node.getId());
            cnx.commit();
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }

        refreshConfiguration();
    }

    @Override
    public void resume()
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();
            cnx.runUpdate("node_update_enabled_by_id", Boolean.TRUE, node.getId());
            cnx.commit();
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }

        refreshConfiguration();
    }

    @Override
    public void refreshConfiguration()
    {
        this.intPoller.forceLoop();
    }
}
