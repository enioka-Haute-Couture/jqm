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
import org.apache.log4j.Logger;

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
public class Dispatcher
{
	private static Logger jqmlogger = Logger.getLogger(Dispatcher.class);
	public static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	public static EntityManager em = emf.createEntityManager();

	public static void resetEM()
	{
		if (em != null)
			em.close();
		if (emf != null)
			emf.close();
		emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
		em = emf.createEntityManager();
	}

	// ----------------------------- JOBDEFINITIONTOJOBDEF --------------------------------------

	private static JobDef jobDefinitionToJobDef(JobDefinition jd)
	{
		JobDef job = null;

		try
		{
			jqmlogger.debug("Retrieving JobDef for Application named " + jd.getApplicationName());
			job = em.createQuery("SELECT j FROM JobDef j WHERE j.applicationName = :name", JobDef.class)
					.setParameter("name", jd.getApplicationName()).getSingleResult();
		} catch (Exception e)
		{
			jqmlogger.error("Could not retrieve JobDef from job instance request", e);
			return null;
		}

		return job;
	}

	private static com.enioka.jqm.api.JobDefinition jobDefToJobDefinition(JobDef jd)
	{

		com.enioka.jqm.api.JobDefinition job = new com.enioka.jqm.api.JobDefinition();
		Map<String, String> h = new HashMap<String, String>();

		if (jd.getParameters() != null)
		{
			for (JobDefParameter i : jd.getParameters())
			{

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

	private static com.enioka.jqm.api.Queue getQueue(Queue queue)
	{

		com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();

		q.setDescription(queue.getDescription());
		q.setId(queue.getId());
		q.setName(queue.getName());

		return q;
	}

	private static com.enioka.jqm.api.JobInstance getJobInstance(JobInstance job)
	{

		Map<String, String> parameters = new HashMap<String, String>();
		com.enioka.jqm.api.JobInstance j = new com.enioka.jqm.api.JobInstance();

		for (JobParameter i : job.getParameters())
		{

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

	public static List<JobParameter> overrideParameter(JobDef jdef, JobDefinition jdefinition)
	{

		Map<String, String> m = jdefinition.getParameters();
		List<JobParameter> res = new ArrayList<JobParameter>();

		if (m == null)
		{
			return res;
		}
		else if (m.isEmpty())
		{
			jqmlogger.debug("Parameters no overriding");
			if (jdef.getParameters() == null)
				return res;

			for (JobDefParameter i : jdef.getParameters())
			{
				res.add(CreationTools.createJobParameter(i.getKey(), i.getValue(), em));
			}

			return res;
		}
		else if (!m.isEmpty() && !jdef.getParameters().isEmpty())
		{
			System.out.println("Parameters overriding");
			for (JobDefParameter i : jdef.getParameters())
			{
				res.add(CreationTools.createJobParameter(i.getKey(), i.getValue(), em));
			}

			for (int j = 0; j < res.size(); j++)
			{
				if (m.containsKey(res.get(j).getKey()))
				{
					for (Iterator<String> i = m.keySet().iterator(); i.hasNext();)
					{
						String key = i.next();
						String value = m.get(key);

						if (res.get(j).getKey().equals(key))
						{
							res.remove(j);
							res.add(CreationTools.createJobParameter(key, value, em));
							break;
						}
					}
				}
			}

			for (Iterator<String> i = m.keySet().iterator(); i.hasNext();)
			{
				String key = i.next();
				String value = m.get(key);

				for (int j = 0; j < res.size(); j++)
				{
					boolean present = false;

					if (res.get(j).getKey().equals(key))
						present = true;

					if (j == res.size() - 1 && !present)
					{
						res.add(CreationTools.createJobParameter(key, value, em));
					}
				}
			}

			// for (JobParameter j : res)
			// {
			// System.out.println("CONTENU PARAMETERS: " + j.getKey() + ", " + j.getValue());
			// }
			System.out.println("Parameters overrided will be returned");
			return res;
		}
		else
		{
			jqmlogger.debug("Parameters will be SUPER overriding");
			for (Iterator<String> i = m.keySet().iterator(); i.hasNext();)
			{

				String key = i.next();
				String value = m.get(key);

				res.add(CreationTools.createJobParameter(key, value, em));
			}

			return res;
		}
	}

	// ----------------------------- ENQUEUE --------------------------------------

	public static int enQueue(JobDefinition jd)
	{
		jqmlogger.debug("DEBUT ENQUEUE");
		JobDef job = jobDefinitionToJobDef(jd);
		jqmlogger.debug("Job to enqueue is from JobDef " + job.getId());

		Calendar enqueueDate = GregorianCalendar.getInstance(Locale.getDefault());

		History h = null;

		Integer p = em.createQuery("SELECT MAX (j.position) FROM JobInstance j " + "WHERE j.jd.queue.name = :queue", Integer.class)
				.setParameter("queue", (job.getQueue().getName())).getSingleResult();

		jqmlogger.debug("POSITION: " + p);

		em.getTransaction().begin();

		JobInstance ji = CreationTools.createJobInstance(job, overrideParameter(job, jd), jd.getUser(), 42, "SUBMITTED", (p == null) ? 1
				: p + 1, job.queue, em);
		jqmlogger.debug("JI QUI VIENT D'ETRE CREE: " + ji.getId());

		ArrayList<JobHistoryParameter> jhp = new ArrayList<JobHistoryParameter>();
		ArrayList<Message> msgs = new ArrayList<Message>();

		h = CreationTools.createhistory(1, (Calendar) null, "History of the Job --> ID = " + (ji.getId()), msgs, ji, enqueueDate,
				(Calendar) null, (Calendar) null, jhp, em);
		jqmlogger.debug("HISTORY QUI VIENT D'ETRE CREE: " + h.getId());
		// CreationTools.em.createQuery("UPDATE JobParameter jp SET jp.jobInstance = :j WHERE").executeUpdate();

		// Update status in the history table
		// System.exit(0);
		Query q = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", ji.getId());

		if (!q.equals(null))
		{
			for (JobParameter j : ji.getParameters())
			{

				JobHistoryParameter jp = new JobHistoryParameter();

				jp.setKey(j.getKey());
				jp.setValue(j.getValue());

				em.persist(jp);

				jhp.add(jp);
			}

		}
		em.getTransaction().commit();
		return ji.getId();
	}

	// ----------------------------- GETJOB --------------------------------------

	public static com.enioka.jqm.api.JobInstance getJob(int idJob)
	{

		return getJobInstance(em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :job", JobInstance.class).setParameter("job", idJob)
				.getSingleResult());
	}

	// ----------------------------- DELJOBINQUEUE --------------------------------------

	public static void delJobInQueue(int idJob)
	{

		EntityTransaction transac = em.getTransaction();
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);
		Query q = null;

		transac.begin();

		try
		{
			q = em.createQuery("DELETE FROM JobInstance j WHERE j.id = :idJob").setParameter("idJob", idJob);
		} catch (Exception e)
		{
			jqmlogger.info("delJobInQueue: " + e);
		}
		int res = q.executeUpdate();

		if (res != 1)
			jqmlogger.info("delJobInQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();

	}

	// ----------------------------- CANCELJOBINQUEUE --------------------------------------

	public static void cancelJobInQueue(int idJob)
	{
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);
		try
		{
			@SuppressWarnings("unused")
			History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", idJob)
					.getSingleResult();

			EntityTransaction transac = em.getTransaction();
			transac.begin();

			Query q = em.createQuery("UPDATE JobInstance j SET j.state = 'CANCELLED' WHERE j.id = :idJob").setParameter("idJob", idJob);
			int res = q.executeUpdate();

			em.createQuery(
					"UPDATE Message m SET m.textMessage = :msg WHERE m.history.id = "
							+ "(SELECT h.id FROM History h WHERE h.jobInstance.id = :j)").setParameter("j", idJob)
					.setParameter("msg", "Status updated: CANCELLED").executeUpdate();

			if (res != 1)
				jqmlogger.info("CancelJobInQueueError: Job ID or Queue ID doesn't exists.");

			transac.commit();
		} catch (Exception e)
		{
			jqmlogger.info("cancelJobInnQueue" + e);
		}

	}

	// ----------------------------- STOPJOB --------------------------------------

	public static void stopJob(int idJob)
	{

	}

	// ----------------------------- KILLJOB --------------------------------------

	public static void killJob(int idJob)
	{

	}

	public static void restartCrashedJob(int idJob)
	{

	}

	public static int restartJob(int idJob)
	{

		return 0;
	}

	// ----------------------------- SETPOSITION --------------------------------------

	public static void setPosition(int idJob, int position)
	{
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);
		List<JobInstance> q = null;
		EntityTransaction transac = em.getTransaction();
		Query query = null;

		if (position < 1)
			position = 1;

		try
		{
			q = em.createQuery("SELECT j FROM JobInstance j WHERE j.state = :s " + "ORDER BY j.position", JobInstance.class)
					.setParameter("s", "SUBMITTED").getResultList();

			transac.begin();

			query = em
					.createQuery(
							"UPDATE JobInstance j SET j.position = :pos WHERE "
									+ "j.id = (SELECT ji.id FROM JobInstance ji WHERE ji.id = :idJob)").setParameter("idJob", idJob)
					.setParameter("pos", position);

			@SuppressWarnings("unused")
			int result = query.executeUpdate();

			for (int i = 0; i < q.size(); i++)
			{
				if (q.get(i).getId() == idJob)
					continue;
				else if (i + 1 == position)
				{
					Query queryEg = em.createQuery("UPDATE JobInstance j SET j.position = :i WHERE j.id = :job")
							.setParameter("i", position + 2).setParameter("job", q.get(i).getId());
					@SuppressWarnings("unused")
					int res = queryEg.executeUpdate();
					i++;
				}
				else
				{
					Query qq = em.createQuery("UPDATE JobInstance j SET j.position = :i WHERE j.id = :job").setParameter("i", i + 1)
							.setParameter("job", q.get(i).getId());
					@SuppressWarnings("unused")
					int res = qq.executeUpdate();

				}
			}
		} catch (Exception e)
		{
			jqmlogger.info("setPosition: " + e);
		}

		transac.commit();
	}

	// ----------------------------- GETDELIVERABLES --------------------------------------

	public static List<InputStream> getDeliverables(int idJob) throws IOException, NoSuchAlgorithmException
	{

		URL url = null;
		File file = null;
		ArrayList<InputStream> streams = new ArrayList<InputStream>();
		List<Deliverable> tmp = new ArrayList<Deliverable>();
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);

		try
		{

			tmp = em.createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class).setParameter("idJob", idJob)
					.getResultList();

			jqmlogger.debug("idJob of the deliverable: " + idJob);
			jqmlogger.debug("size of the deliverable list: " + tmp.size());

			JobInstance job = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :job", JobInstance.class).setParameter("job", idJob)
					.getSingleResult();

			DeploymentParameter dp = em
					.createQuery("SELECT dp FROM DeploymentParameter dp WHERE dp.queue.id = :q", DeploymentParameter.class)
					.setParameter("q", job.getJd().getQueue().getId()).getSingleResult();

			for (int i = 0; i < tmp.size(); i++)
			{

				url = new URL("http://" + dp.getNode().getListeningInterface() + ":" + dp.getNode().getPort() + "/getfile?file="
						+ tmp.get(i).getFilePath() + tmp.get(i).getFileName());

				if (tmp.get(i).getHashPath().equals(Cryptonite.sha1(tmp.get(i).getFilePath() + tmp.get(i).getFileName())))
				{
					// mettre en base le repertoire de dl
					jqmlogger.debug("dlRepository: " + dp.getNode().getDlRepo() + tmp.get(i).getFileFamily() + "/" + job.getId() + "/");
					File dlRepo = new File(dp.getNode().getDlRepo() + tmp.get(i).getFileFamily() + "/" + job.getId() + "/");
					dlRepo.mkdirs();
					file = new File(dp.getNode().getDlRepo() + tmp.get(i).getFileFamily() + "/" + job.getId() + "/"
							+ tmp.get(i).getFileName());

					FileUtils.copyURLToFile(url, file);
					streams.add(new FileInputStream(file));
				}
				else
				{
					jqmlogger.info("GetDeliverables: You are not the owner of this document.");
					return null;
				}
			}
		} catch (FileNotFoundException e)
		{

			jqmlogger.info(e);
		} catch (Exception e)
		{

			jqmlogger.info("No deliverable available", e);
		}

		return streams;
	}

	// ----------------------------- GETALLDELIVERABLES --------------------------------------

	public static List<Deliverable> getAllDeliverables(int idJob)
	{
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);
		ArrayList<Deliverable> deliverables = new ArrayList<Deliverable>();

		try
		{
			deliverables = (ArrayList<Deliverable>) em
					.createQuery("SELECT d FROM Deliverable d WHERE d.jobInstance = :idJob", Deliverable.class)
					.setParameter("idJob", idJob).getResultList();
		} catch (Exception e)
		{
			jqmlogger.info(e);
		}

		return deliverables;
	}

