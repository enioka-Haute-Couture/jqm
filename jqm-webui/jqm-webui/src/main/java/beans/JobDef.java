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
import com.enioka.jqm.api.JobDefinition;

@ManagedBean(eager = true)
@RequestScoped
public class JobDef implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -608970776489109835L;
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	EntityManager em = emf.createEntityManager();

	private ArrayList<utils.JobDefCustom> jobs = new ArrayList<utils.JobDefCustom>();
	private String userName;

	public JobDef()
	{
		getJobs();
	}

	public ArrayList<utils.JobDefCustom> getJobs() {

		List q = em.createQuery("SELECT j FROM JobDef j").getResultList();
		List<com.enioka.jqm.jpamodel.JobDef> tmp = q;

		jobs = new ArrayList<utils.JobDefCustom>();
		for(com.enioka.jqm.jpamodel.JobDef j : tmp)
		{
			jobs.add(new utils.JobDefCustom(j, false));
		}

		return jobs;
	}

	public void setJobs(ArrayList<utils.JobDefCustom> jobs) {
		this.jobs = jobs;
	}

	public String enqueue() {
		for (utils.JobDefCustom j : jobs) {
			if (j.isEnqueue()) {
				JobDefinition jd = new JobDefinition(j.getJ().getApplicationName(), userName);
				Dispatcher.enQueue(jd);
			}
		}
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("jobdef.xhtml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "null";
	}


	public String getUserName() {

		return userName;
	}


	public void setUserName(String userName) {

		this.userName = userName;
	}
}
