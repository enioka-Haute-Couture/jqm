package com.enioka.jqm.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@ManagedBean(eager = true)
@RequestScoped
public class HistoryBeans implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8655619320083974510L;

	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	EntityManager em = emf.createEntityManager();

	private ArrayList<com.enioka.jqm.jpamodel.History> hs = null;

	public HistoryBeans() {

		getHs();
	}

	public ArrayList<com.enioka.jqm.jpamodel.History> getHs() {

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
}

