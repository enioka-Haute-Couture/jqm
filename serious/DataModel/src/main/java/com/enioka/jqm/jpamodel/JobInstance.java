package com.enioka.jqm.jpamodel;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;


/**
 *
 * @author pierre.coppee
 */
@Entity
public class JobInstance implements Comparable<JobInstance>, Serializable{

	/**
     *
     */
    private static final long serialVersionUID = -7710486847228806301L;
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	@ManyToOne(fetch=FetchType.LAZY)
	private JobDef jd;
	@ManyToOne(fetch=FetchType.LAZY)
	public JobInstance parent;
	@Column(length=50)
    private String user;
    private Integer sessionID;
    @Column(nullable=false, length=50)
    private String state;
    private Integer position;
    @ManyToOne(targetEntity=com.enioka.jqm.jpamodel.Queue.class)
    private Queue queue;
    @OneToMany(orphanRemoval=true)
	@JoinColumn(name="JOB_PARAMETERS")
    private List<JobParameter> parameters;

    public int getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public int getSessionID() {
        return sessionID;
    }
    public JobDef getJd()
	{
		return jd;
	}

	public void setJd(JobDef jd)
	{
		this.jd = jd;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public Integer getPosition()
	{
		return position;
	}

	public void setPosition(Integer position)
	{
		this.position = position;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public void setSessionID(Integer sessionID)
	{
		this.sessionID = sessionID;
	}

	public JobInstance getParent()
	{
		return parent;
	}

	public void setParent(JobInstance parent)
	{
		this.parent = parent;
	}

	@Override
    public int compareTo(JobInstance arg0) {

		int nb1 = arg0.getPosition();
	      int nb2 = this.getPosition();
	      if (nb1 > nb2)  return -1;
	      else if(nb1 == nb2) return 0;
	      else return 1;
    }


    public Queue getQueue() {

    	return queue;
    }


    public void setQueue(Queue queue) {

    	this.queue = queue;
    }


    public List<JobParameter> getParameters() {

    	return parameters;
    }


    public void setParameters(List<JobParameter> parameters) {

    	this.parameters = parameters;
    }

}
