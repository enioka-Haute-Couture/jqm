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
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobHistoryParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Queue;

/**
 * Main JQM client API entry point.
 */
public final class Dispatcher
{
	private static Logger jqmlogger = Logger.getLogger(Dispatcher.class);
	private static final String PERSISTENCE_UNIT = "jobqueue-api-pu";

	private Dispatcher()
	{
	}

	private static EntityManagerFactory emf = null;

	/**
	 * For tests only. Never use in production code.
	 */
	public static void resetEM()
	{
		if (emf != null)
		{
			emf.close();
		}
		emf = createFactory();
	}

	private static EntityManagerFactory createFactory()
	{
		Properties p = new Properties();
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream("conf/db.properties");
			p.load(fis);
			IOUtils.closeQuietly(fis);
			return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, p);
		} catch (FileNotFoundException e)
		{
			// No properties file means we use the test HSQLDB (file, not in-memory) as specified in the persistence.xml
			IOUtils.closeQuietly(fis);
			try
			{
				jqmlogger.info("New EMF will be created");
				return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
			} catch (Exception ex)
			{
				jqmlogger.fatal("Could not create EMF", ex);
				return null;
			}
		} catch (IOException e)
		{
			jqmlogger.fatal("conf/db.properties file is invalid", e);
			IOUtils.closeQuietly(fis);
			System.exit(1);
			// Stupid, just for Eclipse's parser and therefore avoid red lines...
			return null;
		}
	}

	static EntityManager getEm()
	{
		if (emf == null)
		{
			emf = createFactory();
		}

		try
		{
			jqmlogger.debug("A new EM will be created");
			return emf.createEntityManager();
		} catch (Exception e)
		{
			jqmlogger.fatal("Could not create EM. Exiting.", e);
			System.exit(2);
			return null;
		}
	}

	// ----------------------------- JOBDEFINITIONTOJOBDEF --------------------------------------

	private static JobDef jobDefinitionToJobDef(JobDefinition jd, EntityManager em)
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
		j.setUser(job.getUserName());

		return j;
	}

	private static List<JobParameter> overrideParameter(JobDef jdef, JobDefinition jdefinition, EntityManager em)
	{
		Map<String, String> m = jdefinition.getParameters();
		List<JobParameter> res = new ArrayList<JobParameter>();

		if (m == null)
		{
			m = new HashMap<String, String>();
		}
		if (m.isEmpty())
		{
			jqmlogger.debug("Parameters no overriding");
			if (jdef.getParameters() == null)
			{
				return res;
			}

			for (JobDefParameter i : jdef.getParameters())
			{
				res.add(createJobParameter(i.getKey(), i.getValue(), em));
			}
			return res;
		}
		else if (!m.isEmpty() && !jdef.getParameters().isEmpty())
		{
			jqmlogger.debug("Parameters overriding");

			for (JobDefParameter i : jdef.getParameters())
			{
				res.add(createJobParameter(i.getKey(), i.getValue(), em));
			}

			for (int j = 0; j < res.size(); j++)
			{
				if (m.containsKey(res.get(j).getKey()))
				{
					for (Entry<String, String> e : m.entrySet())
					{
						String key = e.getKey();
						String value = e.getValue();

						if (res.get(j).getKey().equals(key))
						{
							res.remove(j);
							res.add(createJobParameter(key, value, em));
							break;
						}
					}
				}
			}

			for (Entry<String, String> e : m.entrySet())
			{
				String key = e.getKey();
				String value = e.getValue();

				for (int j = 0; j < res.size(); j++)
				{
					boolean present = false;

					if (res.get(j).getKey().equals(key))
					{
						present = true;
					}

					if (j == res.size() - 1 && !present)
					{
						res.add(createJobParameter(key, value, em));
					}
				}
			}

			// for (JobParameter j : res)
			// {
			// System.out.println("CONTENU PARAMETERS: " + j.getKey() + ", " + j.getValue());
			// }
			jqmlogger.debug("Parameters overrided will be returned: " + res.size());
			return res;
		}
		else
		{
			jqmlogger.debug("Parameters will be SUPER overriding");
			for (Entry<String, String> e : m.entrySet())
			{
				String key = e.getKey();
				String value = e.getValue();

				res.add(createJobParameter(key, value, em));
			}

			return res;
		}
	}

	// ----------------------------- ENQUEUE --------------------------------------

	/**
	 * Ask for a new job instance, as described in the parameter object
	 * 
	 * @param jd
	 *            the description of the desired job instance
	 * @return
	 */
	public static int enQueue(JobDefinition jd)
	{
		jqmlogger.debug("BEGINING ENQUEUE");
		EntityManager em = getEm();
		JobDef job = jobDefinitionToJobDef(jd, em);
		jqmlogger.debug("Job to enqueue is from JobDef " + job.getId());
		Integer hl = null;

		Calendar enqueueDate = GregorianCalendar.getInstance(Locale.getDefault());
		Date date = enqueueDate.getTime();

		History h = null;

		Integer p = em.createQuery("SELECT MAX (j.position) FROM JobInstance j " + "WHERE j.jd.queue.name = :queue", Integer.class)
				.setParameter("queue", (job.getQueue().getName())).getSingleResult();
		jqmlogger.debug("POSITION: " + p);

		if (job.isHighlander())
		{
			hl = highlanderMode(job, em);
		}

		if (hl != null)
		{
			jqmlogger.debug("JI can't be enqueue because a job in the highlander mode is currently submitted: " + hl);
			return hl;
		}
		jqmlogger.debug("Not in highlander mode");

		em.getTransaction().begin();

		JobInstance ji = new JobInstance();
		List<JobParameter> jps = overrideParameter(job, jd, em);
		for (JobParameter jp : jps)
		{
			jqmlogger.debug("Parameter: " + jp.getKey() + " - " + jp.getValue());
		}
		ji.setJd(job);
		ji.setSessionID(42);
		ji.setUserName(jd.getUser());
		ji.setState("SUBMITTED");
		ji.setPosition((p == null) ? 1 : p + 1);
		ji.setQueue(job.getQueue());
		ji.setNode(null);
		if (jd.getEmail() == null)
		{
			ji.setEmail(null);
		}
		else
		{
			ji.setEmail(jd.getEmail());
		}

		em.persist(ji);
		ji.setParameters(jps);
		jqmlogger.debug("JI recently created: " + ji.getId());

		h = new History();
		// h.setReturnedValue(null);
		// h.setJobDate(jobDate);
		h.setJd(job);
		h.setSessionId(ji.getSessionID());
		h.setQueue(job.getQueue());
		h.setMessages(new ArrayList<Message>());
		h.setJobInstance(ji);
		h.setEnqueueDate(enqueueDate);
		// h.setExecutionDate(executionDate);
		// h.setEndDate(endDate);
		h.setUserName(jd.getUser());
		h.setEmail(ji.getEmail());
		h.setPosition(ji.getPosition());
		// h.setNode(null);
		h.setParameters(new ArrayList<JobHistoryParameter>());
		em.persist(h);

		jqmlogger.debug("History recently created: " + h.getId());

		for (JobParameter j : ji.getParameters())
		{
			JobHistoryParameter jp = new JobHistoryParameter();
			jp.setKey(j.getKey());
			jp.setValue(j.getValue());
			j.setJobinstance(ji);

			em.persist(jp);
			h.getParameters().add(jp);
		}

		em.getTransaction().commit();
		em.close();
		return ji.getId();
	}

	// ----------------------------- HIGHLANDER --------------------------------------

	private static Integer highlanderMode(JobDef jd, EntityManager em)
	{
		Integer res = null;

		jqmlogger.debug("Highlander mode is begining");
		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em
				.createQuery("SELECT j FROM JobInstance j WHERE j.jd.applicationName = :j", JobInstance.class)
				.setParameter("j", jd.getApplicationName()).getResultList();

		for (JobInstance j : jobs)
		{
			jqmlogger.debug("JI seeing by highlander: " + j.getId() + j.getState());
			if (j.getState().equals("SUBMITTED"))
			{
				// must be canceld and enqueue must returned the id of the submitted JI
				jqmlogger.debug("In the highlander if");
				res = j.getId();
			}
		}
		jqmlogger.debug("Highlander mode will returned: " + res);
		return res;
	}

	// ----------------------------- GETJOB --------------------------------------

	/**
	 * Retrieve a job instance. The returned object will contain all relevant data on that job instance such as its status.
	 * 
	 * @param idJob
	 * @return
	 */
	public static com.enioka.jqm.api.JobInstance getJob(int idJob)
	{

		return getJobInstance(getEm().createQuery("SELECT j FROM JobInstance j WHERE j.id = :job", JobInstance.class)
				.setParameter("job", idJob).getSingleResult());
	}

	// ----------------------------- DELJOBINQUEUE --------------------------------------

	/**
	 * Remove an enqueued job from the queue. Should not be called of the job is already running, but will not throw any exceptions in that
	 * case.
	 * 
	 * @param idJob
	 */
	public static void delJobInQueue(int idJob)
	{
		EntityManager em = getEm();
		EntityTransaction transac = em.getTransaction();
		Query q = null;

		transac.begin();

		try
		{
			q = em.createQuery("DELETE FROM JobInstance j WHERE j.id = :idJob").setParameter("idJob", idJob);
		} catch (Exception e)
		{
			jqmlogger.info("delJobInQueue: " + e);
			throw new JqmException("Could not create a query on JobInstance", e);
		}
		int res = q.executeUpdate();

		if (res != 1)
		{
			jqmlogger.info("delJobInQueueError: Job ID or Queue ID doesn't exists.");
		}

		transac.commit();
		em.close();
	}

	// ----------------------------- CANCELJOBINQUEUE --------------------------------------

	/**
	 * Cancel a job without removing it from the queue.
	 * 
	 * @param idJob
	 */
	public static void cancelJobInQueue(int idJob)
	{
		EntityManager em = getEm();
		try
		{
			@SuppressWarnings("unused")
			History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", idJob)
			.getSingleResult();

			JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :id", JobInstance.class).setParameter("id", idJob)
					.getSingleResult();

			EntityTransaction transac = em.getTransaction();
			transac.begin();

			Query q = em.createQuery("UPDATE JobInstance j SET j.state = 'CANCELLED' WHERE j.id = :idJob").setParameter("idJob", idJob);
			int res = q.executeUpdate();

			em.createQuery(
					"UPDATE Message m SET m.textMessage = :msg WHERE m.history.id = "
							+ "(SELECT h.id FROM History h WHERE h.jobInstance.id = :j)").setParameter("j", idJob)
							.setParameter("msg", "Status updated: CANCELLED by the user: " + ji.getUserName()).executeUpdate();

			if (res != 1)
			{
				jqmlogger.error("CancelJobInQueueError: Job ID or Queue ID doesn't exists.");
			}

			transac.commit();
		} catch (Exception e)
		{
			throw new JqmException("cancelJobInQueue", e);
		} finally
		{
			em.close();
		}

	}

	// ----------------------------- STOPJOB --------------------------------------

	/**
	 * Nicely stop a running job (asks the job to stop itself - this will usually not do anything as most jobs do not implement this
	 * function)
	 * 
	 * @param idJob
	 */
	public static void stopJob(int idJob)
	{

	}

	// ----------------------------- KILLJOB --------------------------------------

	/**
	 * Kill a running job. Kill is not immediate, and is only possible when a job calls some JQM APIs. If none are called, the job cannot be
	 * killed.
	 * 
	 * @param idJob
	 */
	public static void killJob(int idJob)
	{
		jqmlogger.debug("The job (ID: " + idJob + ")" + " will be killed");
		EntityManager em = getEm();
		JobInstance j = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :i", JobInstance.class).setParameter("i", idJob)
				.getSingleResult();
		History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", idJob)
				.getSingleResult();
		em.getTransaction().begin();
		j.setState("KILLED");
		Message m = new Message();
		m.setHistory(h);
		m.setTextMessage("Status updated: ENDED");
		em.persist(m);
		em.getTransaction().commit();
		JobInstance jj = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :i", JobInstance.class).setParameter("i", idJob)
				.getSingleResult();
		jqmlogger.debug("Job status after killJob: " + jj.getState());
	}

	/**
	 * Will restart a crashed job. This will eventually update the history and make the error disappear from the logs should the job end OK
	 * this time.
	 * 
	 * @param idJob
	 */
	public static void restartCrashedJob(int idJob)
	{
		EntityManager em = getEm();

		History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", idJob)
				.getSingleResult();

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :id", JobInstance.class).setParameter("id", idJob)
				.getSingleResult();

		if (!ji.getJd().isCanBeRestarted())
		{
			return;
		}

		JobDefinition j = new JobDefinition(ji.getJd().getApplicationName(), h.getUserName());

		for (JobHistoryParameter i : h.getParameters())
		{
			j.addParameter(i.getKey(), i.getValue());
		}

		Dispatcher.enQueue(j);
	}

	/**
	 * Will enqueue (copy) a job once again with the same parameters and environment. This will not destroy the log of the original job
	 * instance.
	 * 
	 * @param idJob
	 * @return
	 */
	public static int restartJob(int idJob)
	{
		EntityManager em = getEm();

		History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :j", History.class).setParameter("j", idJob)
				.getSingleResult();

		JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :id", JobInstance.class).setParameter("id", idJob)
				.getSingleResult();

		JobDefinition j = new JobDefinition(ji.getJd().getApplicationName(), h.getUserName());

		for (JobHistoryParameter i : h.getParameters())
		{
			j.addParameter(i.getKey(), i.getValue());
		}

		return Dispatcher.enQueue(j);

	}

	// ----------------------------- SETPOSITION --------------------------------------

	/**
	 * Change the position of a job instance inside a queue.
	 * 
	 * @param idJob
	 * @param position
	 */
	public static void setPosition(int idJob, int position)
	{
		List<JobInstance> q = null;
		EntityManager em = getEm();
		EntityTransaction transac = em.getTransaction();
		Query query = null;
		int newPos = position;

		if (newPos < 1)
		{
			newPos = 1;
		}

		try
		{
			q = em.createQuery("SELECT j FROM JobInstance j WHERE j.state = :s " + "ORDER BY j.position", JobInstance.class)
					.setParameter("s", "SUBMITTED").getResultList();

			transac.begin();

			query = em
					.createQuery(
							"UPDATE JobInstance j SET j.position = :pos WHERE "
									+ "j.id = (SELECT ji.id FROM JobInstance ji WHERE ji.id = :idJob)").setParameter("idJob", idJob)
									.setParameter("pos", newPos);

			@SuppressWarnings("unused")
			int result = query.executeUpdate();

			for (int i = 0; i < q.size(); i++)
			{
				if (q.get(i).getId() == idJob)
				{
					continue;
				}
				else if (i + 1 == newPos)
				{
					Query queryEg = em.createQuery("UPDATE JobInstance j SET j.position = :i WHERE j.id = :job")
							.setParameter("i", newPos + 2).setParameter("job", q.get(i).getId());
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
			throw new JqmException("Position can't be setted", e);
		} finally
		{
			em.close();
		}

		transac.commit();
	}

	// ----------------------------- GETDELIVERABLES --------------------------------------

	/**
	 * Retrieve a list of all deliverables produced by a job instance as OutputStreams.
	 * 
	 * @param idJob
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static List<InputStream> getDeliverables(int idJob) throws IOException, NoSuchAlgorithmException
	{
		EntityManager em = getEm();
		URL url = null;
		File file = null;
		ArrayList<InputStream> streams = new ArrayList<InputStream>();
		List<Deliverable> tmp = null;

		try
		{
			tmp = em.createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class).setParameter("idJob", idJob)
					.getResultList();

			jqmlogger.debug("idJob of the deliverable: " + idJob);
			jqmlogger.debug("size of the deliverable list: " + tmp.size());

			History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :job", History.class).setParameter("job", idJob)
					.getSingleResult();

			for (int i = 0; i < tmp.size(); i++)
			{

				url = new URL("http://" + h.getJobInstance().getNode().getListeningInterface() + ":"
						+ h.getJobInstance().getNode().getPort() + "/getfile?file=" + tmp.get(i).getFilePath() + tmp.get(i).getFileName());

				if (tmp.get(i).getHashPath().equals(Cryptonite.sha1(tmp.get(i).getFilePath() + tmp.get(i).getFileName())))
				{
					// mettre en base le repertoire de dl
					jqmlogger.debug("dlRepository: " + h.getJobInstance().getNode().getDlRepo() + tmp.get(i).getFileFamily() + "/"
							+ h.getJobInstance().getId() + "/");
					File dlRepo = new File(h.getJobInstance().getNode().getDlRepo() + tmp.get(i).getFileFamily() + "/"
							+ h.getJobInstance().getId() + "/");
					dlRepo.mkdirs();
					file = new File(h.getJobInstance().getNode().getDlRepo() + tmp.get(i).getFileFamily() + "/"
							+ h.getJobInstance().getId() + "/" + tmp.get(i).getFileName());

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
			throw new JqmException("The deliverable can't be found", e);
		} catch (Exception e)
		{
			throw new JqmException("The deliverable is not available", e);
		} finally
		{
			em.close();
		}

		return streams;
	}

	// ----------------------------- GETALLDELIVERABLES --------------------------------------

	/**
	 * Get a list of deliverables description produced by a job instance.
	 * 
	 * @param idJob
	 * @return
	 */
	public static List<com.enioka.jqm.api.Deliverable> getAllDeliverables(int idJob)
	{
		ArrayList<Deliverable> deliverables = new ArrayList<Deliverable>();

		try
		{
			deliverables = (ArrayList<Deliverable>) getEm()
					.createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class)
					.setParameter("idJob", idJob).getResultList();
		} catch (Exception e)
		{
			throw new JqmException("Deliverables cannot be found", e);
		}

		List<com.enioka.jqm.api.Deliverable> res = new ArrayList<com.enioka.jqm.api.Deliverable>();

		for (Deliverable d : deliverables)
		{
			res.add(new com.enioka.jqm.api.Deliverable(d.getFilePath(), d.getFileName()));
		}

		return res;
	}

	// ----------------------------- GETONEDELIVERABLE --------------------------------------

	/**
	 * Retrieve one deliverable from JQM as a Stream.
	 * 
	 * @param d
	 * @return
	 * @throws Exception
	 */
	public static InputStream getOneDeliverable(com.enioka.jqm.api.Deliverable d) throws JqmException
	{
		EntityManager em = getEm();
		URL url = null;
		File file = null;
		History h = null;
		Deliverable deliverable = null;

		try
		{
			deliverable = em.createQuery("SELECT d FROM Deliverable d WHERE d.filePath = :f AND d.fileName = :fn", Deliverable.class)
					.setParameter("f", d.getFilePath()).setParameter("fn", d.getFileName()).getSingleResult();
		} catch (Exception e)
		{
			jqmlogger.info(e);
			em.close();
			throw new JqmException("Could not get find deliverable description inside DB", e);
		}

		try
		{
			h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :job", History.class)
					.setParameter("job", deliverable.getJobId()).getSingleResult();
		} catch (Exception e)
		{
			h = null;
			jqmlogger.info("GetOneDeliverable: No job found with the deliverable ID");
			em.close();
			throw new JqmException("No job found with the deliverable ID", e);
		}

		try
		{
			url = new URL("http://" + h.getJobInstance().getNode().getListeningInterface() + ":" + h.getJobInstance().getNode().getPort()
					+ "/getfile?file=" + deliverable.getFilePath() + deliverable.getFileName());
			jqmlogger.debug("URL: " + deliverable.getFilePath() + deliverable.getFileName());
		} catch (MalformedURLException e)
		{
			throw new JqmException("URL is not valid " + url, e);
		}

		if (deliverable.getHashPath().equals(Cryptonite.sha1(deliverable.getFilePath() + deliverable.getFileName()))
				&& h.getJobInstance() != null)
		{
			jqmlogger.debug("dlRepo: " + h.getJobInstance().getNode().getDlRepo() + deliverable.getFileFamily() + "/"
					+ h.getJobInstance().getId() + "/");
			File dlRepo = new File(h.getJobInstance().getNode().getDlRepo() + deliverable.getFileFamily() + "/"
					+ h.getJobInstance().getId() + "/");
			dlRepo.mkdirs();
			try
			{
				file = new File(h.getJobInstance().getNode().getDlRepo() + deliverable.getFileFamily() + "/" + h.getJobInstance().getId()
						+ "/" + deliverable.getFileName());
				FileUtils.copyURLToFile(url, file);
			} catch (IOException e)
			{
				throw new JqmException("Could not copy the downloaded file", e);
			}
		}
		else
		{
			jqmlogger.info("GetOneDeliverable: You are not the owner of this document.");
			return null;
		}
		em.close();

		FileInputStream res = null;
		try
		{
			res = new FileInputStream(file);
		} catch (IOException e)
		{
			throw new JqmException("Could not copy the downloaded file", e);
		}
		return res;
	}

	// ----------------------------- GETUSERDELIVERABLES --------------------------------------

	public static List<Deliverable> getUserDeliverables(String user)
	{
		EntityManager em = getEm();
		ArrayList<Deliverable> d = null;
		ArrayList<Deliverable> res = new ArrayList<Deliverable>();
		ArrayList<History> h = null;

		try
		{
			h = (ArrayList<History>) em.createQuery("SELECT h FROM History h WHERE h.userName = :u", History.class).setParameter("u", user)
					.getResultList();
		} catch (Exception e)
		{
			throw new JqmException("Could not find history inside the database", e);
		}

		for (int i = 0; i < h.size(); i++)
		{
			d = (ArrayList<Deliverable>) em.createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class)
					.setParameter("idJob", h.get(i).getJobInstance().getId()).getResultList();
			res.addAll(d);
		}
		em.close();
		return res;
	}

	// ----------------------------- GETMSG --------------------------------------

	public static List<String> getMsg(int idJob)
	{
		// -------------TODO------------

		ArrayList<String> msgs = new ArrayList<String>();

		return msgs;
	}

	// ----------------------------- GETUSERJOBS --------------------------------------

	/**
	 * List all active job instances created by a given user
	 * 
	 * @param user
	 * @return
	 */
	public static List<JobInstance> getUserJobs(String user)
	{
		ArrayList<JobInstance> jobs = null;

		try
		{
			jobs = (ArrayList<JobInstance>) getEm().createQuery("SELECT j FROM JobInstance j WHERE j.userName = :u", JobInstance.class)
					.setParameter("u", user).getResultList();
		} catch (Exception e)
		{
			throw new JqmException("The user cannot be found", e);
		}

		return jobs;
	}

	// ----------------------------- GETJOBS --------------------------------------

	/**
	 * List all active job instances
	 * 
	 * @return
	 */
	public static List<com.enioka.jqm.api.JobInstance> getJobs()
	{
		ArrayList<com.enioka.jqm.api.JobInstance> res = new ArrayList<com.enioka.jqm.api.JobInstance>();
		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) getEm().createQuery("SELECT j FROM JobInstance j", JobInstance.class)
				.getResultList();

		for (JobInstance j : jobs)
		{
			com.enioka.jqm.api.JobInstance tmp = new com.enioka.jqm.api.JobInstance();

			tmp.setId(j.getId());
			tmp.setJd(jobDefToJobDefinition(j.getJd()));
			if (j.getParent() != null)
			{
				tmp.setParent(j.getParent().getId());
			}
			else
			{
				tmp.setParent(null);
			}

			res.add(tmp);
		}

		return res;
	}

	// ----------------------------- GETQUEUES --------------------------------------

	/**
	 * List all the available queues.
	 * 
	 * @return
	 */
	public static List<com.enioka.jqm.api.Queue> getQueues()
	{
		List<com.enioka.jqm.api.Queue> res = new ArrayList<com.enioka.jqm.api.Queue>();
		ArrayList<Queue> queues = (ArrayList<Queue>) getEm().createQuery("SELECT j FROM Queue j", Queue.class).getResultList();

		for (Queue queue : queues)
		{
			com.enioka.jqm.api.Queue q = getQueue(queue);
			res.add(q);
		}

		return res;
	}

	// ----------------------------- CHANGEQUEUE --------------------------------------

	/**
	 * Move a job instance from a queue to another.
	 * 
	 * @param idJob
	 * @param idQueue
	 */
	public static void changeQueue(int idJob, int idQueue)
	{
		EntityManager em = getEm();
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
			{
				jqmlogger.info("changeQueueError: Job ID or Queue ID doesn't exists.");
			}
		} catch (Exception e)
		{
			throw new JqmException("The queue cannot be changed", e);
		}

		transac.commit();
		em.close();
	}

	// ----------------------------- CHANGEQUEUE --------------------------------------

	/**
	 * Move a job instance from a queue to another queue
	 * 
	 * @param idJob
	 * @param queue
	 */
	public static void changeQueue(int idJob, Queue queue)
	{
		EntityManager em = getEm();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		try
		{
			Query query = em.createQuery("UPDATE JobInstance j SET j.queue = :q WHERE j.id = :jd").setParameter("q", queue)
					.setParameter("jd", idJob);
			int result = query.executeUpdate();

			if (result != 1)
			{
				jqmlogger.info("changeQueueError: Job ID or Queue ID doesn't exists.");
			}
		} catch (Exception e)
		{
			throw new JqmException("The queue cannot be changed", e);
		}

		transac.commit();
		em.close();
	}

	private static JobParameter createJobParameter(String key, String value, EntityManager em)
	{
		JobParameter j = new JobParameter();

		j.setKey(key);
		j.setValue(value);

		em.persist(j);
		return j;
	}

}
