package com.enioka.jqm.tools;

import javax.persistence.EntityManager;

import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.Message;

public class Link
{
	private ClassLoader old = null;
	private EntityManager em = null;

	public Link(ClassLoader old, EntityManager em)
	{
		this.old = old;
		this.em = em;
	};

	public void sendMsg(String msg, Integer id)
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(old);

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

	public void sendProgress(String msg)
	{

	}
}
