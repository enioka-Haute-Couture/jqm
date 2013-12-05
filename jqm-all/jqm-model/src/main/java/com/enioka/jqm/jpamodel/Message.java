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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="Message")
public class Message implements Serializable{

	private static final long serialVersionUID = 1234354709423602792L;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(length=1000, name="textMessage")
	private String textMessage;
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="message_history", nullable=false)
	private History history;


	/**
	 * @return the id
	 */
	public Integer getId()
	{
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(final Integer id)
	{
		this.id = id;
	}
	/**
	 * @return the textMessage
	 */
	public String getTextMessage()
	{
		return textMessage;
	}
	/**
	 * @param textMessage the textMessage to set
	 */
	public void setTextMessage(final String textMessage)
	{
		this.textMessage = textMessage;
	}
	public History getHistory()
	{
		return history;
	}
	public void setHistory(final History history)
	{
		this.history = history;
	}
}
