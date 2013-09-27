package com.enioka.jqm.temp;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.enioka.jqm.jpamodel.JobInstance;

public class Polling
{
	JobInstance job = null;
	public Polling()
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jqmapi");
		EntityManager em = emf.createEntityManager();

		JobInstance q = em.createQuery("SELECT DISTINCT (j) FROM JobInstance j WHERE EXISTS (" +
				"SELECT MAX (j.position) FROM JobInstance j WHERE j.state = :state)",
				JobInstance.class).setParameter("state", "SUBMITTED").getSingleResult();
		job = q;
	}

	public JobInstance getJob()
	{
		return job;
	}

}
