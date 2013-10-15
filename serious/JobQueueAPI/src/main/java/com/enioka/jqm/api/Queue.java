package com.enioka.jqm.api;



public class Queue {

	private int id;
    private String name;
    private String description;
    private Integer maxTempInQueue;
    private Integer maxTempRunning;
    private boolean defaultQueue;

    public int getId() {

    	return id;
    }

    public void setId(int id) {

    	this.id = id;
    }

    public String getName() {

    	return name;
    }

    public void setName(String name) {

    	this.name = name;
    }

    public String getDescription() {

    	return description;
    }

    public void setDescription(String description) {

    	this.description = description;
    }

    public Integer getMaxTempInQueue() {

    	return maxTempInQueue;
    }

    public void setMaxTempInQueue(Integer maxTempInQueue) {

    	this.maxTempInQueue = maxTempInQueue;
    }

    public Integer getMaxTempRunning() {

    	return maxTempRunning;
    }

    public void setMaxTempRunning(Integer maxTempRunning) {

    	this.maxTempRunning = maxTempRunning;
    }

    public boolean isDefaultQueue() {

    	return defaultQueue;
    }

    public void setDefaultQueue(boolean defaultQueue) {

    	this.defaultQueue = defaultQueue;
    }
}
