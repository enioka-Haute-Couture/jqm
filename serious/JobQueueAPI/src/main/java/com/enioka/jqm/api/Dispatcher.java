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
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.commons.io.FileUtils;

import com.enioka.jqm.hash.Cryptonite;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDefinition;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;

/**
 *
 * @author pierre.coppee
 */
public class Dispatcher {

	public static int enQueue(JobDefinition job) {

		Calendar enqueueDate = GregorianCalendar.getInstance(Locale.getDefault());

		History h = null;
		Integer p = CreationTools.em.createQuery("SELECT MAX (j.position) FROM JobInstance j, JobDefinition jd " +
				"WHERE j.jd.queue.name = :queue", Integer.class).setParameter("queue", (job.getQueue().getName())).getSingleResult();
		System.out.println("POSITION: " + p);
		JobInstance ji = CreationTools.createJobInstance(job, "MAG", 42, "SUBMITTED", (p == null) ? 1 : p + 1, job.queue);

		//CreationTools.em.createQuery("UPDATE JobParameter jp SET jp.jobInstance = :j WHERE").executeUpdate();

		// Update status in the history table

		Query q = CreationTools.em.createQuery("SELECT h FROM History h WHERE h.jobId = :j", History.class).setParameter("j", ji.getId());

		if (!q.equals(null)) {

			Message m = null;

			h = CreationTools.createhistory(1, null, "History of the Job --> ID = " + (ji.getId()),
					m, ji.getId(), enqueueDate, null, null);

			m = CreationTools.createMessage("Status updated: SUBMITTED", h);

		}
		else {
			EntityTransaction transac = CreationTools.em.getTransaction();
			transac.begin();

			h = (History) q.getSingleResult();

			Message m = CreationTools.em.createQuery("SELECT m FROM Message m WHERE m.id = :h",
					Message.class).setParameter("h", h.getMessage().getId()).getSingleResult();

			m.setTextMessage("Status updated: SUBMITTED");

			CreationTools.em.createQuery("UPDATE Message m SET m.textMessage = :msg WHERE" +
					"m.history.id = :h").setParameter("h", h.getId()).setParameter("msg", "Status updated: SUBMITTED").executeUpdate();

			transac.commit();
		}

		return ji.getId();
	}

	public static void delJobInQueue(int idJob) {

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query q = CreationTools.em.createQuery("DELETE FROM JobInstance j WHERE j.id = :idJob").setParameter("idJob", idJob);
		int res = q.executeUpdate();

		if (res != 1)
			System.err.println("delJobInQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();

	}

	public static void cancelJobInQueue(int idJob) {

		@SuppressWarnings("unused")
        History h = CreationTools.em.createQuery("SELECT h FROM History h WHERE h.jobId = :j", History.class).setParameter("j", idJob).getSingleResult();

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query q = CreationTools.em.createQuery("UPDATE JobInstance j SET j.state = 'CANCELLED' WHERE j.id = :idJob").setParameter("idJob", idJob);
		int res = q.executeUpdate();

		CreationTools.em
        .createQuery(
                "UPDATE Message m SET m.textMessage = :msg WHERE m.history.id = "
                        + "(SELECT h.id FROM History h WHERE h.jobId = :j)")
        .setParameter("j", idJob)
        .setParameter("msg", "Status updated: CANCELLED")
        .executeUpdate();

		if (res != 1)
			System.err.println("CancelJobInQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();

	}

	public static void stopJob(int idJob) {

	}

	public static void killJob(int idJob) {

	}

	public static void restartCrashedJob(int idJob) {

	}

	public static int restartJob(int idJob) {

		return 0;
	}

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
					.setParameter("q", job.getQueue().getId())
					.getSingleResult();

			for (int i = 0; i < tmp.size(); i++) {

				// Ajouter listeninginterface en guise d'adresse (localhost)
				url = new URL(
						"http://" +
								dp.getNode().getListeningInterface() +
								":" +
								dp.getNode().getPort() +
								"/getfile?file=" +
								tmp.get(i).getFilePath());

				if (tmp.get(i).getHashPath().equals(Cryptonite.sha1(tmp.get(i).getFilePath()))) {

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

	public static List<Deliverable> getAllDeliverables(int idJob) {

		ArrayList<Deliverable> deliverables = new ArrayList<Deliverable>();

		deliverables = (ArrayList<Deliverable>) CreationTools.em.createQuery(
				"SELECT d FROM Deliverable d WHERE d.jobInstance = :idJob",
				Deliverable.class)
				.setParameter("idJob", idJob)
				.getResultList();

		return deliverables;
	}

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
				.setParameter("q", job.getQueue().getId())
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

	public static List<String> getMsg(int idJob) { // -------------TODO------------

		ArrayList<String> msgs = new ArrayList<String>();

		return msgs;
	}

	public static List<JobInstance> getUserJobs(String user) {

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) CreationTools.em.createQuery("SELECT j FROM JobInstance j WHERE j.user = :u", JobInstance.class).setParameter("u", user).getResultList();

		return jobs;
	}

	public static List<JobInstance> getJobs() {

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) CreationTools.em.createQuery("SELECT j FROM JobInstance j", JobInstance.class).getResultList();

		return jobs;
	}

	public static List<Queue> getQueues() {

		ArrayList<Queue> queues = (ArrayList<Queue>) CreationTools.em.createQuery("SELECT j FROM Queue j", Queue.class).getResultList();

		return queues;
	}

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
