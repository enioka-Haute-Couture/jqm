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

package com.enioka.jqm.api;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.commons.io.FileUtils;

import com.enioka.jqm.hash.Cryptonite;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobHistoryParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;

/**
 *
 * @author pierre.coppee
 */
public class Dispatcher {

	//----------------------------- JOBDEFINITIONTOJOBDEF --------------------------------------

	private static JobDef jobDefinitionToJobDef(JobDefinition jd) {

		 JobDef job = CreationTools.em.createQuery("SELECT j FROM JobDef j WHERE j.applicationName = :name", JobDef.class)
		.setParameter("name", jd.getApplicationName())
		.getSingleResult();

		 return job;
	}

	private static com.enioka.jqm.api.JobDefinition jobDefToJobDefinition(JobDef jd) {

		com.enioka.jqm.api.JobDefinition job = new com.enioka.jqm.api.JobDefinition();
		 Map<String, String> h = new HashMap<String, String>();

		 if(jd.getParameters() != null) {
			 for (JobDefParameter i : jd.getParameters()) {

				 h.put(i.getKey(), i.getValue());
			 }
		 }

		 job.setParameters(h);
		 job.setApplicationName(jd.getApplicationName());
		 job.setSessionID(jd.getSessionID());
		 job.setApplication(jd.getApplication());
		 job.setModule(jd.getModule());
		 job.setOther1(jd.getOther1());
		 job.setOther2(jd.getOther2());
		 job.setOther3(jd.getOther3());

		 return job;
	}

	private static com.enioka.jqm.api.Queue getQueue(Queue queue) {

		com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();

		q.setDefaultQueue(queue.isDefaultQueue());
		q.setDescription(queue.getDescription());
		q.setId(queue.getId());
		q.setMaxTempInQueue(queue.getMaxTempInQueue());
		q.setMaxTempRunning(queue.getMaxTempRunning());
		q.setName(queue.getName());

		return q;
	}

    private static com.enioka.jqm.api.JobInstance getJobInstance(JobInstance job) {

		Map<String, String> parameters = new HashMap<String, String>();
		com.enioka.jqm.api.JobInstance j = new com.enioka.jqm.api.JobInstance();

		for (JobParameter i : job.getParameters()) {

			parameters.put(i.getKey(), i.getValue());
        }

		j.setId(job.getId());
		j.setJd(jobDefToJobDefinition(job.getJd()));
		j.setParameters(parameters);
		j.setParent(job.getParent().getId());
		j.setPosition(job.getPosition());
		j.setQueue(getQueue(job.getQueue()));
		j.setSessionID(job.getSessionID());
		j.setState(job.getState());
		j.setUser(job.getUser());

		return j;
	}
	// ----------------------------- ENQUEUE --------------------------------------

