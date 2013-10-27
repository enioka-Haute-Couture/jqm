/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
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

package com.enioka.jqm.api;

import java.util.HashMap;
import java.util.Map;



public class JobInstance {

	private Integer id;
	private JobDefinition jd;
	public Integer parent;
	private String user;
	private Integer sessionID;
	private String state;
	private Integer position;
	private Queue queue;
	private Map<String, String> parameters = new HashMap<String, String>();

	public Integer getId() {

		return id;
	}

	public void setId(final Integer id) {

		this.id = id;
	}

	public JobDefinition getJd() {

		return jd;
	}

	public void setJd(final JobDefinition jd) {

		this.jd = jd;
	}

	public Integer getParent() {

		return parent;
	}

	public void setParent(final Integer parent) {

		this.parent = parent;
	}

	public String getUser() {

		return user;
	}

	public void setUser(final String user) {

		this.user = user;
	}

	public Integer getSessionID() {

		return sessionID;
	}

	public void setSessionID(final Integer sessionID) {

		this.sessionID = sessionID;
	}

	public String getState() {

		return state;
	}

	public void setState(final String state) {

		this.state = state;
	}

	public Integer getPosition() {

		return position;
	}

	public void setPosition(final Integer position) {

		this.position = position;
	}

	public Queue getQueue() {

		return queue;
	}

	public void setQueue(final Queue queue) {

		this.queue = queue;
	}

	public Map<String, String> getParameters() {

		return parameters;
	}

	public void setParameters(final Map<String, String> parameters) {

		this.parameters = parameters;
	}
}
