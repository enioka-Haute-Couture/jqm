package com.enioka.jqm.tools;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.Message;

public final class Helpers
{
	// The one and only EMF in the engine.
	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");

	/**
	 * Get a fresh EM on the jobqueue-api-pu persistence Unit
	 * @return an EntityManager
	 */
	public static EntityManager getNewEm()
	{
		return emf.createEntityManager();
	}

	/**
	 * For internal test use only
	 */
	static void resetEmf()
	{
		if (emf != null)
		{
			emf.close();
		}
		emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	}

	/**
	 * Create a text message that will be stored in the database.
	 * Must be called inside a JPA transaction.
	 * @param textMessage
	 * @param history
	 * @param em
	 * @return the JPA message created
	 */
	public static Message createMessage(String textMessage, History history, EntityManager em)
	{
		Message m = new Message();

		m.setTextMessage(textMessage);
		m.setHistory(history);

		em.persist(m);
		return m;
	}


	/**
	 * Create a Deliverable inside the datbase that will track a file created by a JobInstance
	 * Must be called from inside a JPA transaction
	 * @param fp FilePath (relative to a root directory - cf. Node)
	 * @param fn FileName
	 * @param hp HashPath
	 * @param ff File family (may be null). E.g.: "daily report"
	 * @param jobId Job Instance ID
	 * @param em the EM to use.
	 * @return
	 */
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
