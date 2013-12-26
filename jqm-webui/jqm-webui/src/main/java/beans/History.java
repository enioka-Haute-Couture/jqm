package beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@ManagedBean(eager = false)
@RequestScoped
public class History implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7136876637706661208L;

	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	EntityManager em = emf.createEntityManager();

	private List<com.enioka.jqm.jpamodel.History> hs = null;
	private List<com.enioka.jqm.jpamodel.History> hss = null;
	private List<com.enioka.jqm.jpamodel.History> filteredhs;

	@PostConstruct
	public void init()
	{
		FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		em.clear();
		getHs();
	}

	public String reload()
	{
		return "null";
	}

	public List<com.enioka.jqm.jpamodel.History> getHs() {

		em.clear();

		@SuppressWarnings("unchecked")
		List<com.enioka.jqm.jpamodel.History> tmp = em.createQuery("SELECT h FROM History h").getResultList();

		hs = new ArrayList<com.enioka.jqm.jpamodel.History>();

		for(com.enioka.jqm.jpamodel.History j : tmp)
		{
			hs.add(j);
		}

		return hs;
	}

	public void setHs(ArrayList<com.enioka.jqm.jpamodel.History> hs) {
		this.hs = hs;
	}


	public List<com.enioka.jqm.jpamodel.History> getHss() {
		hss = hs;
		return hss;
	}


	public void setHss(List<com.enioka.jqm.jpamodel.History> hss) {

		this.hss = hss;
	}


	public List<com.enioka.jqm.jpamodel.History> getFilteredhs() {
		return filteredhs;
	}


	public void setFilteredhs(List<com.enioka.jqm.jpamodel.History> filteredhs) {

		this.filteredhs = filteredhs;
	}
}
