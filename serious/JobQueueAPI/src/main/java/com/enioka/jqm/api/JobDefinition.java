package com.enioka.jqm.api;

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.jpamodel.Queue;


public class JobDefinition {

	public boolean canBeRestarted = true;
	public String javaClassName;
	public String filePath;
	public Queue queue;
	public Integer maxTimeRunning;
	public String applicationName;
	public Integer sessionID;
	public String application;
	public String module;
	public String other1;
	public String other2;
	public String other3;
	public boolean highlander = false;
    public String jarPath;
    public Map<String, String> parameters = new HashMap<String, String>();

    public JobDefinition(String applicationName) {

    	this.applicationName = applicationName;
    }

    public JobDefinition(boolean canBeRestarted, String javaClassName, Map<String, String> jps, String filePath, String jp,
			 						Queue queue, Integer maxTimeRunning, String applicationName, Integer sessionID,
			 						String application, String module, String other1, String other2, String other3,
			 						boolean highlander) {

    	this.canBeRestarted = canBeRestarted;
    	this.javaClassName = javaClassName;
    	this.parameters = jps;
    	this.filePath = filePath;
    	this.jarPath = jp;
    	this.queue = queue;
    	this.maxTimeRunning = maxTimeRunning;
    	this.applicationName = applicationName;
    	this.sessionID = sessionID;
    	this.application = application;
    	this.module = module;
    	this.other1 = other1;
    	this.other2 = other2;
    	this.other3 = other3;
    	this.highlander = highlander;

    }

	public void addParameter(String key, String value) {

		parameters.put(key, value);
	}

	public void delParameter(String key) {

		parameters.remove(key);
	}


    public boolean isCanBeRestarted() {

    	return canBeRestarted;
    }


    public void setCanBeRestarted(boolean canBeRestarted) {

    	this.canBeRestarted = canBeRestarted;
    }


    public String getJavaClassName() {

    	return javaClassName;
    }


    public void setJavaClassName(String javaClassName) {

    	this.javaClassName = javaClassName;
    }


    public String getFilePath() {

    	return filePath;
    }


    public void setFilePath(String filePath) {

    	this.filePath = filePath;
    }


    public Queue getQueue() {

    	return queue;
    }


    public void setQueue(Queue queue) {

    	this.queue = queue;
    }


    public Integer getMaxTimeRunning() {

    	return maxTimeRunning;
    }


    public void setMaxTimeRunning(Integer maxTimeRunning) {

    	this.maxTimeRunning = maxTimeRunning;
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


    public boolean isHighlander() {

    	return highlander;
    }


    public void setHighlander(boolean highlander) {

    	this.highlander = highlander;
    }


    public String getJarPath() {

    	return jarPath;
    }


    public void setJarPath(String jarPath) {

    	this.jarPath = jarPath;
    }


    public Map<String, String> getParameters() {

    	return parameters;
    }


    public void setParameters(Map<String, String> parameters) {

    	this.parameters = parameters;
    }
}
