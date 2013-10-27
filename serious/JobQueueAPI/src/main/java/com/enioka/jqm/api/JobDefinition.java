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


public class JobDefinition {

	public int parentID;
	public String applicationName;
	public Integer sessionID;
	public String application;
	public String user;
	public String module;
	public String other1;
	public String other2;
	public String other3;
	public Map<String, String> parameters = new HashMap<String, String>();

	public JobDefinition() {

	}

	public JobDefinition(final String applicationName, final String user) {

		this.applicationName = applicationName;
		this.user = user;
	}

	public void addParameter(final String key, final String value) {

		parameters.put(key, value);
	}

	public void delParameter(final String key) {

		parameters.remove(key);
	}


	public int getParentID() {

		return parentID;
	}


	public void setParentID(final int parentID) {

		this.parentID = parentID;
	}


	public String getApplicationName() {

		return applicationName;
	}


	public void setApplicationName(final String applicationName) {

		this.applicationName = applicationName;
	}


	public Integer getSessionID() {

		return sessionID;
	}


	public void setSessionID(final Integer sessionID) {

		this.sessionID = sessionID;
	}


	public String getApplication() {

		return application;
	}


	public void setApplication(final String application) {

		this.application = application;
	}


	public String getModule() {

		return module;
	}


	public void setModule(final String module) {

		this.module = module;
	}


	public String getOther1() {

		return other1;
	}


	public void setOther1(final String other1) {

		this.other1 = other1;
	}


	public String getOther2() {

		return other2;
	}


	public void setOther2(final String other2) {

		this.other2 = other2;
	}


	public String getOther3() {

		return other3;
	}


	public void setOther3(final String other3) {

		this.other3 = other3;
	}


	public Map<String, String> getParameters() {

		return parameters;
	}


	public void setParameters(final Map<String, String> parameters) {

		this.parameters = parameters;
	}


	public String getUser() {

		return user;
	}


	public void setUser(final String user) {

		this.user = user;
	}


}