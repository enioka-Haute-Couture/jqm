package com.enioka.jqm.tools;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.Node;

class InternalPoller implements Runnable
{
    private static Logger jqmlogger = Logger.getLogger(InternalPoller.class);
    private boolean run = true;
    private JqmEngine engine = null;
    private EntityManager em = Helpers.getNewEm();
    private Node node = null;
    private Thread localThread = null;
    private long step = 10000;
    private long alive = 60000;

    InternalPoller(JqmEngine e)
    {
        this.engine = e;
        this.node = em.find(Node.class, e.getNode().getId());
        this.step = Long.parseLong(Helpers.getParameter("internalPollingPeriodMs", String.valueOf(this.step), em));
        this.alive = Long.parseLong(Helpers.getParameter("aliveSignalMs", String.valueOf(this.step), em));
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
        jqmlogger.info("Start of the internal poller");
        this.localThread = Thread.currentThread();
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

            // Check if stop order
            em.refresh(this.node);
            if (this.node.isStop())
            {
                jqmlogger.info("Node has received a stop order from the database");
                jqmlogger.trace("At stop order time, there are " + this.engine.getCurrentlyRunningJobCount() + " jobs running in the node");
                this.run = false;
                this.engine.stop();
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
        }

        em.close();
        jqmlogger.info("End of the internal poller");
    }
}