	public static int enQueue(JobDefinition jd) {

		ArrayList<JobParameter> jps = new ArrayList<JobParameter>();
		ArrayList<JobParameter> newJps = new ArrayList<JobParameter>();
		boolean override = false;
		JobDef job = jobDefinitionToJobDef(jd);

		if (job.getParameters() != null && job.getParameters() != null) {

			for( Iterator<String> i = jd.getParameters().keySet().iterator(); i.hasNext();) {

				boolean change = false;
				String key = i.next();
				String value = jd.getParameters().get(key);

				for (int j = 0; j < job.getParameters().size(); j++) {

					if (job.getParameters().get(j).getValue() == value)
						break;
					else if (j == job.getParameters().size() - 1 && job.getParameters().get(j).getValue() != value) {

						change = true;
						break;
					}
				}

				if (change) {
					override = true;
					for( Iterator<String> k = jd.getParameters().keySet().iterator(); k.hasNext();) {

						String keyk = k.next();
						String valuev = jd.getParameters().get(keyk);

						newJps.add(CreationTools.createJobParameter(keyk, valuev));
					}
					break;
				}
			}
		}
		else {
			if (job.getParameters() != null) {
				for (JobDefParameter j : job.getParameters()) {

					EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
					EntityManager em = emf.createEntityManager();
					JobParameter jp = new JobParameter();
					EntityTransaction transac = em.getTransaction();
					transac.begin();

					jp.setKey(j.getKey());
					jp.setValue(j.getValue());

					em.persist(jp);
					transac.commit();
					em.close();
					emf.close();

					jps.add(jp);
				}
			}
			else {

				for( Iterator<String> i = jd.getParameters().keySet().iterator(); i.hasNext();) {

					String key = i.next();
					String value = jd.getParameters().get(key);

					jps.add(CreationTools.createJobParameter(key, value));
				}
			}
		}

		System.out.println("bool: " + override);
		for (JobParameter jobParameter : newJps) {
			System.out.println("newjps: " + jobParameter.getValue());
		}

		Calendar enqueueDate = GregorianCalendar.getInstance(Locale.getDefault());

		History h = null;
		Integer p = CreationTools.em.createQuery("SELECT MAX (j.position) FROM JobInstance j, JobDef jd " +
				"WHERE j.jd.queue.name = :queue", Integer.class).setParameter("queue", (job.getQueue().getName())).getSingleResult();
		System.out.println("POSITION: " + p);
		JobInstance ji = CreationTools.createJobInstance(job, (override == true) ? newJps : jps, "MAG", 42, "SUBMITTED", (p == null) ? 1 : p + 1, job.queue);

		//CreationTools.em.createQuery("UPDATE JobParameter jp SET jp.jobInstance = :j WHERE").executeUpdate();

		// Update status in the history table
		//System.exit(0);
		Query q = CreationTools.em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", ji.getId());

		if (!q.equals(null)) {

			Message m = null;
			ArrayList<JobHistoryParameter> jhp = new ArrayList<JobHistoryParameter>();

			for (JobParameter j : ji.getParameters()) {

				EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
				EntityManager em = emf.createEntityManager();
				JobHistoryParameter jp = new JobHistoryParameter();
				EntityTransaction transac = em.getTransaction();
				transac.begin();

				jp.setKey(j.getKey());
				jp.setValue(j.getValue());

				em.persist(jp);
				transac.commit();
				em.close();
				emf.close();

				jhp.add(jp);
			}

			ArrayList<Message> msgs = new ArrayList<Message>();

			h = CreationTools.createhistory(1, (Calendar) null, "History of the Job --> ID = " + (ji.getId()),
					msgs, ji, enqueueDate, (Calendar) null, (Calendar) null, jhp);

			m = CreationTools.createMessage("Status updated: SUBMITTED", h);
			msgs.add(m);
		}
		return ji.getId();
	}

	//----------------------------- GETJOB --------------------------------------

	public static com.enioka.jqm.api.JobInstance getJob(int idJob) {

		return getJobInstance(CreationTools.em.createQuery(
				"SELECT j FROM JobInstance j WHERE j.id = :job",
				JobInstance.class)
				.setParameter("job", idJob)
				.getSingleResult());
	}

	//----------------------------- DELJOBINQUEUE --------------------------------------

