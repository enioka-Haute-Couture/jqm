/**
 * Copyright © 2013 enioka. All rights reserved
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
