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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;

class Polling implements Runnable
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

	void stop()
	{
		run = false;
	}

	Polling(DeploymentParameter dp, Map<String, URL[]> cache, JqmEngine engine)
	{
		jqmlogger.debug("Polling JobInstances with the Deployment Parameter: " + dp.getClassId());
		this.dp = dp;
		this.queue = dp.getQueue();
		this.actualNbThread = 0;
		this.tp = new ThreadPool(queue, dp.getNbThread(), cache);
		this.engine = engine;
	}

	protected JobInstance dequeue()
	{
		// Free room?
		if (actualNbThread >= tp.getNbThread())
		{
			return null;
		}

		// Get the list of all jobInstance within the defined queue, ordered by position
		em.getTransaction().begin();
		List<JobInstance> availableJobs = em
				.createQuery(
						"SELECT j FROM JobInstance j LEFT JOIN FETCH j.jd LEFT JOIN FETCH j.queue "
								+ "WHERE j.queue = :q AND j.state = 'SUBMITTED' ORDER BY j.internalPosition ASC", JobInstance.class)
								.setParameter("q", queue).getResultList();

		for (JobInstance res : availableJobs)
		{
			// Lock is given when object is read, not during select... stupid.
			// So we must check if the object is still SUBMITTED.
			try
			{
				em.refresh(res, LockModeType.PESSIMISTIC_WRITE);
			} catch (EntityNotFoundException e)
			{
				// It has already been eaten and finished by another engine
				continue;
			}
			if (!res.getState().equals("SUBMITTED"))
			{
				em.lock(res, LockModeType.NONE);
				continue;
			}

			// Highlander?
			if (res.getJd().isHighlander() && !highlanderPollingMode(res, em))
			{
				em.lock(res, LockModeType.NONE);
				continue;
			}

			// Reserve the JI for this engine
			res.setState("ATTRIBUTED");
			res.setNode(dp.getNode());

			// Stop at the first suitable JI. Release the lock & update the JI which has been attributed to us.
			jqmlogger.debug("JI number " + res.getId() + " will be selected by this poller loop");
			em.getTransaction().commit();
			return res;
		}

		// If here, no suitable JI is available
		em.getTransaction().commit();
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
		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em
				.createQuery(
						"SELECT j FROM JobInstance j WHERE j.id IS NOT :refid AND j.jd.id = :n AND (j.state = 'RUNNING' OR j.state = 'ATTRIBUTED')",
						JobInstance.class).setParameter("refid", jobToTest.getId()).setParameter("n", jobToTest.getJd().getId())
						.getResultList();
		return jobs.isEmpty();
	}

	@Override
	public void run()
	{
		Calendar date = GregorianCalendar.getInstance(Locale.getDefault());
		while (true)
		{
			Calendar today = GregorianCalendar.getInstance(Locale.getDefault());
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

				// if true, CRASHED jobs are removed
				if (today.DATE != date.DATE)
				{
					GlobalParameter gp = em.createQuery("SELECT g FROM GlobalParameter g WHERE g.key = 'deadline'", GlobalParameter.class).getSingleResult();
					String[] format = {"yyyy", "MM", "dd"};
					Date tmp = null;
					try {
						tmp = DateUtils.addDays(DateUtils.parseDate(gp.getValue(), format), +10);
					} catch (ParseException e) {
						jqmlogger.debug(e);
					}
					em.createQuery("DELETE FROM JobInstance j WHERE j.endDate.date > :ddline AND j.state = 'CRASHED'")
					.setParameter("ddline", tmp)
					.executeUpdate();
				}


				// Updating the deploymentParameter
				dp = em.createQuery(
						"SELECT dp FROM DeploymentParameter dp LEFT JOIN FETCH dp.queue LEFT JOIN FETCH dp.node WHERE dp.id = :l",
						DeploymentParameter.class).setParameter("l", dp.getId()).getSingleResult();
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

				// Update the history object: the JI has been attributed
				em.getTransaction().begin();
				History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstanceId = :j", History.class)
						.setParameter("j", ji.getId()).getSingleResult();
				h.setNode(dp.getNode());
				em.getTransaction().commit();

				jqmlogger.debug("The job " + ji.getId() + " has been updated with the node: " + ji.getNode().getListeningInterface());
				jqmlogger.debug("The job history " + h.getId() + " has been updated with the node: " + h.getNode().getListeningInterface());

				// We will run this JI!
				actualNbThread++;

				jqmlogger.debug("TPS QUEUE: " + tp.getQueue().getId());
				jqmlogger.debug("INCREMENTATION NBTHREAD: " + actualNbThread);
				jqmlogger.debug("POLLING QUEUE: " + ji.getQueue().getId());
				jqmlogger.debug("Should the node corresponding to the current job be stopped: " + ji.getNode().isStop());

				em.getTransaction().begin();
				Message m = new Message();
				m.setTextMessage("Status updated: ATTRIBUTED");
				m.setHistory(h);
				em.persist(m);
				em.getTransaction().commit();

				// Run it
				tp.run(ji, this, false);

				// Done for this loop
				jqmlogger.debug("End of poller loop  on queue " + this.queue.getName());

			} catch (InterruptedException e)
			{
				jqmlogger.warn(e);
			}
		}
		this.tp.stop();
		this.hasStopped = true;
		jqmlogger.info("Poller on queue " + dp.getQueue().getName() + " has ended");
		// Let the engine decide if it should stop completely
		this.engine.checkEngineEnd();
	}

	Integer getActualNbThread()
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
}
