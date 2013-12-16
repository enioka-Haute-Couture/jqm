package com.enioka.jqm.webui;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.enioka.jqm.jpamodel.JobInstance;

@ManagedBean(eager = true)
@RequestScoped
public class JobInstanceCustom extends com.enioka.jqm.jpamodel.JobInstance implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2430208189418753346L;
	private boolean stop;
	private com.enioka.jqm.jpamodel.JobInstance ji;


	public JobInstanceCustom()
	{

	}

	public JobInstanceCustom(JobInstance ji, boolean stop)
	{
		this.stop = stop;
		this.ji = ji;
	}


	public boolean isStop() {

		return stop;
	}


	public void setStop(boolean stop) {

		this.stop = stop;
	}


	public com.enioka.jqm.jpamodel.JobInstance getJi() {

		return ji;
	}


	public void setJi(com.enioka.jqm.jpamodel.JobInstance ji) {

		this.ji = ji;
	}

}
