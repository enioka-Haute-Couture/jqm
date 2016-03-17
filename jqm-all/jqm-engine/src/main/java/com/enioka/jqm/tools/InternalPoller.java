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
package com.enioka.jqm.tools;

import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.Node;

/**
 * The internal poller is responsible for doing all the repetitive tasks of an engine (excluding polling queues). Namely: check if
 * {@link Node#isStop()} has become true (stop order) and update {@link Node#setLastSeenAlive(java.util.Calendar)} to make visible to the
 * whole cluster that the engine is still alive and that no other engine should start with the same node name.
 */
class InternalPoller implements Runnable
{
    private static Logger jqmlogger = Logger.getLogger(InternalPoller.class);
    private boolean run = true;
    private JqmEngine engine = null;
    private Thread localThread = null;
    private long step;
    private Node node = null;
    private String logLevel = null;
    private Semaphore loop = new Semaphore(0);

    InternalPoller(JqmEngine e)
    {
        this.engine = e;
        EntityManager em = Helpers.getNewEm();

        // Get configuration data
        node = em.find(Node.class, this.engine.getNode().getId());
        this.step = Long.parseLong(Helpers.getParameter("internalPollingPeriodMs", "60000", em));
        this.logLevel = node.getRootLogLevel();
        em.close();
    }

    void stop()
    {
        // The test is important: it prevents the engine from calling interrupt() when stopping
        // ... which can be triggered inside InternalPoller.run!
        jqmlogger.info("Internal poller has received a stop request");
        if (this.run)
        {
            this.run = false;
            if (this.localThread != null)
            {
                this.localThread.interrupt();
            }
        }
    }

    void forceLoop()
    {
        this.loop.release(1);
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName("INTERNAL_POLLER;polling orders;");
        jqmlogger.info("Start of the internal poller");
        EntityManager em = null;
        this.localThread = Thread.currentThread();
        Calendar latestJettyRestart = Calendar.getInstance(), lastJndiPurge = latestJettyRestart;
        String nodePrms = null;

        // Launch main loop
        while (true)
        {
            try
            {
                loop.tryAcquire(this.step, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                run = false;
            }
            if (!run)
            {
                break;
            }

            try
            {
                // Get session
                em = Helpers.getNewEm();

                // Check if stop order
                node = em.find(Node.class, node.getId());
                if (node == null || node.isStop())
                {
                    jqmlogger.info("Node has received a stop order from the database or was removed from the database");
                    jqmlogger.trace("At stop order time, there are " + this.engine.getCurrentlyRunningJobCount()
                            + " jobs running in the node");
                    this.run = false;
                    this.engine.stop();
                    em.close();
                    break;
                }

                // Change log level?
                if (!this.logLevel.equals(node.getRootLogLevel()))
                {
                    this.logLevel = node.getRootLogLevel();
                    Helpers.setLogLevel(this.logLevel);
                }

                // I am alive
                em.getTransaction().begin();
                em.createQuery("UPDATE Node n SET n.lastSeenAlive = current_timestamp() WHERE n.id = :id").setParameter("id", node.getId())
                        .executeUpdate();
                em.getTransaction().commit();

                // Have queue bindings changed, or is engine disabled?

                this.engine.syncPollers(em, node);

                // Jetty restart. Conditions are:
                // * some parameters (such as security parameters) have changed
                // * node parameter change such as start or stop an API.
                Calendar bflkpm = Calendar.getInstance();
                String np = node.getDns() + node.getPort() + node.getLoadApiAdmin() + node.getLoadApiClient() + node.getLoapApiSimple();
                if (nodePrms == null)
                {
                    nodePrms = np;
                }
                Long i = em
                        .createQuery(
                                "SELECT COUNT(gp) from GlobalParameter gp WHERE gp.lastModified > :lm "
                                        + "AND key IN ('disableWsApi', 'enableWsApiSsl', 'enableInternalPki', "
                                        + "'pfxPassword', 'enableWsApiAuth')", Long.class).setParameter("lm", latestJettyRestart)
                        .getSingleResult();
                if (i > 0L || !np.equals(nodePrms))
                {
                    this.engine.getJetty().start(node, em);
                    latestJettyRestart = bflkpm;
                    nodePrms = np;
                }

                // Should JNDI cache be purged?
                i = em.createQuery(
                        "SELECT COUNT(p) FROM JndiObjectResourceParameter p WHERE p.lastModified > :lm OR p.resource.lastModified > :lm",
                        Long.class).setParameter("lm", lastJndiPurge).getSingleResult();
                if (i > 0L)
                {
                    try
                    {
                        ((JndiContext) NamingManager.getInitialContext(null)).resetSingletons();
                        lastJndiPurge = bflkpm;
                    }
                    catch (Exception e)
                    {
                        jqmlogger
                                .warn("Could not reset JNDI singleton resources. New parameters won't be used. Restart engine to update them.",
                                        e);
                    }
                }
            }
            catch (RuntimeException e)
            {
                if (Helpers.testDbFailure(e))
                {
                    jqmlogger.error("connection to database lost - stopping internal poller");
                    jqmlogger.trace("connection error was:", e.getCause());
                    run = false;
                    this.engine.startDbRestarter();
                    break;
                }
                else
                {
                    throw e;
                }
            }
            finally
            {
                // Loop is done, let session go
                Helpers.closeQuietly(em);
            }
        }

        jqmlogger.info("End of the internal poller");
    }
}
