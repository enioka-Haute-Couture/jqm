package com.enioka.jqm.jpamodel;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;

public class TestPersistence
{
	@Test
	public void testSave()
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");

		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		Node n = new Node();
		n.setListeningInterface("localhost");
		n.setPort(1234);
		n.setDlRepo("/Temp/");

		em.persist(n);

		em.getTransaction().commit();

		EntityManager em2 = emf.createEntityManager();
		long i = (Long) em2.createQuery("SELECT COUNT(t) from Node t").getSingleResult();

		Assert.assertTrue(i >= 1);
	}

	@Test
	public void testSave2()
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");

		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		Queue q = new Queue();
		q.setDefaultQueue(true);
		q.setDescription("ppp");
		q.setMaxTempInQueue(12);
		q.setMaxTempRunning(13);
		q.setName("super queue");
		em.persist(q);

		JobDef jd = new JobDef();

		jd.setApplication("MARSU");
		jd.setApplicationName("MARSU");
		jd.setCanBeRestarted(false);
		jd.setFilePath("/eeee");
		jd.setHighlander(false);
		jd.setJarPath("/ml");
		jd.setJavaClassName("/mlmlmlmlmlml");
		jd.setMaxTimeRunning(10);
		jd.setModule("MMM");
		jd.setQueue(q);

		em.persist(jd);

		JobDefParameter p1 = new JobDefParameter();
		jd.getParameters().add(p1);
		p1.setKey("mm");
		p1.setValue("pppp");
		em.persist(p1);

		JobDefParameter p2 = new JobDefParameter();
		jd.getParameters().add(p2);
		p2.setKey("mm");
		p2.setValue("pppp");
		em.persist(p2);

		em.getTransaction().commit();

		EntityManager em2 = emf.createEntityManager();
		long i = (Long) em2.createQuery("SELECT COUNT(t) from JobDef t").getSingleResult();
		Assert.assertTrue(i >= 1);

		jd = em2.find(JobDef.class, jd.getId());
		Assert.assertTrue(2 <= jd.getParameters().size());
	}
}
