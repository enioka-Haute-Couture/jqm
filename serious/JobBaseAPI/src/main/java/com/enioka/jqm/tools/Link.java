package com.enioka.jqm.tools;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;

public class Link
{
	private Logger jqmlogger = Logger.getLogger(Polling.class);
	private ClassLoader old = null;
	private EntityManager em = null;
	private Integer id = null;
	private volatile boolean running = true;

	public Link(ClassLoader old, Integer id, EntityManager em)
	{
		this.old = old;
		this.em = em;
		this.id = id;
	};

	public void sendMsg(String msg) throws JqmKillException
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(old);

		JobInstance j = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :i", JobInstance.class).setParameter("i", id)
				.getSingleResult();

		if (j.getState().equals("KILLED"))
		{
			Thread.currentThread().setContextClassLoader(cl);
			throw new JqmKillException("This job" + "(ID: " + id + ")" + " has been killed by a user");
		}
		else
		{
			History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :i", History.class).setParameter("i", id)
					.getSingleResult();

			em.getTransaction().begin();
			Message mssg = new Message();

			mssg.setHistory(h);
			mssg.setTextMessage(msg);
			em.persist(mssg);
			em.getTransaction().commit();
		}

		Thread.currentThread().setContextClassLoader(cl);
	}

	public void sendProgress(Integer msg) throws InterruptedException
	{
		em.clear();
		JobInstance j = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :i", JobInstance.class).setParameter("i", id)
				.getSingleResult();
		jqmlogger.debug("Job status before Kill: " + j.getState());

		if (j.getState().equals("KILLED"))
		{
			jqmlogger.debug("Link: Job will be KILLED");
			Thread.currentThread().interrupt();
			throw new JqmKillException("This job" + "(ID: " + j.getId() + ")" + " has been killed by a user");
		}
		else
		{

			j = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :i", JobInstance.class).setParameter("i", id).getSingleResult();
			em.getTransaction().begin();
			j.setProgress(msg);
			j = em.merge(j);
			em.getTransaction().commit();

			jqmlogger.debug("Actual progression: " + j.getProgress());
		}

		jqmlogger.debug("Actual progression: " + j.getProgress());
	}

	public boolean isRunning()
	{
		return running;
	}

	public void setRunning(boolean running)
	{
		this.running = running;
	}
}
