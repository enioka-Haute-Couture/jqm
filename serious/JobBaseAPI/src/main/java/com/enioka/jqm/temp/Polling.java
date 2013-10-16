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

package com.enioka.jqm.temp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;
import com.enioka.jqm.tools.ThreadPool;

public class Polling implements Runnable {

	private ArrayList<JobInstance> job = new ArrayList<JobInstance>();
	private DeploymentParameter dp = null;
	private Queue queue = null;
	private EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	private EntityManager em = emf.createEntityManager();
	private ThreadPool tp = null;

	public Polling(DeploymentParameter dp, Map<String, ClassLoader> cache) {

		this.dp = dp;
		this.queue = dp.getQueue();

		this.tp = new ThreadPool(queue, dp.getNbThread(), cache);
	}

	public JobInstance dequeue() {

		// Get the list of all jobInstance with the queue VIP ordered by
		// position
		// ArrayList<JobInstance> q = (ArrayList<JobInstance>) em.createQuery(;

		TypedQuery<JobInstance> query = em.createQuery(
		        "SELECT j FROM JobInstance j WHERE j.queue.name = :q AND j.state = :s ORDER BY j.position ASC", JobInstance.class);
		query.setParameter("q", queue.getName()).setParameter("s", "SUBMITTED");
		job = (ArrayList<JobInstance>) query.getResultList();

		// Set<JobInstance> setq = new HashSet<JobInstance>(q);
		// List<JobInstance> newq = new ArrayList<JobInstance>(setq);

		// for (JobInstance jobInstance : newq) {
		//
		// System.out.println("JOBS: " + jobInstance.getId());
		// }

		// job = new ArrayList<JobInstance>(newq);
		// Collections.sort(job);

		// System.exit(0);

		// Higlander?
		if (job.size() > 0 && job.get(0).getJd().isHighlander() == true) {

			HighlanderMode(em);
		}

		// em
		// .createQuery(
		// "UPDATE Message m SET m.textMessage = :msg WHERE m.history.id = "
		// +
		// "(SELECT h.id FROM History h WHERE h.jobInstance.id = :j)").setParameter("j",
		// job.get(0).getId())
		// .setParameter("msg",
		// "Status updated: ATTRIBUTED").executeUpdate();

		return (!job.isEmpty()) ? job.get(0) : null;
	}

	public ArrayList<JobInstance> getJob() {

		return job;
	}

	public void executionStatus() {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		History h = em.createQuery("SELECT h FROM History h WHERE h.id = :j", History.class).setParameter("j", job.get(0).getId()).getSingleResult();

		CreationTools.createMessage("Status updated: RUNNING", h, em);

		em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j)").setParameter("j", job.get(0).getId())
		        .setParameter("msg", "RUNNING").executeUpdate();

		transac.commit();
		em.close();
		emf.close();
	}

	public void HighlanderMode(EntityManager em) {

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em
		        .createQuery("SELECT j FROM JobInstance j, JobDef jd " + "WHERE j.id IS NOT :refid AND j.jd = :myjd", JobInstance.class)
		        .setParameter("refid", job.get(0).getId()).setParameter("myjd", job.get(0).getJd()).getResultList();

		System.out.println("JOBSSIZE" + jobs.size());

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		for (int i = 1; i < jobs.size(); i++) {

			EntityTransaction t = em.getTransaction();
			t.begin();

			History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", jobs.get(i).getId())
			        .getSingleResult();

			Message m = new Message();

			m.setTextMessage("Status updated: CANCELLED");
			m.setHistory(h);

			em.persist(m);
			t.commit();

			// CreationTools.createMessage("Status updated: CANCELLED", h);

			em.createQuery("UPDATE JobInstance j SET j.state = 'CANCELLED' WHERE j.id = :idJob").setParameter("idJob", jobs.get(i).getId())
			        .executeUpdate();
		}

		transac.commit();

	}

	public void updateExecutionDate(EntityManager em) {

		Calendar executionDate = GregorianCalendar.getInstance(Locale.getDefault());

		History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", job.get(0).getId())
		        .getSingleResult();

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		em.createQuery("UPDATE History h SET h.executionDate = :date WHERE h.id = :h").setParameter("h", h.getId())
		        .setParameter("date", executionDate).executeUpdate();

		transac.commit();
	}

	@Override
	public void run() {

		while (true) {

			try {
				Thread.sleep(dp.getPollingInterval());

				JobInstance ji = dequeue();

				if (ji == null)
					continue;

				em.getTransaction().begin();

				History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance = :j", History.class).setParameter("j", ji).getSingleResult();

				Message m = new Message();

				m.setTextMessage("Status updated: ATTRIBUTED");
				m.setHistory(h);

				em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j)").setParameter("j", ji.getId())
				        .setParameter("msg", "ATTRIBUTED").executeUpdate();

				em.persist(m);
				em.getTransaction().commit();

				System.out.println("TPS QUEUE: " + tp.getQueue().getId());
				System.out.println("POLLING QUEUE: " + ji.getQueue().getId());

				tp.run(ji);
				System.out.println("APRES THREADPOOL RUN");

			} catch (InterruptedException e) {
			}
		}
	}

	// public void clean() {
	//
	// this.job.clear();
	// }

}
