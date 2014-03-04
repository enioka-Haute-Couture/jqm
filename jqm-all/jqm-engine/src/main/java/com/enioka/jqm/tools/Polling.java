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
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.State;

class Polling implements Runnable, PollingMBean
{
    private static Logger jqmlogger = Logger.getLogger(Polling.class);
    private DeploymentParameter dp = null;
    private Queue queue = null;
    private EntityManager em = Helpers.getNewEm();
    private ThreadPool tp = null;
    private boolean run = true;
    private Integer actualNbThread;
    private JqmEngine engine;
    private boolean hasStopped = false;
    private ObjectName name = null;
    private Calendar lastLoop = null;

    @Override
    public void stop()
    {
        run = false;
    }

    Polling(DeploymentParameter dp, Map<String, URL[]> cache, JqmEngine engine)
    {
        jqmlogger.debug("Polling JobInstances with the Deployment Parameter: " + dp.getClassId());
        this.dp = em
                .createQuery("SELECT dp FROM DeploymentParameter dp LEFT JOIN FETCH dp.queue LEFT JOIN FETCH dp.node WHERE dp.id = :l",
                        DeploymentParameter.class).setParameter("l", dp.getId()).getSingleResult();
        this.queue = dp.getQueue();
        this.actualNbThread = 0;
        this.tp = new ThreadPool(queue, dp.getNbThread(), cache);
        this.engine = engine;

        try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            name = new ObjectName("com.enioka.jqm:type=Node.Queue,Node=" + this.dp.getNode().getName() + ",name="
                    + this.dp.getQueue().getName());
            mbs.registerMBean(this, name);
        }
        catch (Exception e)
        {
            throw new JqmInitError("Could not create JMX beans", e);
        }
    }

    protected JobInstance dequeue()
    {
        // Free room?
        if (actualNbThread >= dp.getNbThread())
        {
            return null;
        }

        // Get the list of all jobInstance within the defined queue, ordered by position
        List<JobInstance> availableJobs = em
                .createQuery(
                        "SELECT j FROM JobInstance j LEFT JOIN FETCH j.jd WHERE j.queue = :q AND j.state = :s ORDER BY j.internalPosition ASC",
                        JobInstance.class).setParameter("q", queue).setParameter("s", State.SUBMITTED).getResultList();

        em.getTransaction().begin();
        for (JobInstance res : availableJobs)
        {
            // Lock is given when object is read, not during select... stupid.
            // So we must check if the object is still SUBMITTED.
            try
            {
                em.refresh(res, LockModeType.PESSIMISTIC_WRITE);
            }
            catch (EntityNotFoundException e)
            {
                // It has already been eaten and finished by another engine
                continue;
            }
            if (!res.getState().equals(State.SUBMITTED))
            {
                // Already eaten by another engine, not yet done
                continue;
            }

            // Highlander?
            if (res.getJd().isHighlander() && !highlanderPollingMode(res, em))
            {
                continue;
            }

            // Reserve the JI for this engine. Use a query rather than setter to avoid updating all fields (and locks when verifying FKs)
            em.createQuery(
                    "UPDATE JobInstance j SET j.state = 'ATTRIBUTED', j.node = :n, j.attributionDate = current_timestamp() WHERE id=:i")
                    .setParameter("i", res.getId()).setParameter("n", dp.getNode()).executeUpdate();

            // Stop at the first suitable JI. Release the lock & update the JI which has been attributed to us.
            jqmlogger.debug("JI number " + res.getId() + " will be selected by this poller loop (already " + actualNbThread + "/"
                    + dp.getNbThread() + " on " + this.queue.getName() + ")");
            em.getTransaction().commit();

            // Refresh: we have used update queries, so the cached entity is out of date.
            em.refresh(res);
            return res;
        }

        // If here, no suitable JI is available
        em.getTransaction().rollback();
        return null;
    }

    /**
     * 
     * @param jobToTest
     * @param em
     * @return true if job can be launched even if it is in highlander mode
     */
    protected boolean highlanderPollingMode(JobInstance jobToTest, EntityManager em)
    {
        List<JobInstance> jobs = em
                .createQuery(
                        "SELECT j FROM JobInstance j WHERE j IS NOT :refid AND j.jd = :jd AND (j.state = 'RUNNING' OR j.state = 'ATTRIBUTED')",
                        JobInstance.class).setParameter("refid", jobToTest).setParameter("jd", jobToTest.getJd()).getResultList();
        return jobs.isEmpty();
    }

    @Override
    public void run()
    {
        while (true)
        {
            lastLoop = Calendar.getInstance();

            if (em != null)
            {
                em.close();
            }
            em = Helpers.getNewEm();
            try
            {
                // Sleep for the required time (& exit if asked to)
                if (!run)
                {
                    break;
                }

                // Wait according to the deploymentParameter
                Thread.sleep(dp.getPollingInterval());
                if (!run)
                {
                    break;
                }

                // Check if a stop order has been given
                Node n = dp.getNode();
                if (n.isStop())
                {
                    jqmlogger.debug("Current node must be stopped: " + dp.getNode().isStop());

                    Long nbRunning = (Long) em
                            .createQuery(
                                    "SELECT COUNT(j) FROM JobInstance j WHERE j.node = :node AND j.state = 'ATTRIBUTED' OR j.state = 'RUNNING'")
                            .setParameter("node", n).getSingleResult();
                    jqmlogger.debug("Waiting the end of " + nbRunning + " job(s)");

                    if (nbRunning == 0)
                    {
                        run = false;
                    }

                    // Whatever happens, do not not take new jobs during shutdown
                    continue;
                }

                // Get a JI to run
                JobInstance ji = dequeue();
                if (ji == null)
                {
                    continue;
                }

                jqmlogger.debug("((((((((((((((((((()))))))))))))))))");
                jqmlogger.debug("Actual deploymentParameter: " + dp.getNode().getId());
                jqmlogger.debug("Theorical max nbThread: " + dp.getNbThread());
                jqmlogger.debug("Actual nbThread: " + actualNbThread);
                jqmlogger.debug("JI that will be attributed: " + ji.getId());
                jqmlogger.debug("((((((((((((((((((()))))))))))))))))");

                // We will run this JI!
                actualNbThread++;

                jqmlogger.debug("TPS QUEUE: " + tp.getQueue().getId());
                jqmlogger.debug("INCREMENTATION NBTHREAD: " + actualNbThread);
                jqmlogger.debug("POLLING QUEUE: " + ji.getQueue().getId());
                jqmlogger.debug("Should the node corresponding to the current job be stopped: " + ji.getNode().isStop());

                em.getTransaction().begin();
                Helpers.createMessage("Status updated: ATTRIBUTED", ji, em);
                em.getTransaction().commit();

                // Run it
                tp.run(ji, this);
                ji = null;
            }
            catch (InterruptedException e)
            {
                jqmlogger.warn(e);
            }
        }
        jqmlogger.debug("Poller loop on queue " + this.queue.getName() + " is stopping [engine " + this.dp.getNode().getName() + "]");
        em.close();
        this.tp.stop();
        this.hasStopped = true;
        jqmlogger.info("Poller on queue " + dp.getQueue().getName() + " has ended");
        // Let the engine decide if it should stop completely
        this.engine.checkEngineEnd();

        // JMX
        try
        {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not create JMX beans", e);
        }
    }

    @Override
    public Integer getCurrentActiveThreadCount()
    {
        return actualNbThread;
    }

    synchronized void decreaseNbThread()
    {
        this.actualNbThread--;
    }

    public DeploymentParameter getDp()
    {
        return dp;
    }

    boolean isRunning()
    {
        return !this.hasStopped;
    }

    // //////////////////////////////////////////////////////////
    // JMX
    // //////////////////////////////////////////////////////////

    @Override
    public long getCumulativeJobInstancesCount()
    {
        EntityManager em = Helpers.getNewEm();
        Long nb = em.createQuery("SELECT COUNT(i) From History i WHERE i.node = :n AND i.queue = :q", Long.class)
                .setParameter("n", this.dp.getNode()).setParameter("q", this.dp.getQueue()).getSingleResult();
        em.close();
        return nb;
    }

    @Override
    public float getJobsFinishedPerSecondLastMinute()
    {
        EntityManager em = Helpers.getNewEm();
        Calendar minusOneMinute = Calendar.getInstance();
        minusOneMinute.add(Calendar.MINUTE, -1);
        Float nb = em.createQuery("SELECT COUNT(i) From History i WHERE i.endDate >= :d and i.node = :n AND i.queue = :q", Long.class)
                .setParameter("d", minusOneMinute).setParameter("n", this.dp.getNode()).setParameter("q", this.dp.getQueue())
                .getSingleResult() / 60f;
        em.close();
        return nb;
    }

    @Override
    public long getCurrentlyRunningJobCount()
    {
        EntityManager em = Helpers.getNewEm();
        Long nb = em.createQuery("SELECT COUNT(i) From JobInstance i WHERE i.node = :n AND i.queue = :q", Long.class)
                .setParameter("n", this.dp.getNode()).setParameter("q", this.dp.getQueue()).getSingleResult();
        em.close();
        return nb;
    }

    @Override
    public Integer getPollingIntervalMilliseconds()
    {
        return this.dp.getPollingInterval();
    }

    @Override
    public Integer getMaxConcurrentJobInstanceCount()
    {
        return this.dp.getNbThread();
    }

    @Override
    public boolean isActuallyPolling()
    {
        return (Calendar.getInstance().getTimeInMillis() - this.lastLoop.getTimeInMillis()) <= Math.max(2, dp.getPollingInterval());
    }

    @Override
    public boolean isFull()
    {
        return this.actualNbThread >= this.dp.getNbThread();
    }
}
