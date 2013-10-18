package com.enioka.jqm.api;



public class Deliverable {

	private String filePath;
    private String fileName;

    public Deliverable(String filePath, String fileName) {

    	this.filePath = filePath;
    	this.fileName = fileName;
    }

    public String getFilePath() {

    	return filePath;
    }

    public void setFilePath(String filePath) {

    	this.filePath = filePath;
    }

    public String getFileName() {

    	return fileName;
    }

    public void setFileName(String fileName) {

    	this.fileName = fileName;
    }
}
