package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class DatabaseProp implements Serializable{

	/**
     *
     */
    private static final long serialVersionUID = 5286608402747360301L;
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
	protected Integer id;
    @Column(nullable=false)
    private String driver;
    @Column(nullable=false)
    private String url;
    @Column(nullable=false)
    private String user;
    @Column(nullable=false)
    private String pwd;

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


    public Integer getId() {

    	return id;
    }
}
