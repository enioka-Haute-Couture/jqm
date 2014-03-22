package com.enioka.jqm.tools;

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
    private EntityManager em = null;
    private Node node = null;
    private Thread localThread = null;
    private long step = 10000;
    private long alive = 60000;

    InternalPoller(JqmEngine e)
    {
        this.engine = e;
    }

    void stop()
    {
        // The test is important: it prevents the engine from calling interrupt() when stopping
        // ... which can be triggered inside InternalPoller.run!
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
        // Log
        jqmlogger.info("Start of the internal poller");
        Thread.currentThread().setName("INTERNAL_POLLER;polling orders;");

        // New EM (after setting thread name)
        em = Helpers.getNewEm();

        // Get configuration data
        this.node = em.find(Node.class, this.engine.getNode().getId());
        this.step = Long.parseLong(Helpers.getParameter("internalPollingPeriodMs", String.valueOf(this.step), em));
        this.alive = Long.parseLong(Helpers.getParameter("aliveSignalMs", String.valueOf(this.step), em));
        em.close();
        this.localThread = Thread.currentThread();

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
            }
            if (!run)
            {
                break;
            }

            // Get session
            em = Helpers.getNewEm();

            // Check if stop order
            this.node = em.find(Node.class, this.node.getId());
            if (this.node.isStop())
            {
                jqmlogger.info("Node has received a stop order from the database");
                jqmlogger.trace("At stop order time, there are " + this.engine.getCurrentlyRunningJobCount() + " jobs running in the node");
                this.run = false;
                this.engine.stop();
                em.close();
                break;
            }

            // I am alive signal
            sinceLatestPing += this.step;
            if (sinceLatestPing >= this.alive * 0.9)
            {
                em.getTransaction().begin();
                em.createQuery("UPDATE Node n SET n.lastSeenAlive = current_timestamp() WHERE n.id = :id")
                        .setParameter("id", this.node.getId()).executeUpdate();
                em.getTransaction().commit();
                sinceLatestPing = 0;
            }

            // Loop is done, let session go
            em.close();
        }

        jqmlogger.info("End of the internal poller");
    }
}
