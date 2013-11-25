/**
 * Copyright �� 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Queue;

class Polling implements Runnable
{
	private static Logger jqmlogger = Logger.getLogger(Polling.class);
	private List<JobInstance> job = new ArrayList<JobInstance>();
	private DeploymentParameter dp = null;
	private Queue queue = null;
	private EntityManager em = Helpers.getNewEm();
	private ThreadPool tp = null;
	private boolean run = true;
	private Integer actualNbThread;

	void stop()
	{

		run = false;
	}

	Polling(DeploymentParameter dp, Map<String, URL[]> cache)
	{
		jqmlogger.debug("Polling instanciation with the Deployment Parameter: " + dp.getClassId());
		this.dp = dp;
		this.queue = dp.getQueue();
		this.actualNbThread = 0;
		this.tp = new ThreadPool(queue, dp.getNbThread(), cache);
	}

	protected JobInstance dequeue()
	{
		// Get the list of all jobInstance with the queue definded ordered by position
		em.getTransaction().begin();
		TypedQuery<JobInstance> query = em.createQuery(
				"SELECT j FROM JobInstance j WHERE j.queue.name = :q AND j.state = :s ORDER BY j.position ASC", JobInstance.class);
		query.setParameter("q", queue.getName()).setParameter("s", "SUBMITTED").setLockMode(LockModeType.PESSIMISTIC_WRITE);
		job = query.getResultList();
		em.getTransaction().commit();

		// Higlander?
		if (job.size() > 0 && job.get(0).getJd().isHighlander() == true)
		{
			highlanderPollingMode(job.get(0), em);
		}

		return (!job.isEmpty()) ? job.get(0) : null;
	}

	protected void highlanderPollingMode(JobInstance currentJob, EntityManager em)
	{
		em.getTransaction().begin();
		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em
				.createQuery("SELECT j FROM JobInstance j WHERE j.id IS NOT :refid AND j.jd.applicationName = :n", JobInstance.class)
				.setParameter("refid", currentJob.getId()).setParameter("n", currentJob.getJd().getApplicationName())
				.setLockMode(LockModeType.PESSIMISTIC_READ).getResultList();

		for (JobInstance j : jobs)
		{
			if (j.getState().equals("ATTRIBUTED") || j.getState().equals("RUNNING"))
			{
				job = new ArrayList<JobInstance>();
			}
		}
		em.getTransaction().commit();
	}

	@Override
	public void run()
	{
		while (true)
		{
			em.getEntityManagerFactory().getCache().evictAll();
			em.clear();
			em.refresh(em.merge(dp.getNode()));
			try
			{
				if (!run)
				{
					break;
				}
				dp = em.createQuery("SELECT dp FROM DeploymentParameter dp WHERE dp.id = :l", DeploymentParameter.class)
						.setParameter("l", dp.getId()).getSingleResult();
				Thread.sleep(dp.getPollingInterval());
				if (!run)
				{
					break;
				}

				JobInstance ji = dequeue();

				if (ji == null)
				{
					continue;
				}

				if (actualNbThread >= tp.getNbThread())
				{
					continue;
				}

				jqmlogger.debug("((((((((((((((((((()))))))))))))))))");
				jqmlogger.debug("Actual deploymentParameter: " + dp.getNode().getId());
				jqmlogger.debug("Theorical max nbThread: " + dp.getNbThread());
				jqmlogger.debug("Actual nbThread: " + actualNbThread);
				jqmlogger.debug("JI that will be attributed: " + ji.getId());
				jqmlogger.debug("((((((((((((((((((()))))))))))))))))");

				em.getTransaction().begin();

				JobInstance tt = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :j", JobInstance.class)
						.setParameter("j", ji.getId()).setLockMode(LockModeType.PESSIMISTIC_READ).getSingleResult();

				tt.setNode(dp.getNode());

				History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance = :j", History.class).setParameter("j", ji)
						.setLockMode(LockModeType.PESSIMISTIC_READ).getSingleResult();

				h.setNode(dp.getNode());

				em.getTransaction().commit();

				em.getTransaction().begin();
				JobInstance t = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :j", JobInstance.class)
						.setParameter("j", ji.getId()).setLockMode(LockModeType.PESSIMISTIC_READ).getSingleResult();
				em.getTransaction().commit();

				jqmlogger.debug("The job " + t.getId() + " have been updated with the node: " + t.getNode().getListeningInterface());
				jqmlogger
						.debug("The job history " + h.getId() + " have been updated with the node: " + h.getNode().getListeningInterface());

				actualNbThread++;

				jqmlogger.debug("TPS QUEUE: " + tp.getQueue().getId());
				jqmlogger.debug("INCREMENTATION NBTHREAD: " + actualNbThread);
				jqmlogger.debug("POLLING QUEUE: " + ji.getQueue().getId());

				jqmlogger.debug("Node corresponding to the current job must be stopped: " + t.getNode().isStop());

				Long n = (Long) em
						.createQuery(
								"SELECT COUNT(j) FROM JobInstance j WHERE j.node = :node AND j.state = 'ATTRIBUTED' OR j.state = 'RUNNING'")
						.setParameter("node", t.getNode()).getSingleResult();

				if (dp.getNode().isStop())
				{
					jqmlogger.debug("Current node must be stop: " + dp.getNode().isStop());

					if (n == 0)
					{
						tp.run(ji, this, true);
					}
					else if (n > 0)
					{
						jqmlogger.debug("Waiting the end of " + n + " job(s)");
						continue;
					}
				}
				else
				{
					em.getTransaction().begin();
					Message m = new Message();

					m.setTextMessage("Status updated: ATTRIBUTED");
					m.setHistory(h);

					em.lock(ji, LockModeType.PESSIMISTIC_WRITE);
					ji.setState("ATTRIBUTED");
					ji = em.merge(ji);
					em.persist(m);
					em.getTransaction().commit();

					tp.run(ji, this, false);
				}

				jqmlogger.debug("End of poller loop  on queue " + this.queue.getName());

			} catch (InterruptedException e)
			{
				jqmlogger.debug(e);
			}
		}
	}

	Integer getActualNbThread()
	{
		return actualNbThread;
	}

	void setActualNbThread(Integer actualNbThread)
	{
		this.actualNbThread = actualNbThread;
	}

	public DeploymentParameter getDp()
	{
		return dp;
	}
}
