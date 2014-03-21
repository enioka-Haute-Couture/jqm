package com.enioka.jqm.providers;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;

public class Interceptor extends JdbcInterceptor
{

    @Override
    public void reset(ConnectionPool parent, PooledConnection con)
    {
        if (con == null)
        {
            return;
        }

        if (con.getConnection().getClass().toString().contains("racle"))
        {
            try
            {
                String[] names = Thread.currentThread().getName().split(";");

                String module = "'" + names[0] + "'";
                String action = names.length > 1 ? "'" + names[1] + "'" : "NULL";
                String clientInfo = names.length > 2 ? "'" + names[2] + "'" : "NULL";

                Statement s = con.getConnection().createStatement();
                s.execute("CALL DBMS_APPLICATION_INFO.SET_MODULE(" + module + ", " + action + ")");
                s.execute("CALL DBMS_APPLICATION_INFO.SET_CLIENT_INFO(" + clientInfo + ")");
                s.close();
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
