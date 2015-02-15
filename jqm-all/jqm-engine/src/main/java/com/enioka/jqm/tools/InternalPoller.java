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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.hibernate.exception.JDBCConnectionException;

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
    private long step = 10000;
    private long alive = 60000;
    private Node node = null;

    InternalPoller(JqmEngine e)
    {
        this.engine = e;
        EntityManager em = Helpers.getNewEm();

        // Get configuration data
        node = em.find(Node.class, this.engine.getNode().getId());
        this.step = Long.parseLong(Helpers.getParameter("internalPollingPeriodMs", String.valueOf(this.step), em));
        this.alive = Long.parseLong(Helpers.getParameter("aliveSignalMs", String.valueOf(this.step), em));
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

    @Override
    public void run()
    {
        Thread.currentThread().setName("INTERNAL_POLLER;polling orders;");
        jqmlogger.info("Start of the internal poller");
        EntityManager em = null;
        this.localThread = Thread.currentThread();
        String jettyPrmHash = null;

        // Launch main loop
        long sinceLatestPing = 0;
        while (true)
        {
            try
            {
                Thread.sleep(this.step);
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
                if (node.isStop())
                {
                    jqmlogger.info("Node has received a stop order from the database");
                    jqmlogger.trace("At stop order time, there are " + this.engine.getCurrentlyRunningJobCount()
                            + " jobs running in the node");
                    this.run = false;
                    this.engine.stop();
                    em.close();
                    break;
                }

                // Slower polls & signals
                sinceLatestPing += this.step;
                if (sinceLatestPing >= this.alive * 0.9)
                {
                    // I am alive
                    em.getTransaction().begin();
                    em.createQuery("UPDATE Node n SET n.lastSeenAlive = current_timestamp() WHERE n.id = :id")
                            .setParameter("id", node.getId()).executeUpdate();
                    em.getTransaction().commit();
                    sinceLatestPing = 0;

                    // Have queue bindings changed?
                    this.engine.syncPollers(em, node);

                    // Have parameters changed and require a Jetty restart?
                    String n = node.getDns() + node.getPort() + node.getLoadApiAdmin() + node.getLoadApiClient() + node.getLoapApiSimple()
                            + Helpers.getParameter("disableWsApi", " ", em) + Helpers.getParameter("enableWsApiSsl", " ", em)
                            + Helpers.getParameter("enableInternalPki", " ", em) + Helpers.getParameter("pfxPassword", " ", em)
                            + Helpers.getParameter("enableWsApiAuth", " ", em);
                    if (jettyPrmHash != null && !jettyPrmHash.equals(n))
                    {
                        this.engine.getJetty().start(node, em);
                    }
                    jettyPrmHash = n;
                }
            }
            catch (PersistenceException e)
            {
                if (e.getCause() instanceof JDBCConnectionException)
                {
                    jqmlogger.error("connection to database lost - stopping internal poller");
                    jqmlogger.trace("connection error was:", e.getCause());
                    run = false;
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
