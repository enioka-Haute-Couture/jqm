/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="JobParameter")
public class JobParameter implements Serializable
{
	private static final long serialVersionUID = -8894511645365690426L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(nullable=false, length=50, name="KEYNAME")
	private String key;
	@Column(nullable=false, length=1000, name="VALUE")
	private String value;

	@ManyToOne
	@JoinColumn(name="jobinstance_id")
	private JobInstance jobinstance;

	/**
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(final String key)
	{
		this.key = key;
	}
	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(final String value)
	{
		this.value = value;
	}
	public Integer getId()
	{
		return id;
	}
	public void setId(final Integer id)
	{
		this.id = id;
	}
	public JobInstance getJobinstance()
	{
		return jobinstance;
	}
	public void setJobinstance(JobInstance jobinstance)
	{
		this.jobinstance = jobinstance;
	}

}
