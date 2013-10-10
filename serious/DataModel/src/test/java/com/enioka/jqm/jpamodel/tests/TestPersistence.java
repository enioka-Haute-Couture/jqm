package com.enioka.jqm.jpamodel.tests;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.junit.Test;

import com.enioka.jqm.jpamodel.Node;

public class TestPersistence {

	@Test
	public void testSave() {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("jobqueue-api-pu");

		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		Node n = new Node();
		n.setListeningInterface("localhost");
		n.setPort(1234);

		em.persist(n);

		em.getTransaction().commit();

		EntityManager em2 = emf.createEntityManager();
		long i = (Long) em2.createQuery("SELECT COUNT(t) from Node t")
				.getSingleResult();

		Assert.assertEquals(1, i);
	}
}
