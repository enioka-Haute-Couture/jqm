package com.enioka.jqm.webui;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.enioka.jqm.jpamodel.JobDef;

@ManagedBean(eager = true)
@SessionScoped
public class JobDefCustom extends JobDef implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1076548059466510604L;
	private com.enioka.jqm.jpamodel.JobDef j = null;
	private boolean enqueue = false;

	public JobDefCustom() {}

	public JobDefCustom(com.enioka.jqm.jpamodel.JobDef j, boolean isEnqueue)
	{
		this.j = j;
		this.enqueue = isEnqueue;
	}


	public JobDef getJ() {

		return j;
	}


	public void setJ(JobDef j) {

		this.j = j;
	}


	public boolean isEnqueue() {

		return enqueue;
	}


	public void setEnqueue(boolean isEnqueue) {

		this.enqueue = isEnqueue;
	}
}
