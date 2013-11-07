/**
 * Copyright © 2013 enioka. All rights reserved
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
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
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
	private ArrayList<JobInstance> job = new ArrayList<JobInstance>();
	DeploymentParameter dp = null;
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
		TypedQuery<JobInstance> query = em.createQuery(
				"SELECT j FROM JobInstance j WHERE j.queue.name = :q AND j.state = :s ORDER BY j.position ASC", JobInstance.class);
		query.setParameter("q", queue.getName()).setParameter("s", "SUBMITTED");
		job = (ArrayList<JobInstance>) query.getResultList();

		// Higlander?
		if (job.size() > 0 && job.get(0).getJd().isHighlander() == true)
		{
			HighlanderPollingMode(job.get(0), em);
		}

		return (!job.isEmpty()) ? job.get(0) : null;
	}

	ArrayList<JobInstance> getJob()
	{

		return job;
	}

	void executionStatus()
	{
		EntityManager em = Helpers.getNewEm();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		History h = em.createQuery("SELECT h FROM History h WHERE h.id = :j", History.class).setParameter("j", job.get(0).getId())
				.getSingleResult();

		Helpers.createMessage("Status updated: RUNNING", h, em);

		em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j)").setParameter("j", job.get(0).getId())
				.setParameter("msg", "RUNNING").executeUpdate();

		transac.commit();
		em.close();
	}

	protected void HighlanderPollingMode(JobInstance currentJob, EntityManager em)
	{
		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em
				.createQuery("SELECT j FROM JobInstance j WHERE j.id IS NOT :refid AND j.jd.applicationName = :n", JobInstance.class)
				.setParameter("refid", currentJob.getId()).setParameter("n", currentJob.getJd().getApplicationName()).getResultList();

		for (JobInstance j : jobs)
		{
			if (j.getState().equals("ATTRIBUTED") || j.getState().equals("RUNNING"))
				job = new ArrayList<JobInstance>();
		}
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				if (!run)
				{
					break;
				}
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

				if (actualNbThread == tp.getNbThread())
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
						.setParameter("j", ji.getId()).getSingleResult();

				tt.setNode(dp.getNode());

				// em.createQuery("UPDATE JobInstance j SET j.node = :n WHERE j.id = :j").setParameter("j", ji.getId())
				// .setParameter("n", dp.getNode()).executeUpdate();

				em.createQuery("UPDATE History h SET h.node = :n WHERE h.jobInstance.id = :j").setParameter("j", ji.getId())
						.setParameter("n", dp.getNode()).executeUpdate();

				History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance = :j", History.class).setParameter("j", ji)
						.getSingleResult();

				Message m = new Message();

				m.setTextMessage("Status updated: ATTRIBUTED");
				m.setHistory(h);

				em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j").setParameter("j", ji.getId())
						.setParameter("msg", "ATTRIBUTED").executeUpdate();

				em.persist(m);
				em.getTransaction().commit();

				JobInstance t = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :j", JobInstance.class)
						.setParameter("j", ji.getId()).getSingleResult();

				History th = em.createQuery("SELECT j FROM History j WHERE j.jobInstance.id = :j", History.class)
						.setParameter("j", ji.getId()).getSingleResult();

				jqmlogger.debug("The job " + t.getId() + " have been updated with the node: " + t.getNode().getListeningInterface());
				jqmlogger.debug("The job history " + th.getId() + " have been updated with the node: "
						+ th.getNode().getListeningInterface());

				actualNbThread++;

				jqmlogger.debug("TPS QUEUE: " + tp.getQueue().getId());
				jqmlogger.debug("INCREMENTATION NBTHREAD: " + actualNbThread);
				jqmlogger.debug("POLLING QUEUE: " + ji.getQueue().getId());

				tp.run(ji, this);

				jqmlogger.debug("End of poller loop  on queue " + this.queue.getName());

			} catch (InterruptedException e)
			{
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
}
