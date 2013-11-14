package com.enioka.jqm.tools;

import javax.persistence.EntityManager;

import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;

public class Link
{
	private ClassLoader old = null;
	private EntityManager em = null;
	private Integer id = null;

	public Link(ClassLoader old, Integer id, EntityManager em)
	{
		this.old = old;
		this.em = em;
		this.id = id;
	};

	public void sendMsg(String msg)
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(old);

		JobInstance j = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :i", JobInstance.class).setParameter("i", id)
				.getSingleResult();

		History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance.id = :i", History.class).setParameter("i", id)
				.getSingleResult();

		em.getTransaction().begin();
		Message mssg = new Message();

		mssg.setHistory(h);
		mssg.setTextMessage(msg);
		em.persist(mssg);
		em.getTransaction().commit();

		Thread.currentThread().setContextClassLoader(cl);
	}

	public void sendProgress(Integer msg)
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(old);

		JobInstance j = em.createQuery("SELECT j FROM JobInstance j WHERE j.id = :i", JobInstance.class).setParameter("i", id)
				.getSingleResult();

		em.getTransaction().begin();
		j.setProgress(msg);
		em.getTransaction().commit();

		Thread.currentThread().setContextClassLoader(cl);
	}
}
