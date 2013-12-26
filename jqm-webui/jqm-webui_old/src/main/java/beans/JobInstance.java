package beans;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.enioka.jqm.api.Dispatcher;

@ManagedBean(eager = true)
@RequestScoped
public class JobInstance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7869897762565932002L;
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	EntityManager em = emf.createEntityManager();

	private ArrayList<utils.JobInstanceCustom> jobs;

	public JobInstance() {

		getJobs();
	}

	public String stop()
	{
		for (utils.JobInstanceCustom j : jobs) {
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
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("queue.xhtml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "null";

	}

	public ArrayList<utils.JobInstanceCustom> getJobs() {

		List q = em.createQuery("SELECT j FROM JobInstance j").getResultList();
		List<com.enioka.jqm.jpamodel.JobInstance> tmp = q;

		jobs = new ArrayList<utils.JobInstanceCustom>();
		for(com.enioka.jqm.jpamodel.JobInstance j : tmp)
		{
			jobs.add(new utils.JobInstanceCustom(j, false));
		}

		return jobs;
	}

	public void setJobs(ArrayList<utils.JobInstanceCustom> jobs) {
		this.jobs = jobs;
	}
}