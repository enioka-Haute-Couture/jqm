package com.enioka.jqm.tools;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.Message;

public class Helpers
{
	// The one and only EMF in the engine.
	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");

	public static EntityManager getNewEm()
	{
		return emf.createEntityManager();
	}

	public static void resetEmf()
	{
		if (emf != null)
			emf.close();
		emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	}

	public static Message createMessage(String textMessage, History history, EntityManager em)
	{
		Message m = new Message();

		m.setTextMessage(textMessage);
		m.setHistory(history);

		em.persist(m);
		return m;
	}

	// ------------------ DELIVERABLES ------------------------

	public static Deliverable createDeliverable(String fp, String fn, String hp, String ff, Integer jobId, EntityManager em)
	{
		Deliverable j = new Deliverable();

		j.setFilePath(fp);
		j.setHashPath(hp);
		j.setFileFamily(ff);
		j.setJobId(jobId);
		j.setFileName(fn);

		em.persist(j);
		return j;
	}

}
