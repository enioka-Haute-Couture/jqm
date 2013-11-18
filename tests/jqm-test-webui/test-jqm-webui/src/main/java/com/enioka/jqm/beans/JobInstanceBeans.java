package com.enioka.jqm.beans;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@ManagedBean
@ApplicationScoped
public class JobInstanceBeans implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8413276493119721697L;
	private List<com.enioka.jqm.jpamodel.JobInstance> jobs;

	public JobInstanceBeans() {

		getJobs();
	}

	@PostConstruct
	public void initJobInstanceBeans()
	{
		//		this.jobs = new ArrayList<JobInstance>();
		//
		//		jobs.add(new JobInstance(1, "jd1", "parent1", "username1", 1, "SUBMITTED", 1, "VIP", "node1"));
		//		jobs.add(new JobInstance(2, "jd2", "parent2", "username2", 1, "SUBMITTED", 2, "Normal", "node1"));
		//		jobs.add(new JobInstance(3, "jd3", "parent3", "username3", 1, "SUBMITTED", 3, "Slow", "node1"));
		//		jobs.add(new JobInstance(4, "jd4", "parent4", "username4", 1, "SUBMITTED", 4, "VIP", "node1"));

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
		EntityManager em = emf.createEntityManager();

		jobs = em.createQuery("SELECT j FROM JobInstance j",
				com.enioka.jqm.jpamodel.JobInstance.class).getResultList();

		em.close();
		emf.close();
	}

	public List<com.enioka.jqm.jpamodel.JobInstance> getJobs() {

		return jobs;
	}

}
