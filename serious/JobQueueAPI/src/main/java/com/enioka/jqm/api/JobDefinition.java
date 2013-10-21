package com.enioka.jqm.api;

import java.util.HashMap;
import java.util.Map;


public class JobDefinition {

	public int parentID;
	public String applicationName;
	public Integer sessionID;
	public String application;
	public String user;
	public String module;
	public String other1;
	public String other2;
	public String other3;
    public Map<String, String> parameters = new HashMap<String, String>();

    public JobDefinition() {

    }

    public JobDefinition(String applicationName, String user) {

    	this.applicationName = applicationName;
    	this.user = user;
    }

	public void addParameter(String key, String value) {

		parameters.put(key, value);
	}

	public void delParameter(String key) {

		parameters.remove(key);
	}


    public int getParentID() {

    	return parentID;
    }


    public void setParentID(int parentID) {

    	this.parentID = parentID;
    }


    public String getApplicationName() {

    	return applicationName;
    }


    public void setApplicationName(String applicationName) {

    	this.applicationName = applicationName;
    }


    public Integer getSessionID() {

    	return sessionID;
    }


    public void setSessionID(Integer sessionID) {

    	this.sessionID = sessionID;
    }


    public String getApplication() {

    	return application;
    }


    public void setApplication(String application) {

    	this.application = application;
    }


    public String getModule() {

    	return module;
    }


    public void setModule(String module) {

    	this.module = module;
    }


    public String getOther1() {

    	return other1;
    }


    public void setOther1(String other1) {

    	this.other1 = other1;
    }


    public String getOther2() {

    	return other2;
    }


    public void setOther2(String other2) {

    	this.other2 = other2;
    }


    public String getOther3() {

    	return other3;
    }


    public void setOther3(String other3) {

    	this.other3 = other3;
    }


    public Map<String, String> getParameters() {

    	return parameters;
    }


    public void setParameters(Map<String, String> parameters) {

    	this.parameters = parameters;
    }


    public String getUser() {

    	return user;
    }


    public void setUser(String user) {

    	this.user = user;
    }


}