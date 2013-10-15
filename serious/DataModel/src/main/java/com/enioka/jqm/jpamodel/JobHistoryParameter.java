package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Embeddable
public class JobHistoryParameter implements Serializable{

	/**
     *
     */
    private static final long serialVersionUID = -667768580903076029L;

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=false, length=50)
	private String key;
	@Column(nullable=false, length=1000)
	private String value;

    public Integer getId() {

    	return id;
    }

    public void setId(Integer id) {

    	this.id = id;
    }

    public String getKey() {

    	return key;
    }

    public void setKey(String key) {

    	this.key = key;
    }

    public String getValue() {

    	return value;
    }

    public void setValue(String value) {

    	this.value = value;
    }
}