	// ----------------------------- GETONEDELIVERABLE --------------------------------------

	public static InputStream getOneDeliverable(com.enioka.jqm.api.Deliverable d) throws NoSuchAlgorithmException, IOException
	{

		URL url = null;
		File file = null;
		JobInstance job = null;
		Deliverable deliverable = null;
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);

		try
		{
			deliverable = em.createQuery("SELECT d FROM Deliverable d WHERE d.filePath = :f AND d.fileName = :fn", Deliverable.class)
					.setParameter("f", d.getFilePath()).setParameter("fn", d.getFileName()).getSingleResult();
		} catch (Exception e)
		{
			jqmlogger.info(e);
		}

		try
		{
			job = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :job", JobInstance.class)
					.setParameter("job", deliverable.getJobId()).getSingleResult();
		} catch (Exception e)
		{
			job = null;
			jqmlogger.info("GetOneDeliverable: No job found with the deliverable ID");
		}

		DeploymentParameter dp = em.createQuery("SELECT dp FROM DeploymentParameter dp WHERE dp.queue.id = :q", DeploymentParameter.class)
				.setParameter("q", job.getJd().getQueue().getId()).getSingleResult();

		url = new URL("http://" + dp.getNode().getListeningInterface() + ":" + dp.getNode().getPort() + "/getfile?file="
				+ deliverable.getFilePath() + deliverable.getFileName());

