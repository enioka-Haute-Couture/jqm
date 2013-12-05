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

package com.enioka.jqm.jndi;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * A JDBC Datasource
 * 
 */
public class DbDataSource implements DataSource, Serializable
{
	private static final long serialVersionUID = -7943086055863888338L;
	private String connectionString, userName, password;

	/**
	 * Constructor
	 * 
	 * @param connStr
	 * @param username
	 * @param password
	 */
	public DbDataSource(String connStr, String username, String password)
	{
		this.connectionString = connStr;
		this.userName = username;
		this.password = password;
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection(connectionString, userName, password);
	}

	@Override
	public int getLoginTimeout() throws SQLException
	{
		return 0;
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException
	{
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException
	{
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException
	{
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return false;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException
	{
		return null;
	}

	/**
	 * Not supported.
	 * @return an exception
	 * @throws SQLFeatureNotSupportedException
	 */
	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{
		return null;
	}
}
