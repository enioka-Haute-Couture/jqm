package com.enioka.jqm.api;

import java.util.HashMap;
import java.util.Map;



public class JobInstance {

	private Integer id;
	private JobDefinition jd;
	public Integer parent;
    private String user;
    private Integer sessionID;
    private String state;
    private Integer position;
    private Queue queue;
    private Map<String, String> parameters = new HashMap<String, String>();

    public Integer getId() {

    	return id;
    }

    public void setId(Integer id) {

    	this.id = id;
    }

    public JobDefinition getJd() {

    	return jd;
    }

    public void setJd(JobDefinition jd) {

    	this.jd = jd;
    }

    public Integer getParent() {

    	return parent;
    }

    public void setParent(Integer parent) {

    	this.parent = parent;
    }

    public String getUser() {

    	return user;
    }

    public void setUser(String user) {

    	this.user = user;
    }

    public Integer getSessionID() {

    	return sessionID;
    }

    public void setSessionID(Integer sessionID) {

    	this.sessionID = sessionID;
    }

    public String getState() {

    	return state;
    }

    public void setState(String state) {

    	this.state = state;
    }

    public Integer getPosition() {

    	return position;
    }

    public void setPosition(Integer position) {

    	this.position = position;
    }

    public Queue getQueue() {

    	return queue;
    }

    public void setQueue(Queue queue) {

    	this.queue = queue;
    }

    public Map<String, String> getParameters() {

    	return parameters;
    }

    public void setParameters(Map<String, String> parameters) {

    	this.parameters = parameters;
    }
}