		jqmlogger.debug("URL: " + deliverable.getFilePath() + deliverable.getFileName());

		if (deliverable.getHashPath().equals(Cryptonite.sha1(deliverable.getFilePath() + deliverable.getFileName())) && job != null)
		{
			System.out.println("dlRepo: " + dp.getNode().getDlRepo() + deliverable.getFileFamily() + "/" + job.getId() + "/");
			File dlRepo = new File(dp.getNode().getDlRepo() + deliverable.getFileFamily() + "/" + job.getId() + "/");
			dlRepo.mkdirs();
			file = new File(dp.getNode().getDlRepo() + deliverable.getFileFamily() + "/" + job.getId() + "/" + deliverable.getFileName());
			FileUtils.copyURLToFile(url, file);
		}
		else
		{
			jqmlogger.info("GetOneDeliverable: You are not the owner of this document.");
			return null;
		}
		return (new FileInputStream(file));
	}

	// ----------------------------- GETUSERDELIVERABLES --------------------------------------

	public static List<Deliverable> getUserDeliverables(String user)
	{
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);
		ArrayList<Deliverable> d = new ArrayList<Deliverable>();
		ArrayList<Deliverable> res = new ArrayList<Deliverable>();
		ArrayList<JobInstance> j = null;

		try
		{
			j = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j WHERE j.user = :u", JobInstance.class)
					.setParameter("u", user).getResultList();
		} catch (Exception e)
		{
			jqmlogger.info(e);
		}

		for (int i = 0; i < j.size(); i++)
		{

			d = (ArrayList<Deliverable>) em.createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class)
					.setParameter("idJob", j.get(i).getId()).getResultList();

			res.addAll(d);
		}
		return res;
	}

	// ----------------------------- GETMSG --------------------------------------

	public static List<String> getMsg(int idJob)
	{ // -------------TODO------------

		ArrayList<String> msgs = new ArrayList<String>();

		return msgs;
	}

	// ----------------------------- GETUSERJOBS --------------------------------------

	public static List<JobInstance> getUserJobs(String user)
	{
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);
		ArrayList<JobInstance> jobs = null;

		try
		{
			jobs = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j WHERE j.user = :u", JobInstance.class)
					.setParameter("u", user).getResultList();
		} catch (Exception e)
		{
			jqmlogger.info(e);
		}

		return jobs;
	}

	// ----------------------------- GETJOBS --------------------------------------

	public static List<com.enioka.jqm.api.JobInstance> getJobs()
	{
		ArrayList<com.enioka.jqm.api.JobInstance> res = new ArrayList<com.enioka.jqm.api.JobInstance>();

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j", JobInstance.class)
				.getResultList();

		for (JobInstance j : jobs)
		{
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

	// ----------------------------- GETQUEUES --------------------------------------

	public static List<com.enioka.jqm.api.Queue> getQueues()
	{

		List<com.enioka.jqm.api.Queue> res = new ArrayList<com.enioka.jqm.api.Queue>();
		ArrayList<Queue> queues = (ArrayList<Queue>) em.createQuery("SELECT j FROM Queue j", Queue.class).getResultList();

		for (Queue queue : queues)
		{

			com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();

			q = getQueue(queue);

			res.add(q);
		}

		return res;
	}

	// ----------------------------- CHANGEQUEUE --------------------------------------

	public static void changeQueue(int idJob, int idQueue)
	{
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		try
		{
			Queue q = em.createQuery("SELECT Queue FROM Queue queue " + "WHERE queue.id = :q", Queue.class).setParameter("q", idQueue)
					.getSingleResult();

			Query query = em.createQuery("UPDATE JobInstance j SET j.queue = :q WHERE j.id = :jd").setParameter("q", q)
					.setParameter("jd", idJob);
			int result = query.executeUpdate();

			if (result != 1)
				jqmlogger.info("changeQueueError: Job ID or Queue ID doesn't exists.");
		} catch (Exception e)
		{
			jqmlogger.info(e);
		}

		transac.commit();
	}

	// ----------------------------- CHANGEQUEUE --------------------------------------

	public static void changeQueue(int idJob, Queue queue)
	{
		Logger jqmlogger = Logger.getLogger(Dispatcher.class);
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		try
		{
			Query query = em.createQuery("UPDATE JobInstance j SET j.queue = :q WHERE j.id = :jd").setParameter("q", queue)
					.setParameter("jd", idJob);
			int result = query.executeUpdate();

			if (result != 1)
				jqmlogger.info("changeQueueError: Job ID or Queue ID doesn't exists.");
		} catch (Exception e)
		{
			jqmlogger.info(e);
		}

		transac.commit();
	}
}
