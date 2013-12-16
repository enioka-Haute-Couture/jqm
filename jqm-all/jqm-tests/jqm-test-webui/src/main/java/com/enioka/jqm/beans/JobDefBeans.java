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
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.webui.JobDefCustom;

@ManagedBean(eager = true)
@RequestScoped
public class JobDefBeans implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2144227424128605638L;

	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	EntityManager em = emf.createEntityManager();

	private ArrayList<JobDefCustom> jobs = new ArrayList<JobDefCustom>();
	private List<com.enioka.jqm.jpamodel.JobDef> tmp;
	private JobDef j;
	private String userName;

	public JobDefBeans() {

		getJobs();
	}

	public String enqueue()
	{
		for (JobDefCustom j : jobs) {
			if (j.isEnqueue())
			{
				JobDefinition jd = new JobDefinition(j.getJ().getApplicationName(), userName);
				Dispatcher.enQueue(jd);
			}
		}

		return "enqueue";
	}

	public JobDef getJ() {

		return j;
	}


	public void setJ(JobDef j) {

		this.j = j;
	}


	public List<JobDefCustom> getJobs() {

		jobs = new ArrayList<JobDefCustom>();
		em.clear();

		this.tmp = em.createQuery("SELECT j FROM JobDef j",
				com.enioka.jqm.jpamodel.JobDef.class).getResultList();

		for (int i = 0; i < tmp.size(); i++)
		{
			jobs.add(new JobDefCustom(tmp.get(i), false));
		}
		tmp.clear();
		return jobs;
	}


	public List<com.enioka.jqm.jpamodel.JobDef> getTmp() {

		return tmp;
	}


	public void setTmp(List<com.enioka.jqm.jpamodel.JobDef> tmp) {

		this.tmp = tmp;
	}

	public void setJobs(ArrayList<JobDefCustom> jobs) {

		this.jobs = jobs;

	}


	public String getUserName() {

		return userName;
	}


	public void setUserName(String userName) {

		this.userName = userName;
	}

}
