package com.enioka.jqm.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.webui.JobInstanceCustom;

@ManagedBean(eager = true)
@RequestScoped
public class JobInstanceBeans extends JobInstance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8413276493119721697L;

	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	EntityManager em = emf.createEntityManager();

	private ArrayList<JobInstanceCustom> jobs;
	private List<com.enioka.jqm.jpamodel.JobInstance> tmp;

	public JobInstanceBeans() {

		getJobs();
	}

	public String stop()
	{
		for (JobInstanceCustom j : jobs) {
			if (j.isStop())
			{
				if (j.getJi().getState().equals("SUBMITTED"))
				{
					Dispatcher.cancelJobInQueue(j.getJi().getId());
				}
				else if (j.getJi().getState().equals("ATTRIBUTED") || j.getJi().getState().equals("RUNNING"))
				{
					Dispatcher.killJob(j.getJi().getId());
				}
			}
		}

		return "stop";
	}

	public ArrayList<JobInstanceCustom> getJobs() {

		em.clear();
		tmp = em.createQuery("SELECT j FROM JobInstance j",
				com.enioka.jqm.jpamodel.JobInstance.class).getResultList();

		jobs = new ArrayList<JobInstanceCustom>();

		for (com.enioka.jqm.jpamodel.JobInstance j : tmp) {
			jobs.add(new JobInstanceCustom(j, false));
		}

		tmp.clear();
		return jobs;
	}


	public List<com.enioka.jqm.jpamodel.JobInstance> getTmp() {

		return tmp;
	}


	public void setTmp(List<com.enioka.jqm.jpamodel.JobInstance> tmp) {

		this.tmp = tmp;
	}

	public void setJobs(ArrayList<JobInstanceCustom> jobs) {

		this.jobs = jobs;
	}

}
