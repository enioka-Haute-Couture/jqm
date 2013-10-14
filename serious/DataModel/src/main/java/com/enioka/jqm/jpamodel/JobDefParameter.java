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
public class JobDefParameter implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = -5308516206913425230L;
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=false, length=50)
	private String key;
	@Column(nullable=false, length=1000)
	private String value;

	public JobParameter jDPtoJP(JobDefParameter jdp) {

		JobParameter jp = new JobParameter();

		jp.setKey(jdp.getKey());
		jp.setValue(jdp.getValue());

		return jp;
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
