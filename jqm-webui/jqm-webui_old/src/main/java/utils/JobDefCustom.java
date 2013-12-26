package utils;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean(eager = true)
@RequestScoped
public class JobDefCustom extends com.enioka.jqm.jpamodel.JobDef implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1108495302978565146L;
	private boolean enqueue = false;
	private com.enioka.jqm.jpamodel.JobDef j = null;

	public JobDefCustom() {

	}

	public boolean isEnqueue() {
		return enqueue;
	}

	public void setEnqueue(boolean enqueue) {
		this.enqueue = enqueue;
	}

	public com.enioka.jqm.jpamodel.JobDef getJ() {
		return j;
	}

	public void setJ(com.enioka.jqm.jpamodel.JobDef j) {
		this.j = j;
	}

	public JobDefCustom(com.enioka.jqm.jpamodel.JobDef j, boolean enqueue)
	{
		this.j = j;
		this.enqueue = enqueue;
	}


}
