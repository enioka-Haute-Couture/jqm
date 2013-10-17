package com.enioka.jqm.jndi;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;


public class DbDataSource implements DataSource, Serializable {

	/**
     *
     */
    private static final long serialVersionUID = -7943086055863888338L;
	private String connectionString, userName, password;

	public DbDataSource(String connStr, String username, String password) {
		this.connectionString = connStr;
		this.userName = username;
		this.password = password;
	}

	@Override
    public Connection getConnection() throws SQLException {
		return DriverManager
				.getConnection(connectionString, userName, password);
	}

	@Override
    public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
    public void setLoginTimeout(int seconds) throws SQLException {
	}

	@Override
    public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
    public Connection getConnection(String username, String password)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {

		return null;
	}
}