	public static void delJobInQueue(int idJob) {

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query q = CreationTools.em.createQuery("DELETE FROM JobInstance j WHERE j.id = :idJob").setParameter("idJob", idJob);
		int res = q.executeUpdate();

		if (res != 1)
			System.err.println("delJobInQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();

	}

	//----------------------------- CANCELJOBINQUEUE --------------------------------------

	public static void cancelJobInQueue(int idJob) {

		@SuppressWarnings("unused")
        History h = CreationTools.em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", idJob).getSingleResult();

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query q = CreationTools.em.createQuery("UPDATE JobInstance j SET j.state = 'CANCELLED' WHERE j.id = :idJob").setParameter("idJob", idJob);
		int res = q.executeUpdate();

		CreationTools.em
        .createQuery(
                "UPDATE Message m SET m.textMessage = :msg WHERE m.history.id = "
                        + "(SELECT h.id FROM History h WHERE h.jobInstance.id = :j)")
        .setParameter("j", idJob)
        .setParameter("msg", "Status updated: CANCELLED")
        .executeUpdate();

		if (res != 1)
			System.err.println("CancelJobInQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();

	}

	//----------------------------- STOPJOB --------------------------------------

	public static void stopJob(int idJob) {

	}

	//----------------------------- KILLJOB --------------------------------------

	public static void killJob(int idJob) {

	}

	public static void restartCrashedJob(int idJob) {

	}

	public static int restartJob(int idJob) {

		return 0;
	}

	//----------------------------- SETPOSITION --------------------------------------

	public static void setPosition(int idJob, int position) {

		if (position < 1)
			position = 1;

		List<JobInstance> q = CreationTools.em.createQuery("SELECT j FROM JobInstance j WHERE j.state = :s " +
				"ORDER BY j.position", JobInstance.class).setParameter("s", "SUBMITTED").getResultList();

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query query = CreationTools.em.createQuery("UPDATE JobInstance j SET j.position = :pos WHERE " +
				"j.id = (SELECT ji.id FROM JobInstance ji WHERE ji.id = :idJob)").setParameter("idJob", idJob).setParameter("pos", position);

		@SuppressWarnings("unused")
        int result = query.executeUpdate();

		for (int i = 0; i < q.size(); i++)
		{
			if (q.get(i).getId() == idJob)
				continue;
			else if (i + 1 == position)
			{
				Query queryEg = CreationTools.em.createQuery("UPDATE JobInstance j SET j.position = :i WHERE j.id = :job").setParameter("i",
						position + 2).setParameter("job", q.get(i).getId());
				@SuppressWarnings("unused")
                int res = queryEg.executeUpdate();
				i++;
			}
			else
			{
				Query qq = CreationTools.em.createQuery("UPDATE JobInstance j SET j.position = :i WHERE j.id = :job").setParameter("i",
						i + 1).setParameter("job", q.get(i).getId());
				@SuppressWarnings("unused")
                int res = qq.executeUpdate();

			}
		}

		transac.commit();
	}

	//----------------------------- GETDELIVERABLES --------------------------------------

	public static List<InputStream> getDeliverables(int idJob) throws IOException, NoSuchAlgorithmException {

		URL url = null;
		File file = null;
		ArrayList<InputStream> streams = new ArrayList<InputStream>();
		List<Deliverable> tmp = new ArrayList<Deliverable>();

		try {

			tmp = CreationTools.em.createQuery(
					"SELECT d FROM Deliverable d WHERE d.jobId = :idJob",
					Deliverable.class)
					.setParameter("idJob", idJob)
					.getResultList();

			JobInstance job = CreationTools.em.createQuery(
					"SELECT j FROM JobInstance j WHERE j.id = :job",
					JobInstance.class)
					.setParameter("job", idJob)
					.getSingleResult();

			DeploymentParameter dp = CreationTools.em.createQuery(
					"SELECT dp FROM DeploymentParameter dp WHERE dp.queue.id = :q", DeploymentParameter.class)
					.setParameter("q", job.getJd().getQueue().getId())
					.getSingleResult();

			for (int i = 0; i < tmp.size(); i++) {

				// Ajouter listeninginterface en guise d'adresse (localhost)
				url = new URL(
						"http://" +
								dp.getNode().getListeningInterface() +
								":" +
								dp.getNode().getPort() +
								"/getfile?file=" +
								tmp.get(i).getFileName());

				if (tmp.get(i).getHashPath().equals(Cryptonite.sha1(tmp.get(i).getFileName()))) {

					FileUtils.copyURLToFile(url, file = new File("/Users/pico/Downloads/tests/deliverable" + job.getId()));
					streams.add(new FileInputStream(file));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return streams;
	}

	//----------------------------- GETALLDELIVERABLES --------------------------------------

	public static List<Deliverable> getAllDeliverables(int idJob) {

		ArrayList<Deliverable> deliverables = new ArrayList<Deliverable>();

		deliverables = (ArrayList<Deliverable>) CreationTools.em.createQuery(
				"SELECT d FROM Deliverable d WHERE d.jobInstance = :idJob",
				Deliverable.class)
				.setParameter("idJob", idJob)
				.getResultList();

		return deliverables;
	}

	//----------------------------- GETONEDELIVERABLE --------------------------------------

	public static InputStream getOneDeliverable(Deliverable deliverable) throws NoSuchAlgorithmException, IOException {

		URL url = null;
		File file = null;

		JobInstance job = CreationTools.em.createQuery(
				"SELECT j FROM JobInstance j WHERE j.id = :job",
				JobInstance.class)
				.setParameter("job", deliverable.getJobId())
				.getSingleResult();

		DeploymentParameter dp = CreationTools.em.createQuery(
				"SELECT dp FROM DeploymentParameter dp WHERE dp.queue.id = :q", DeploymentParameter.class)
				.setParameter("q", job.getJd().getQueue().getId())
				.getSingleResult();

		url = new URL(
				"http://" +
						dp.getNode().getListeningInterface() +
						":" + dp.getNode().getPort() +
						"/getfile?file=" +
						deliverable.getFilePath());

		if (deliverable.getHashPath().equals(Cryptonite.sha1(deliverable.getFilePath()))) {

			FileUtils.copyURLToFile(url, file = new File("/Users/pico/Downloads/tests/deliverable" + job.getId()));
		}
		return new FileInputStream(file);
	}

	//----------------------------- GETUSERDELIVERABLES --------------------------------------

	public static List<Deliverable> getUserDeliverables(String user) {

		ArrayList<Deliverable> d = new ArrayList<Deliverable>();

		JobInstance j = CreationTools.em.createQuery(
				"SELECT j FROM JobInstance j WHERE j.user = :u",
				JobInstance.class)
				.setParameter("u", user)
				.getSingleResult();

		d = (ArrayList<Deliverable>) CreationTools.em.createQuery(
				"SELECT d FROM Deliverable WHERE d.jobInstance = :idJob",
				Deliverable.class)
				.setParameter("idJob", j.getId())
				.getResultList();

		return d;
	}

	//----------------------------- GETMSG --------------------------------------

	public static List<String> getMsg(int idJob) { // -------------TODO------------

		ArrayList<String> msgs = new ArrayList<String>();

		return msgs;
	}

	//----------------------------- GETUSERJOBS --------------------------------------

	public static List<JobInstance> getUserJobs(String user) {

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) CreationTools.em.createQuery("SELECT j FROM JobInstance j WHERE j.user = :u", JobInstance.class).setParameter("u", user).getResultList();

		return jobs;
	}

	//----------------------------- GETJOBS --------------------------------------

	public static List<com.enioka.jqm.api.JobInstance> getJobs() {

		ArrayList<com.enioka.jqm.api.JobInstance> res = new ArrayList<com.enioka.jqm.api.JobInstance>();
		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) CreationTools.em.createQuery("SELECT j FROM JobInstance j", JobInstance.class).getResultList();

		for (JobInstance j : jobs) {
	        com.enioka.jqm.api.JobInstance tmp = new com.enioka.jqm.api.JobInstance();

	        tmp.setId(j.getId());
	        tmp.setJd(jobDefToJobDefinition(j.getJd()));
	        if (j.getParent() != null)
	        	tmp.setParent(j.getParent().getId());
	        else
	        	tmp.setParent(null);

	        res.add(tmp);
        }

		return res;
	}

	//----------------------------- GETQUEUES --------------------------------------

	public static List<com.enioka.jqm.api.Queue> getQueues() {

		List<com.enioka.jqm.api.Queue> res = new ArrayList<com.enioka.jqm.api.Queue>();
		ArrayList<Queue> queues = (ArrayList<Queue>) CreationTools.em.createQuery("SELECT j FROM Queue j", Queue.class).getResultList();

		for (Queue queue : queues) {

			com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();

			q = getQueue(queue);

			res.add(q);
        }

		return res;
	}

	//----------------------------- CHANGEQUEUE --------------------------------------

	public static void changeQueue(int idJob, int idQueue) {

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Queue q = CreationTools.em.createQuery("SELECT Queue FROM Queue queue " +
				"WHERE queue.id = :q", Queue.class).setParameter("q", idQueue).getSingleResult();


		Query query = CreationTools.em.createQuery("UPDATE JobInstance j SET j.queue = :q WHERE j.id = :jd").setParameter("q", q).setParameter("jd", idJob);
		int result = query.executeUpdate();

		if (result != 1)
			System.err.println("changeQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();
	}

	//----------------------------- CHANGEQUEUE --------------------------------------

	public static void changeQueue(int idJob, Queue queue) {

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query query = CreationTools.em.createQuery("UPDATE JobInstance j SET j.queue = :q WHERE j.id = :jd").setParameter("q", queue).setParameter("jd", idJob);
		int result = query.executeUpdate();

		if (result != 1)
			System.err.println("changeQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();
	}
}
