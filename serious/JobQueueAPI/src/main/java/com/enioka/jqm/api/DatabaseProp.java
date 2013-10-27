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

public class DatabaseProp {

	private String driver;
	private String url;
	private String user;
	private String pwd;

	public DatabaseProp(final String url, final String user) {

		this.url = url;
		this.user = user;
	}

	public String getDriver() {

		return driver;
	}

	public void setDriver(final String driver) {

		this.driver = driver;
	}

	public String getUrl() {

		return url;
	}

	public void setUrl(final String url) {

		this.url = url;
	}

	public String getUser() {

		return user;
	}

	public void setUser(final String user) {

		this.user = user;
	}

	public String getPwd() {

		return pwd;
	}

	public void setPwd(final String pwd) {

		this.pwd = pwd;
	}
}
