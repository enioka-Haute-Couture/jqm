package com.enioka.jqm.webui;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.enioka.jqm.jpamodel.JobDef;

@ManagedBean(name="jobDefCustom")
@SessionScoped
public class JobDefCustom extends JobDef implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1076548059466510604L;
	private com.enioka.jqm.jpamodel.JobDef j = null;
	private boolean enqueue = false;
	private int cl;

	public JobDefCustom() {}

	public JobDefCustom(com.enioka.jqm.jpamodel.JobDef j, boolean isEnqueue)
	{
		System.out.println("ClassLoader Custom: " + JobDef.class.getClassLoader().hashCode());
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


	public int getCl() {
		this.cl = JobDef.class.getClassLoader().hashCode();
		return cl;
	}


	public void setCl(int cl) {

		this.cl = cl;
	}

}
