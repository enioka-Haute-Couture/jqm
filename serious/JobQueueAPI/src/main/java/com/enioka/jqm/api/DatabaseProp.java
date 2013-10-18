package com.enioka.jqm.api;

public class DatabaseProp {

	private String driver;
    private String url;
    private String user;
    private String pwd;

    public DatabaseProp(String url, String user) {

    	this.url = url;
    	this.user = user;
    }

    public String getDriver() {

    	return driver;
    }

    public void setDriver(String driver) {

    	this.driver = driver;
    }

    public String getUrl() {

    	return url;
    }

    public void setUrl(String url) {

    	this.url = url;
    }

    public String getUser() {

    	return user;
    }

    public void setUser(String user) {

    	this.user = user;
    }

    public String getPwd() {

    	return pwd;
    }

    public void setPwd(String pwd) {

    	this.pwd = pwd;
    }
}
